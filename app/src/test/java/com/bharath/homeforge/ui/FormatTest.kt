package com.bharath.homeforge.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class FormatTest {

    @Test
    fun runningClockUnderAnHour() {
        assertEquals("0:00", formatDuration(0))
        assertEquals("0:09", formatDuration(9))
        assertEquals("12:34", formatDuration(12 * 60 + 34))
    }

    @Test
    fun runningClockOverAnHour() {
        assertEquals("1:05:09", formatDuration(3600 + 5 * 60 + 9))
    }

    @Test
    fun negativeTimeIsTreatedAsZero() {
        assertEquals("0:00", formatDuration(-5))
        assertEquals("under 1 min", durationText(-1000))
    }

    @Test
    fun finishedWorkoutDurationInPlainWords() {
        assertEquals("under 1 min", durationText(20_000))
        assertEquals("1 min", durationText(60_000))
        assertEquals("45 min", durationText(45 * 60_000L))
        assertEquals("1 h 00 min", durationText(60 * 60_000L))
        assertEquals("1 h 05 min", durationText(65 * 60_000L))
    }
}
