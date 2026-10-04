package com.bharath.homeforge.data

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.metadata.Metadata
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.bharath.homeforge.domain.Split
import com.bharath.homeforge.ui.formatKg
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit

class SessionWithSets(val session: WorkoutSession, val sets: List<LoggedSet>)

class HealthConnectManager(private val context: Context) {

    val permissions: Set<String> = setOf(
        HealthPermission.getReadPermission(ExerciseSessionRecord::class),
        HealthPermission.getWritePermission(ExerciseSessionRecord::class),
    )

    fun sdkStatus(): Int = HealthConnectClient.getSdkStatus(context, PROVIDER_PACKAGE)

    private fun client(): HealthConnectClient = HealthConnectClient.getOrCreate(context)

    suspend fun hasAllPermissions(): Boolean =
        sdkStatus() == HealthConnectClient.SDK_AVAILABLE &&
            client().permissionController.getGrantedPermissions().containsAll(permissions)

    suspend fun writeSessions(sessions: List<SessionWithSets>) {
        if (sessions.isEmpty()) return
        client().insertRecords(sessions.map(::toRecord))
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

    private fun toRecord(item: SessionWithSets): ExerciseSessionRecord {
        val zone = ZoneId.systemDefault()
        val start = Instant.ofEpochMilli(item.session.startedAt)
        var end = Instant.ofEpochMilli(item.session.endedAt)
        if (!end.isAfter(start)) end = start.plusSeconds(60)

        val split = runCatching { Split.valueOf(item.session.splitName) }.getOrNull()
        val dayName = split?.dayNames?.getOrNull(item.session.dayIndex)
        val title = listOfNotNull("HomeForge", split?.label, dayName).joinToString(" - ")

        return ExerciseSessionRecord(
            startTime = start,
            startZoneOffset = zone.rules.getOffset(start),
            endTime = end,
            endZoneOffset = zone.rules.getOffset(end),
            exerciseType = ExerciseSessionRecord.EXERCISE_TYPE_STRENGTH_TRAINING,
            title = title,
            notes = summary(item.sets),
            metadata = Metadata(clientRecordId = "homeforge-session-${item.session.id}", clientRecordVersion = 1L),
        )
    }

    private fun summary(sets: List<LoggedSet>): String =
        sets.groupBy { it.exerciseName }.entries.joinToString("\n") { (name, exerciseSets) ->
            val weight = exerciseSets.mapNotNull { it.weightKg }.maxOrNull()
            val reps = exerciseSets.joinToString(", ") { it.reps.toString() }
            if (weight != null) "$name: ${formatKg(weight)} kg x $reps" else "$name: $reps reps"
        }

    companion object {
        const val PROVIDER_PACKAGE = "com.google.android.apps.healthdata"
    }
}
