package com.bharath.homeforge.ui

import android.app.Application
import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bharath.homeforge.data.ActiveExerciseSnapshot
import com.bharath.homeforge.data.ActiveSetSnapshot
import com.bharath.homeforge.data.ActiveWorkoutSnapshot
import com.bharath.homeforge.data.ActiveWorkoutStore
import com.bharath.homeforge.data.EquipmentRepository
import com.bharath.homeforge.data.HealthSync
import com.bharath.homeforge.data.HomeForgeDatabase
import com.bharath.homeforge.data.LoggedSet
import com.bharath.homeforge.data.RotationRepository
import com.bharath.homeforge.data.SwapRepository
import com.bharath.homeforge.data.UserPrefsRepository
import com.bharath.homeforge.data.WorkoutSession
import com.bharath.homeforge.data.UserPrefs
import com.bharath.homeforge.data.observeLevelStatus
import com.bharath.homeforge.data.observeReadiness
import com.bharath.homeforge.data.observeSchedule
import com.bharath.homeforge.domain.Adaptation
import com.bharath.homeforge.domain.Difficulty
import com.bharath.homeforge.domain.Effort
import com.bharath.homeforge.domain.Equipment
import com.bharath.homeforge.domain.Exercise
import com.bharath.homeforge.domain.ExerciseLibrary
import com.bharath.homeforge.domain.GeneralWarmUp
import com.bharath.homeforge.domain.GoalProfile
import com.bharath.homeforge.domain.Level
import com.bharath.homeforge.domain.LevelStatus
import com.bharath.homeforge.domain.PersonalRecords
import com.bharath.homeforge.domain.PlannedExercise
import com.bharath.homeforge.domain.RoutineGenerator
import com.bharath.homeforge.domain.ScheduleResult
import com.bharath.homeforge.domain.SetResult
import com.bharath.homeforge.domain.Split
import com.bharath.homeforge.domain.WarmUpMove
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.ceil

class SetDraft(weight: String, reps: String, difficulty: Difficulty? = null) {
    var weight by mutableStateOf(weight)
    var reps by mutableStateOf(reps)
    var done by mutableStateOf(false)
    var difficulty by mutableStateOf(difficulty)
}

class ExerciseDraft(
    val id: Int,
    val planned: PlannedExercise,
    val lastText: String?,
    val note: String?,
    sets: List<SetDraft>,
) {
    val sets = mutableStateListOf<SetDraft>().apply { addAll(sets) }

    /** True when this exercise is paired with the next one as a superset: no rest between them. */
    var isSuperset by mutableStateOf(false)
}

class WorkoutDraft(
    val split: Split,
    val dayIndex: Int,
    val startedAt: Long,
    val warmUpMoves: List<WarmUpMove>,
) {
    val exercises = mutableStateListOf<ExerciseDraft>()
    val warmUpDone = mutableStateListOf<Boolean>().apply { addAll(List(warmUpMoves.size) { false }) }
    var nextId = 0

    var pausedAt by mutableStateOf<Long?>(null)
    var pausedMs by mutableStateOf(0L)
}

class LogViewModel(app: Application) : AndroidViewModel(app) {

    private val dao = HomeForgeDatabase.get(app).workoutDao()
    private val health = HealthSync(app, dao)
    private val equipmentRepo = EquipmentRepository.get(app)
    private val swapRepo = SwapRepository.get(app)
    private val rotationRepo = RotationRepository.get(app)
    private val userPrefsRepo = UserPrefsRepository.get(app)
    private val activeWorkoutStore = ActiveWorkoutStore.get(app)

    val equipment: StateFlow<Equipment> = equipmentRepo.equipment

    val userPrefs: StateFlow<UserPrefs> = userPrefsRepo.userPrefs

