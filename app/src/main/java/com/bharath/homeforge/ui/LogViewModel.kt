package com.bharath.homeforge.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bharath.homeforge.data.HealthSync
import com.bharath.homeforge.data.HomeForgeDatabase
import com.bharath.homeforge.data.LoggedSet
import com.bharath.homeforge.data.WorkoutSession
import com.bharath.homeforge.domain.Equipment
import com.bharath.homeforge.domain.PlannedExercise
import com.bharath.homeforge.domain.Progression
import com.bharath.homeforge.domain.RoutineGenerator
import com.bharath.homeforge.domain.SetResult
import com.bharath.homeforge.domain.Split
import kotlinx.coroutines.launch

class SetDraft(val setNumber: Int, weight: String, reps: String) {
    var weight by mutableStateOf(weight)
    var reps by mutableStateOf(reps)
    var done by mutableStateOf(false)
}

class ExerciseDraft(
    val planned: PlannedExercise,
    val lastText: String?,
    val sets: List<SetDraft>,
)

class WorkoutDraft(
    val split: Split,
    val dayIndex: Int,
    val startedAt: Long,
    val exercises: List<ExerciseDraft>,
)

class LogViewModel(app: Application) : AndroidViewModel(app) {

    private val dao = HomeForgeDatabase.get(app).workoutDao()
    private val health = HealthSync(app, dao)
    private val equipment = Equipment.Default

    var workout by mutableStateOf<WorkoutDraft?>(null)
        private set

    var message by mutableStateOf<String?>(null)
        private set

    fun start(split: Split, dayIndex: Int) {
        viewModelScope.launch {
            message = null
            val routine = RoutineGenerator.generate(split, dayIndex, equipment)
            val exercises = routine.items.map { item ->
                val last = dao.lastSetsFor(item.exercise.name).map { SetResult(it.weightKg, it.reps) }
                val weight = Progression.suggestedWeight(equipment, item.exercise, last, item.sets, item.reps)
                ExerciseDraft(
                    planned = item,
                    lastText = lastText(last),
                    sets = (1..item.sets).map {
                        SetDraft(it, weight?.let(::formatKg).orEmpty(), item.reps.last.toString())
                    },
                )
            }
            workout = WorkoutDraft(split, dayIndex, System.currentTimeMillis(), exercises)
        }
    }

    fun cancel() {
        workout = null
    }

    fun finish() {
        val draft = workout ?: return
        val now = System.currentTimeMillis()
        val sets = draft.exercises.flatMap { exercise ->
            exercise.sets.filter { it.done }.mapNotNull { set ->
                val reps = set.reps.toIntOrNull()?.takeIf { it > 0 } ?: return@mapNotNull null
                LoggedSet(
                    sessionId = 0,
                    exerciseName = exercise.planned.exercise.name,
                    setNumber = set.setNumber,
                    weightKg = if (exercise.planned.exercise.rig == null) null else set.weight.toDoubleOrNull(),
                    reps = reps,
                    loggedAt = now,
                )
            }
        }
        if (sets.isEmpty()) {
            message = "Tick at least one set as done before finishing."
            return
        }
        viewModelScope.launch {
            val sessionId = dao.saveWorkout(
                WorkoutSession(
                    startedAt = draft.startedAt,
                    endedAt = now,
                    splitName = draft.split.name,
                    dayIndex = draft.dayIndex,
                ),
                sets,
            )
            workout = null
            val saved = "Workout saved (${sets.size} sets)."
            message = if (health.enabled) {
                runCatching { health.syncSessionIfEnabled(sessionId) }.fold(
                    onSuccess = { if (it) "$saved Synced to Health Connect." else "$saved Health Connect permission missing, open Settings." },
                    onFailure = { "$saved Health Connect sync failed, retry from Settings." },
                )
            } else {
                saved
            }
        }
    }

    private fun lastText(last: List<SetResult>): String? {
        if (last.isEmpty()) return null
        val weight = last.mapNotNull { it.weightKg }.maxOrNull()
        val reps = last.joinToString(", ") { it.reps.toString() }
        return if (weight != null) "Last time: ${formatKg(weight)} kg x $reps" else "Last time: $reps reps"
    }
}

fun formatKg(kg: Double): String =
    if (kg == kg.toLong().toDouble()) kg.toLong().toString() else kg.toString()
