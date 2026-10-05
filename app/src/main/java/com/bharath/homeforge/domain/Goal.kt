package com.bharath.homeforge.domain

import kotlin.math.roundToInt

enum class Goal(val label: String, val description: String, val baseRestSeconds: Int) {
    STRENGTH("Strength", "Heavier weights, fewer reps, longer rests.", 150),
    MUSCLE_GAIN("Muscle gain", "Balanced rep ranges and rest.", 90),
    WEIGHT_LOSS("Weight loss", "Higher reps, shorter rests.", 45),
}

object GoalProfile {

    private val compound = setOf(
        Movement.SQUAT,
        Movement.HINGE,
        Movement.LUNGE,
        Movement.HORIZONTAL_PUSH,
        Movement.VERTICAL_PUSH,
        Movement.HORIZONTAL_PULL,
    )

    fun reps(goal: Goal, movement: Movement, base: IntRange, timed: Boolean): IntRange {
        if (timed) return base
        return when (goal) {
            Goal.MUSCLE_GAIN -> base
            Goal.STRENGTH ->
                if (movement in compound) {
                    val first = (base.first - 2).coerceAtLeast(3)
                    first..(base.last - 3).coerceAtLeast(first)
                } else {
                    base
                }
            Goal.WEIGHT_LOSS -> (base.first + 2)..(base.last + 3)
        }
    }

    /** Compound lifts get the goal's full rest; isolation work gets about 60% of it. */
    fun restSeconds(goal: Goal, movement: Movement): Int {
        val base = goal.baseRestSeconds
        return if (movement in compound) base else maxOf(30, (base * 0.6 / 15).roundToInt() * 15)
    }
}
