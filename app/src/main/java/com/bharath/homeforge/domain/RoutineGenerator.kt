package com.bharath.homeforge.domain

enum class Split(
    val label: String,
    val dayNames: List<String>,
    val tagline: String,
    val bestFor: String,
    val schedule: String,
) {
    FULL_BODY(
        "Full body",
        listOf("Day 1", "Day 2", "Day 3"),
        "Every workout trains your whole body.",
        "Beginners, or anyone who can train 2 to 3 days a week.",
        "3 days a week with a rest day between, such as Monday, Wednesday and Friday.",
    ),
    PUSH_PULL_LEGS(
        "Push / Pull / Legs",
        listOf("Push", "Pull", "Legs"),
        "Pushing muscles, pulling muscles and legs each get their own day.",
        "Lifters who can train 3 to 4 days a week and want more work per muscle.",
        "Do them in order and repeat: Push, Pull, Legs, then Push again.",
    ),
    SIX_DAY_PPL(
        "6-day PPL",
        listOf("Push 1", "Pull 1", "Legs 1", "Push 2", "Pull 2", "Legs 2"),
        "Push, pull and legs twice a week, with different exercises the second time.",
        "Experienced lifters who can train 5 to 6 days a week and recover well.",
        "6 days a week with one rest day. Do not skip your rest day.",
    ),

    /** An optional extra session for a rest day; every other split already ends with core work. */
    CORE_DAY(
        "Core day",
        listOf("Core"),
        "An extra session that only trains your abs.",
        "An optional add-on for a rest day, on top of any plan.",
        "15 to 20 minutes, any time.",
    ),
    ;

    companion object {
        /** The plans a user can follow. The core day is an extra, not a plan. */
        val programs: List<Split> = listOf(FULL_BODY, PUSH_PULL_LEGS, SIX_DAY_PPL)
    }
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
                Slot(Movement.CORE, 3, 12..15, pick = 2),
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
                Slot(Movement.CORE, 3, 12..15, pick = 3),
            )
            1 -> listOf(
                Slot(Movement.HORIZONTAL_PULL, 4, 6..8),
                Slot(Movement.HORIZONTAL_PULL, 3, 10..12, pick = 1),
                Slot(Movement.HINGE, 3, 8..10),
                Slot(Movement.SHOULDER_ISOLATION, 3, 12..15, pick = 1),
                Slot(Movement.BICEP, 3, 8..10),
                Slot(Movement.BICEP, 2, 10..12, pick = 1),
                Slot(Movement.CORE, 3, 12..15, pick = 4),
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
        Split.CORE_DAY -> List(6) { Slot(Movement.CORE, 3, 12..15, pick = it) }
    }

    /** Days 4-6 repeat push/pull/legs with different exercise variations. */
    private fun sixDaySecondRound(dayIndex: Int): List<Slot> = when (dayIndex) {
        3 -> listOf(
            Slot(Movement.VERTICAL_PUSH, 4, 6..8),
            Slot(Movement.HORIZONTAL_PUSH, 3, 8..10, pick = 2),
            Slot(Movement.VERTICAL_PUSH, 3, 10..12, pick = 1),
            Slot(Movement.SHOULDER_ISOLATION, 3, 12..15, pick = 1),
            Slot(Movement.TRICEP, 3, 10..12, pick = 1),
            Slot(Movement.TRICEP, 2, 10..12),
            Slot(Movement.CORE, 3, 12..15, pick = 5),
        )
        4 -> listOf(
            Slot(Movement.HORIZONTAL_PULL, 4, 6..8, pick = 2),
            Slot(Movement.HORIZONTAL_PULL, 3, 10..12, pick = 3),
            Slot(Movement.HINGE, 3, 8..10, pick = 2),
            Slot(Movement.SHOULDER_ISOLATION, 3, 12..15),
            Slot(Movement.BICEP, 3, 8..10, pick = 1),
            Slot(Movement.BICEP, 2, 10..12),
            Slot(Movement.CORE, 3, 12..15, pick = 6),
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
        level: Level = Level.INTERMEDIATE,
        noEquipment: Boolean = false,
    ): Routine {
        val items = slots(split, dayIndex).mapIndexed { index, slot ->
            val candidates = ExerciseLibrary.poolFor(slot.movement, level, noEquipment)
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
                sets = level.setsFor(slot.sets),
                reps = GoalProfile.reps(goal, exercise.movement, designed, exercise.timed),
                weightKg = exercise.rig?.let { LoadCalculator.snap(equipment, it, exercise.startKg * level.startFactor) },
                restSeconds = GoalProfile.restSeconds(goal, exercise.movement),
            )
        }
        return Routine(split, dayIndex, items)
    }
}
