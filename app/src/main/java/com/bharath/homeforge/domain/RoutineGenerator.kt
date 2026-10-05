package com.bharath.homeforge.domain

enum class Split(val label: String, val dayNames: List<String>) {
    FULL_BODY("Full body", listOf("Day A", "Day B", "Day C")),
    PUSH_PULL_LEGS("Push / Pull / Legs", listOf("Push", "Pull", "Legs")),
    SIX_DAY_PPL("6-day PPL", listOf("Push A", "Pull A", "Legs A", "Push B", "Pull B", "Legs B")),
}

data class Slot(
    val movement: Movement,
    val sets: Int,
    val reps: IntRange,
    val pick: Int = 0,
)

data class PlannedExercise(
    val slotIndex: Int,
    val exercise: Exercise,
    val sets: Int,
    val reps: IntRange,
    val weightKg: Double?,
    val restSeconds: Int = 90,
)

data class Routine(
    val split: Split,
    val dayIndex: Int,
    val items: List<PlannedExercise>,
)

object RoutineGenerator {

    fun slots(split: Split, dayIndex: Int): List<Slot> = when (split) {
        Split.FULL_BODY -> when (dayIndex) {
            0 -> listOf(
                Slot(Movement.SQUAT, 3, 8..10),
                Slot(Movement.HORIZONTAL_PUSH, 3, 8..10),
                Slot(Movement.HORIZONTAL_PULL, 3, 8..10),
                Slot(Movement.SHOULDER_ISOLATION, 2, 12..15),
                Slot(Movement.BICEP, 2, 10..12),
                Slot(Movement.CORE, 3, 30..45),
            )
            1 -> listOf(
                Slot(Movement.HINGE, 3, 8..10),
                Slot(Movement.VERTICAL_PUSH, 3, 8..10),
                Slot(Movement.HORIZONTAL_PULL, 3, 8..10, pick = 1),
                Slot(Movement.LUNGE, 3, 10..12),
                Slot(Movement.TRICEP, 2, 10..12),
                Slot(Movement.CORE, 3, 10..15, pick = 1),
            )
            else -> listOf(
                Slot(Movement.SQUAT, 3, 10..12, pick = 1),
                Slot(Movement.HORIZONTAL_PUSH, 3, 10..12, pick = 1),
                Slot(Movement.HORIZONTAL_PULL, 3, 10..12, pick = 2),
                Slot(Movement.VERTICAL_PUSH, 2, 10..12, pick = 1),
                Slot(Movement.BICEP, 2, 10..12, pick = 1),
                Slot(Movement.CALF, 3, 12..15),
            )
        }
        Split.PUSH_PULL_LEGS -> when (dayIndex) {
            0 -> listOf(
                Slot(Movement.HORIZONTAL_PUSH, 4, 6..8),
                Slot(Movement.VERTICAL_PUSH, 3, 8..10),
                Slot(Movement.HORIZONTAL_PUSH, 3, 10..12, pick = 1),
                Slot(Movement.SHOULDER_ISOLATION, 3, 12..15),
                Slot(Movement.TRICEP, 3, 10..12),
                Slot(Movement.TRICEP, 2, 10..12, pick = 1),
            )
            1 -> listOf(
                Slot(Movement.HORIZONTAL_PULL, 4, 6..8),
                Slot(Movement.HORIZONTAL_PULL, 3, 10..12, pick = 1),
                Slot(Movement.HINGE, 3, 8..10),
                Slot(Movement.SHOULDER_ISOLATION, 3, 12..15, pick = 1),
                Slot(Movement.BICEP, 3, 8..10),
                Slot(Movement.BICEP, 2, 10..12, pick = 1),
            )
            else -> listOf(
                Slot(Movement.SQUAT, 4, 6..8),
                Slot(Movement.HINGE, 3, 8..10, pick = 1),
                Slot(Movement.LUNGE, 3, 10..12),
                Slot(Movement.SQUAT, 3, 10..12, pick = 1),
                Slot(Movement.CALF, 4, 12..15),
                Slot(Movement.CORE, 3, 30..45),
            )
        }
        Split.SIX_DAY_PPL ->
            if (dayIndex < 3) slots(Split.PUSH_PULL_LEGS, dayIndex) else sixDaySecondRound(dayIndex)
    }

    /** Days 4-6 repeat push/pull/legs with different exercise variations. */
    private fun sixDaySecondRound(dayIndex: Int): List<Slot> = when (dayIndex) {
        3 -> listOf(
            Slot(Movement.VERTICAL_PUSH, 4, 6..8),
            Slot(Movement.HORIZONTAL_PUSH, 3, 8..10, pick = 2),
            Slot(Movement.VERTICAL_PUSH, 3, 10..12, pick = 1),
            Slot(Movement.SHOULDER_ISOLATION, 3, 12..15, pick = 1),
            Slot(Movement.TRICEP, 3, 10..12, pick = 2),
            Slot(Movement.TRICEP, 2, 10..12),
        )
        4 -> listOf(
            Slot(Movement.HORIZONTAL_PULL, 4, 6..8, pick = 2),
            Slot(Movement.HORIZONTAL_PULL, 3, 10..12, pick = 3),
            Slot(Movement.HINGE, 3, 8..10, pick = 2),
            Slot(Movement.SHOULDER_ISOLATION, 3, 12..15),
            Slot(Movement.BICEP, 3, 8..10, pick = 1),
            Slot(Movement.BICEP, 2, 10..12),
        )
        else -> listOf(
            Slot(Movement.SQUAT, 4, 6..8, pick = 2),
            Slot(Movement.HINGE, 3, 8..10, pick = 3),
            Slot(Movement.LUNGE, 3, 10..12, pick = 1),
            Slot(Movement.SQUAT, 3, 10..12, pick = 3),
            Slot(Movement.CALF, 4, 12..15, pick = 1),
            Slot(Movement.CORE, 3, 10..15, pick = 1),
        )
    }

    /** [pickOffsets] maps slot index to extra rotation used when the user swaps an exercise. */
    fun generate(
        split: Split,
        dayIndex: Int,
        equipment: Equipment,
        pickOffsets: Map<Int, Int> = emptyMap(),
        rotation: Int = 0,
        goal: Goal = Goal.MUSCLE_GAIN,
    ): Routine {
        val items = slots(split, dayIndex).mapIndexed { index, slot ->
            val candidates = ExerciseLibrary.forMovement(slot.movement)
            val exercise = candidates[(slot.pick + rotation + (pickOffsets[index] ?: 0)) % candidates.size]
            // Core slots hold either seconds or reps depending on which exercise lands in them.
            val designed = when {
                exercise.timed && slot.reps.first < 20 -> 30..45
                !exercise.timed && slot.reps.first >= 20 -> 10..15
                else -> slot.reps
            }
            PlannedExercise(
                slotIndex = index,
                exercise = exercise,
                sets = slot.sets,
                reps = GoalProfile.reps(goal, exercise.movement, designed, exercise.timed),
                weightKg = exercise.rig?.let { LoadCalculator.snap(equipment, it, exercise.startKg) },
                restSeconds = GoalProfile.restSeconds(goal, exercise.movement),
            )
        }
        return Routine(split, dayIndex, items)
    }
}
