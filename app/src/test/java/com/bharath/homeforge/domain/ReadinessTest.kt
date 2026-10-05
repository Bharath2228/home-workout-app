package com.bharath.homeforge.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReadinessTest {

    private fun days(vararg epochDays: Long, sets: Int = 20) = epochDays.map { DayLoad(it, sets) }

    @Test
    fun noWorkoutsMeansNoWarnings() {
        assertTrue(Readiness.assess(emptyList(), 100).isEmpty())
    }

    @Test
    fun sixDaysInARowAsksForRest() {
        val warnings = Readiness.assess(days(95, 96, 97, 98, 99, 100, sets = 5), 100)
        assertTrue(warnings.any { it.contains("6 days in a row") })
    }

    @Test
    fun fiveDaysInARowIsFine() {
        assertTrue(Readiness.assess(days(96, 97, 98, 99, 100, sets = 5), 100).isEmpty())
    }

    @Test
    fun streakStillCountsWhenTodayIsARestDay() {
        val warnings = Readiness.assess(days(94, 95, 96, 97, 98, 99, sets = 5), 100)
        assertTrue(warnings.any { it.contains("6 days in a row") })
    }

    @Test
    fun volumeSpikeIsFlagged() {
        val history = days(75, 82, 89, sets = 30) + DayLoad(100, 60)
        val warnings = Readiness.assess(history, 100)
        assertTrue(warnings.any { it.contains("above your recent weekly average") })
    }

    @Test
    fun steadyVolumeIsNotFlagged() {
        val history = days(75, 82, 89, 96, sets = 30)
        assertTrue(Readiness.assess(history, 98).none { it.contains("above your recent") })
    }

    @Test
    fun longGapSuggestsStartingLighter() {
        val warnings = Readiness.assess(days(80), 100)
        assertEquals(1, warnings.size)
        assertTrue(warnings[0].contains("20 days"))
    }
}
