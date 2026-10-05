package com.bharath.homeforge.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GoalProfileTest {

    @Test
    fun muscleGainKeepsTheDesignedReps() {
        assertEquals(8..10, GoalProfile.reps(Goal.MUSCLE_GAIN, Movement.SQUAT, 8..10, false))
    }

    @Test
    fun strengthLowersRepsOnCompoundsOnly() {
        assertEquals(6..7, GoalProfile.reps(Goal.STRENGTH, Movement.SQUAT, 8..10, false))
        assertEquals(10..12, GoalProfile.reps(Goal.STRENGTH, Movement.BICEP, 10..12, false))
    }

    @Test
    fun weightLossRaisesReps() {
        assertEquals(10..13, GoalProfile.reps(Goal.WEIGHT_LOSS, Movement.SQUAT, 8..10, false))
    }

    @Test
    fun timedExercisesAreNeverChanged() {
        Goal.entries.forEach {
            assertEquals(30..45, GoalProfile.reps(it, Movement.CORE, 30..45, true))
        }
    }

    @Test
    fun restIsLongestForStrengthAndShortestForWeightLoss() {
        val strength = GoalProfile.restSeconds(Goal.STRENGTH, Movement.SQUAT)
        val muscle = GoalProfile.restSeconds(Goal.MUSCLE_GAIN, Movement.SQUAT)
        val loss = GoalProfile.restSeconds(Goal.WEIGHT_LOSS, Movement.SQUAT)
        assertTrue(strength > muscle && muscle > loss)
        assertTrue(GoalProfile.restSeconds(Goal.MUSCLE_GAIN, Movement.BICEP) < muscle)
    }

    @Test
    fun everyGoalGivesValidRoutines() {
        val equipment = Equipment.Default
        Goal.entries.forEach { goal ->
            Split.entries.forEach { split ->
                split.dayNames.indices.forEach { day ->
                    RoutineGenerator.generate(split, day, equipment, goal = goal).items.forEach { item ->
                        assertTrue(item.reps.first <= item.reps.last)
                        assertTrue(item.restSeconds >= 30)
                        if (item.exercise.timed) assertTrue(item.reps.first >= 20) else assertTrue(item.reps.last < 30)
                    }
                }
            }
        }
    }
}
