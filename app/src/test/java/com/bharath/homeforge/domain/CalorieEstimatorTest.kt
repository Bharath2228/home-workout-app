package com.bharath.homeforge.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalorieEstimatorTest {

    @Test
    fun oneHourAt70kg() {
        assertEquals(210.0, CalorieEstimator.activeKcal(70.0, 60.0), 1e-9)
    }

    @Test
    fun durationIsCappedAtThreeHours() {
        assertEquals(
            CalorieEstimator.activeKcal(70.0, 180.0),
            CalorieEstimator.activeKcal(70.0, 500.0),
            1e-9,
        )
    }

    @Test
    fun veryShortDurationCountsAsOneMinute() {
        assertEquals(
            CalorieEstimator.activeKcal(70.0, 1.0),
            CalorieEstimator.activeKcal(70.0, 0.0),
            1e-9,
        )
    }

    @Test
    fun defaultGoalMatchesMuscleGain() {
        assertEquals(
            CalorieEstimator.activeKcal(70.0, 60.0),
            CalorieEstimator.activeKcal(70.0, 60.0, Goal.MUSCLE_GAIN),
            1e-9,
        )
    }

    @Test
    fun weightLossBurnsMoreThanStrengthForTheSameTime() {
        val strength = CalorieEstimator.activeKcal(70.0, 60.0, Goal.STRENGTH)
        val weightLoss = CalorieEstimator.activeKcal(70.0, 60.0, Goal.WEIGHT_LOSS)
        assertTrue(weightLoss > strength)
    }
}