    val schedule: StateFlow<ScheduleResult> = dao
        .observeSchedule(userPrefsRepo.userPrefs)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ScheduleResult.Empty)

    val levelStatus: StateFlow<LevelStatus> = dao
        .observeLevelStatus(userPrefsRepo.userPrefs)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LevelStatus.Initial)

    private var level = Level.INTERMEDIATE

    fun setNoEquipment(value: Boolean) = userPrefsRepo.update { it.copy(noEquipment = value) }

    val warnings: StateFlow<List<String>> = dao.observeReadiness()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    var workout by mutableStateOf<WorkoutDraft?>(null)
        private set

    init {
        workout = restoreActiveWorkout()
    }

    var message by mutableStateOf<String?>(null)
        private set

    var restRemaining by mutableIntStateOf(0)
        private set

    private var restEndsAt = 0L
    private var restJob: Job? = null
    private var pausedRestRemaining: Int? = null

    fun pause() {
        val draft = workout ?: return
        if (draft.pausedAt != null) return
        draft.pausedAt = System.currentTimeMillis()
        if (restRemaining > 0) {
            pausedRestRemaining = restRemaining
            restJob?.cancel()
        }
        persistActiveWorkout()
    }

    fun resume() {
        val draft = workout ?: return
        val pausedAt = draft.pausedAt ?: return
        draft.pausedMs += System.currentTimeMillis() - pausedAt
        draft.pausedAt = null
        pausedRestRemaining?.let {
            startRest(it)
            pausedRestRemaining = null
        }
        persistActiveWorkout()
    }

    fun start(split: Split, dayIndex: Int) {
        viewModelScope.launch {
            message = null
            val prefs = userPrefsRepo.userPrefs.value
            level = dao.observeLevelStatus(userPrefsRepo.userPrefs).first().level
            val routine = RoutineGenerator.generate(
                split,
                dayIndex,
                equipmentRepo.equipment.value,
                SwapRepository.offsetsFor(swapRepo.swaps.value, split, dayIndex),
                rotationRepo.rotation.value.block(),
                prefs.goal,
                level,
                prefs.noEquipment,
            )
            val movements = routine.items.map { it.exercise.movement }.toSet()
            val draft = WorkoutDraft(split, dayIndex, System.currentTimeMillis(), GeneralWarmUp.movesFor(movements))
            routine.items.forEach {
                draft.exercises += buildExercise(draft, it.exercise, it.sets, it.reps)
            }
            workout = draft
            persistActiveWorkout()
        }
    }

    fun toggleWarmUp(index: Int) {
        val list = workout?.warmUpDone ?: return
        if (index in list.indices) list[index] = !list[index]
        persistActiveWorkout()
    }

    fun toggleSuperset(exercise: ExerciseDraft) {
        exercise.isSuperset = !exercise.isSuperset
        persistActiveWorkout()
    }

    /** Called when a set is ticked done. A superset-linked exercise skips the rest until its partner finishes. */
    fun setDone(exercise: ExerciseDraft) {
        if (!exercise.isSuperset) startRest(exercise.planned.restSeconds)
        persistActiveWorkout()
    }

    fun addExercise(exercise: Exercise) {
        val draft = workout ?: return
        viewModelScope.launch {
            val reps = if (exercise.timed) {
                30..45
            } else {
                GoalProfile.reps(userPrefsRepo.userPrefs.value.goal, exercise.movement, 8..12, false)
            }
            draft.exercises += buildExercise(draft, exercise, level.setsFor(3), reps)
            persistActiveWorkout()
        }
    }

    fun moveExercise(index: Int, delta: Int) {
        val list = workout?.exercises ?: return
        val target = index + delta
        if (index !in list.indices || target !in list.indices) return
        list.add(target, list.removeAt(index))
        persistActiveWorkout()
    }

    fun removeExercise(index: Int) {
        val list = workout?.exercises ?: return
        if (index in list.indices) list.removeAt(index)
        persistActiveWorkout()
    }

    fun addSet(exercise: ExerciseDraft) {
        val last = exercise.sets.lastOrNull()
        exercise.sets += SetDraft(last?.weight.orEmpty(), last?.reps ?: "10")
        persistActiveWorkout()
    }

    fun removeLastSet(exercise: ExerciseDraft) {
        if (exercise.sets.size > 1) exercise.sets.removeAt(exercise.sets.lastIndex)
        persistActiveWorkout()
    }

    fun startRest(seconds: Int) {
        if (!userPrefsRepo.userPrefs.value.restTimerEnabled || seconds <= 0) return
        restEndsAt = System.currentTimeMillis() + seconds * 1000L
        restJob?.cancel()
        refreshRest()
        restJob = viewModelScope.launch {
            while (restRemaining > 0) {
                delay(250)
                refreshRest()
            }
            alertRestOver()
        }
    }

    fun addRest(seconds: Int) {
        if (restRemaining <= 0) return
        restEndsAt += seconds * 1000L
        refreshRest()
    }

    fun skipRest() {
        restJob?.cancel()
        restRemaining = 0
    }

    fun cancel() {
        skipRest()
        pausedRestRemaining = null
        workout = null
        activeWorkoutStore.clear()
    }

    fun finish() {
        val draft = workout ?: return
        val now = System.currentTimeMillis()
        val endedAt = now - draft.pausedMs - (draft.pausedAt?.let { now - it } ?: 0L)
        val sets = draft.exercises.flatMap { exercise ->
            exercise.sets.mapIndexedNotNull { index, set ->
                if (!set.done) return@mapIndexedNotNull null
                val reps = set.reps.toIntOrNull()?.takeIf { it > 0 } ?: return@mapIndexedNotNull null
                LoggedSet(
                    sessionId = 0,
                    exerciseName = exercise.planned.exercise.name,
                    setNumber = index + 1,
                    weightKg = if (exercise.planned.exercise.rig == null) null else set.weight.toDoubleOrNull(),
                    reps = reps,
                    loggedAt = now,
                    difficulty = set.difficulty?.name,
                )
            }
        }
        if (sets.isEmpty()) {
            message = "Tick at least one set as done before finishing."
            return
        }
        skipRest()
        viewModelScope.launch {
            val previous = dao.allSetsOnce().map { Effort(it.exerciseName, it.weightKg, it.reps) }
            val records = PersonalRecords.newRecords(previous, sets.map { Effort(it.exerciseName, it.weightKg, it.reps) })

            val sessionId = dao.saveWorkout(
                WorkoutSession(
                    startedAt = draft.startedAt,
                    endedAt = endedAt,
                    splitName = draft.split.name,
                    dayIndex = draft.dayIndex,
                    warmUpDone = draft.warmUpDone.all { it },
                ),
                sets,
            )
            workout = null
            activeWorkoutStore.clear()

            val recordText = if (records.isEmpty()) "" else " New PR: ${records.joinToString { effortText(it) }}."
            val saved = "Workout saved (${sets.size} sets, ${durationText(endedAt - draft.startedAt)}).$recordText"
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

    private suspend fun buildExercise(
        draft: WorkoutDraft,
        exercise: Exercise,
        sets: Int,
        reps: IntRange,
    ): ExerciseDraft {
        val history = dao.recentSetsFor(exercise.name)
            .groupBy { it.sessionId }.values
            .map { session ->
                session.map {
                    SetResult(it.weightKg, it.reps, it.difficulty?.let { d -> runCatching { Difficulty.valueOf(d) }.getOrNull() })
                }
            }
        val suggestion = Adaptation.suggest(
            equipmentRepo.equipment.value,
            exercise,
            history,
            sets,
            reps,
            level.startFactor,
        )
        val weight = suggestion.weightKg
        return ExerciseDraft(
            id = draft.nextId++,
            planned = PlannedExercise(
                slotIndex = -1,
                exercise = exercise,
                sets = suggestion.sets,
                reps = suggestion.reps,
                weightKg = weight,
                restSeconds = GoalProfile.restSeconds(userPrefsRepo.userPrefs.value.goal, exercise.movement),
            ),
            lastText = lastText(history.firstOrNull().orEmpty()),
            note = suggestion.reason,
            sets = List(suggestion.sets) { SetDraft(weight?.let(::formatKg).orEmpty(), suggestion.reps.last.toString()) },
        )
    }

    private fun refreshRest() {
        restRemaining = maxOf(0.0, ceil((restEndsAt - System.currentTimeMillis()) / 1000.0)).toInt()
    }

    private fun alertRestOver() {
        val context = getApplication<Application>()
        runCatching {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(VibrationEffect.createOneShot(600, VibrationEffect.DEFAULT_AMPLITUDE))
        }
        runCatching {
            ToneGenerator(AudioManager.STREAM_NOTIFICATION, 80).startTone(ToneGenerator.TONE_PROP_BEEP2, 400)
        }
    }

    private fun effortText(effort: Effort): String =
        if (effort.weightKg != null) "${effort.exercise} ${formatKg(effort.weightKg)} kg x ${effort.reps}"
        else "${effort.exercise} ${effort.reps} reps"

    private fun lastText(last: List<SetResult>): String? {
        if (last.isEmpty()) return null
        val weight = last.mapNotNull { it.weightKg }.maxOrNull()
        val reps = last.joinToString(", ") { it.reps.toString() }
        return if (weight != null) "Last time: ${formatKg(weight)} kg x $reps" else "Last time: $reps reps"
    }

    /** Saves the in-progress workout to disk so it survives the app process being killed. Call after each edit. */
    fun persistActiveWorkout() {
        val draft = workout
        if (draft == null) {
            activeWorkoutStore.clear()
            return
        }
        activeWorkoutStore.save(
            ActiveWorkoutSnapshot(
                splitName = draft.split.name,
                dayIndex = draft.dayIndex,
                startedAt = draft.startedAt,
                pausedAt = draft.pausedAt,
                pausedMs = draft.pausedMs,
                nextId = draft.nextId,
                warmUpMoveNames = draft.warmUpMoves.map { it.name },
                warmUpMoveDetails = draft.warmUpMoves.map { it.detail },
                warmUpDone = draft.warmUpDone.toList(),
                exercises = draft.exercises.map { e ->
                    ActiveExerciseSnapshot(
                        id = e.id,
                        exerciseName = e.planned.exercise.name,
                        sets = e.planned.sets,
                        repsFirst = e.planned.reps.first,
                        repsLast = e.planned.reps.last,
                        weightKg = e.planned.weightKg,
                        restSeconds = e.planned.restSeconds,
                        lastText = e.lastText,
                        note = e.note,
                        isSuperset = e.isSuperset,
                        setDrafts = e.sets.map { s -> ActiveSetSnapshot(s.weight, s.reps, s.done, s.difficulty?.name) },
                    )
                },
            ),
        )
    }

    /** Rebuilds the draft from the last snapshot, skipping any exercise no longer in the library. */
    private fun restoreActiveWorkout(): WorkoutDraft? {
        val snapshot = activeWorkoutStore.load() ?: return null
        val split = runCatching { Split.valueOf(snapshot.splitName) }.getOrNull() ?: return null
        val warmUpMoves = snapshot.warmUpMoveNames.zip(snapshot.warmUpMoveDetails) { n, d -> WarmUpMove(n, d) }
        val draft = WorkoutDraft(split, snapshot.dayIndex, snapshot.startedAt, warmUpMoves)
        draft.pausedAt = snapshot.pausedAt
        draft.pausedMs = snapshot.pausedMs
        draft.nextId = snapshot.nextId
        draft.warmUpDone.clear()
        draft.warmUpDone.addAll(
            if (snapshot.warmUpDone.size == warmUpMoves.size) snapshot.warmUpDone else List(warmUpMoves.size) { false },
        )
        snapshot.exercises.forEach { e ->
            val exercise = ExerciseLibrary.all.find { it.name == e.exerciseName } ?: return@forEach
            val exerciseDraft = ExerciseDraft(
                id = e.id,
                planned = PlannedExercise(
                    slotIndex = -1,
                    exercise = exercise,
                    sets = e.sets,
                    reps = e.repsFirst..e.repsLast,
                    weightKg = e.weightKg,
                    restSeconds = e.restSeconds,
                ),
                lastText = e.lastText,
                note = e.note,
                sets = e.setDrafts.map { s ->
                    SetDraft(s.weight, s.reps, s.difficulty?.let { d -> runCatching { Difficulty.valueOf(d) }.getOrNull() })
                        .apply { done = s.done }
                },
            )
            exerciseDraft.isSuperset = e.isSuperset
            draft.exercises += exerciseDraft
        }
        return draft.takeIf { it.exercises.isNotEmpty() }
    }
}

fun formatKg(kg: Double): String =
    if (kg == kg.toLong().toDouble()) kg.toLong().toString() else kg.toString()

fun formatClock(totalSeconds: Int): String = "%d:%02d".format(totalSeconds / 60, totalSeconds % 60)
