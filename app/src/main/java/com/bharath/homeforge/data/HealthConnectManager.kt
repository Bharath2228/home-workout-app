package com.bharath.homeforge.data

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.ExerciseSegment
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.Record
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.records.metadata.Metadata
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.health.connect.client.units.Energy
import androidx.health.connect.client.units.Mass
import com.bharath.homeforge.domain.CalorieEstimator
import com.bharath.homeforge.domain.Exercise
import com.bharath.homeforge.domain.Goal
import com.bharath.homeforge.domain.ExerciseLibrary
import com.bharath.homeforge.domain.Movement
import com.bharath.homeforge.domain.Split
import com.bharath.homeforge.ui.formatKg
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

class SessionWithSets(val session: WorkoutSession, val sets: List<LoggedSet>)

class HealthConnectManager(private val context: Context) {

    val permissions: Set<String> = setOf(
        HealthPermission.getReadPermission(ExerciseSessionRecord::class),
        HealthPermission.getWritePermission(ExerciseSessionRecord::class),
        HealthPermission.getWritePermission(ActiveCaloriesBurnedRecord::class),
        HealthPermission.getWritePermission(WeightRecord::class),
    )

    fun sdkStatus(): Int = HealthConnectClient.getSdkStatus(context, PROVIDER_PACKAGE)

    private fun client(): HealthConnectClient = HealthConnectClient.getOrCreate(context)

    suspend fun hasAllPermissions(): Boolean =
        sdkStatus() == HealthConnectClient.SDK_AVAILABLE &&
            client().permissionController.getGrantedPermissions().containsAll(permissions)

    /** Writes each workout, plus an estimated calorie record when [bodyWeightKg] is known. */
    suspend fun writeSessions(sessions: List<SessionWithSets>, bodyWeightKg: Double?, goal: Goal) {
        if (sessions.isEmpty()) return
        // An ever-increasing version lets a re-sync overwrite the earlier record after an edit.
        val version = System.currentTimeMillis()
        val records = ArrayList<Record>()
        sessions.forEach { item ->
            val window = windowFor(item.session)
            records += sessionRecord(item, window, version)
            if (bodyWeightKg != null) records += caloriesRecord(item, window, bodyWeightKg, goal, version)
        }
        client().insertRecords(records)
    }

    suspend fun deleteSession(sessionId: Long) {
        val client = client()
        client.deleteRecords(
            ExerciseSessionRecord::class,
            recordIdsList = emptyList(),
            clientRecordIdsList = listOf("homeforge-session-$sessionId"),
        )
        client.deleteRecords(
            ActiveCaloriesBurnedRecord::class,
            recordIdsList = emptyList(),
            clientRecordIdsList = listOf("homeforge-calories-$sessionId"),
        )
    }

    suspend fun writeWeight(kg: Double) {
        val now = Instant.now()
        client().insertRecords(
            listOf(
                WeightRecord(
                    time = now,
                    zoneOffset = ZoneId.systemDefault().rules.getOffset(now),
                    weight = Mass.kilograms(kg),
                ),
            ),
        )
    }

    suspend fun countFromThisAppLast30Days(): Int {
        val now = Instant.now()
        val response = client().readRecords(
            ReadRecordsRequest(
                recordType = ExerciseSessionRecord::class,
                timeRangeFilter = TimeRangeFilter.between(now.minus(30, ChronoUnit.DAYS), now),
            ),
        )
        return response.records.count { it.metadata.dataOrigin.packageName == context.packageName }
    }

    private class Window(
        val start: Instant,
        val end: Instant,
        val startOffset: ZoneOffset,
        val endOffset: ZoneOffset,
    )

    private fun windowFor(session: WorkoutSession): Window {
        val zone = ZoneId.systemDefault()
        val start = Instant.ofEpochMilli(session.startedAt)
        var end = Instant.ofEpochMilli(session.endedAt)
        if (!end.isAfter(start)) end = start.plusSeconds(60)
        return Window(start, end, zone.rules.getOffset(start), zone.rules.getOffset(end))
    }

    private fun sessionRecord(item: SessionWithSets, window: Window, version: Long): ExerciseSessionRecord =
        runCatching { buildSession(item, window, segments(item, window), version) }
            .getOrElse { buildSession(item, window, emptyList(), version) }

    private fun buildSession(
        item: SessionWithSets,
        window: Window,
        segments: List<ExerciseSegment>,
        version: Long,
    ): ExerciseSessionRecord {
        val split = runCatching { Split.valueOf(item.session.splitName) }.getOrNull()
        val dayName = split?.dayNames?.getOrNull(item.session.dayIndex)
        val title = listOfNotNull("HomeForge", split?.label, dayName).joinToString(" - ")

        return ExerciseSessionRecord(
            startTime = window.start,
            startZoneOffset = window.startOffset,
            endTime = window.end,
            endZoneOffset = window.endOffset,
            exerciseType = ExerciseSessionRecord.EXERCISE_TYPE_STRENGTH_TRAINING,
            title = title,
            notes = summary(item.sets),
            segments = segments,
            metadata = Metadata(
                clientRecordId = "homeforge-session-${item.session.id}",
                clientRecordVersion = version,
            ),
        )
    }

