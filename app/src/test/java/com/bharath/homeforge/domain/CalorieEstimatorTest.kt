package com.bharath.homeforge.domain

import org.junit.Assert.assertEquals
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
}