    private fun caloriesRecord(
        item: SessionWithSets,
        window: Window,
        bodyWeightKg: Double,
        goal: Goal,
        version: Long,
    ): ActiveCaloriesBurnedRecord {
        val minutes = Duration.between(window.start, window.end).toMillis() / 60_000.0
        return ActiveCaloriesBurnedRecord(
            startTime = window.start,
            startZoneOffset = window.startOffset,
            endTime = window.end,
            endZoneOffset = window.endOffset,
            energy = Energy.kilocalories(CalorieEstimator.activeKcal(bodyWeightKg, minutes, goal)),
            metadata = Metadata(
                clientRecordId = "homeforge-calories-${item.session.id}",
                clientRecordVersion = version,
            ),
        )
    }

    /** One segment per exercise, in the order performed, splitting the session time evenly. */
    private fun segments(item: SessionWithSets, window: Window): List<ExerciseSegment> {
        val groups = item.sets.sortedBy { it.id }.groupBy { it.exerciseName }.entries.toList()
        if (groups.isEmpty()) return emptyList()

        val sliceMs = Duration.between(window.start, window.end).toMillis() / groups.size
        return groups.mapIndexed { index, (name, sets) ->
            val segmentStart = window.start.plusMillis(sliceMs * index)
            val segmentEnd = if (index == groups.lastIndex) window.end else window.start.plusMillis(sliceMs * (index + 1))
            val exercise = ExerciseLibrary.all.find { it.name == name }
            val type = exercise?.let(::segmentType) ?: ExerciseSegment.EXERCISE_SEGMENT_TYPE_OTHER_WORKOUT
            val noReps = exercise == null || exercise.timed ||
                type == ExerciseSegment.EXERCISE_SEGMENT_TYPE_PLANK ||
                type == ExerciseSegment.EXERCISE_SEGMENT_TYPE_OTHER_WORKOUT
            ExerciseSegment(
                startTime = segmentStart,
                endTime = segmentEnd,
                segmentType = type,
                repetitions = if (noReps) 0 else sets.sumOf { it.reps },
            )
        }
    }

    private fun segmentType(exercise: Exercise): Int = when (exercise.movement) {
        Movement.SQUAT -> ExerciseSegment.EXERCISE_SEGMENT_TYPE_SQUAT
        Movement.HINGE -> ExerciseSegment.EXERCISE_SEGMENT_TYPE_DEADLIFT
        Movement.LUNGE -> ExerciseSegment.EXERCISE_SEGMENT_TYPE_LUNGE
        Movement.HORIZONTAL_PUSH ->
            if (exercise.name.contains("push-up", ignoreCase = true)) {
                ExerciseSegment.EXERCISE_SEGMENT_TYPE_OTHER_WORKOUT
            } else {
                ExerciseSegment.EXERCISE_SEGMENT_TYPE_BENCH_PRESS
            }
        Movement.VERTICAL_PUSH -> ExerciseSegment.EXERCISE_SEGMENT_TYPE_SHOULDER_PRESS
        Movement.HORIZONTAL_PULL -> ExerciseSegment.EXERCISE_SEGMENT_TYPE_DUMBBELL_ROW
        Movement.BICEP -> ExerciseSegment.EXERCISE_SEGMENT_TYPE_ARM_CURL
        Movement.TRICEP -> ExerciseSegment.EXERCISE_SEGMENT_TYPE_WEIGHTLIFTING
        Movement.SHOULDER_ISOLATION -> ExerciseSegment.EXERCISE_SEGMENT_TYPE_LATERAL_RAISE
        Movement.CORE ->
            if (exercise.timed) ExerciseSegment.EXERCISE_SEGMENT_TYPE_PLANK else ExerciseSegment.EXERCISE_SEGMENT_TYPE_OTHER_WORKOUT
        Movement.CALF -> ExerciseSegment.EXERCISE_SEGMENT_TYPE_OTHER_WORKOUT
    }

    private fun summary(sets: List<LoggedSet>): String =
        sets.sortedBy { it.id }.groupBy { it.exerciseName }.entries.joinToString("\n") { (name, exerciseSets) ->
            val weight = exerciseSets.mapNotNull { it.weightKg }.maxOrNull()
            val reps = exerciseSets.joinToString(", ") { it.reps.toString() }
            if (weight != null) "$name: ${formatKg(weight)} kg x $reps" else "$name: $reps reps"
        }

    companion object {
        const val PROVIDER_PACKAGE = "com.google.android.apps.healthdata"
    }
}
