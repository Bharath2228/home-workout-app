package com.bharath.homeforge.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDateTime

class StreaksAndReminderTest {

    // Epoch day 6 is a Wednesday (1970-01-07); 13 and 20 are the following Wednesdays.
    @Test
    fun streakCountsConsecutiveWeeks() {
        assertEquals(3, Streaks.weeklyStreak(listOf(6L, 13L, 20L), 20L))
    }

    @Test
    fun emptyCurrentWeekDoesNotBreakTheStreak() {
        assertEquals(2, Streaks.weeklyStreak(listOf(6L, 13L), 20L))
    }

    @Test
    fun gapEndsTheStreak() {
        assertEquals(1, Streaks.weeklyStreak(listOf(6L, 20L), 20L))
        assertEquals(0, Streaks.weeklyStreak(listOf(6L), 20L))
        assertEquals(0, Streaks.weeklyStreak(emptyList(), 20L))
    }

    @Test
    fun workoutsThisWeekCountsOnlyThatWeek() {
        // Monday 4 Jan 1970 is epoch day 4... week of Wednesday 6 runs Mon 4 to Sun 10.
        assertEquals(2, Streaks.workoutsThisWeek(listOf(4L, 6L, 13L), 8L))
    }

    @Test
    fun reminderLaterTodayWhenStillAhead() {
        val now = LocalDateTime.of(2026, 10, 7, 10, 0) // Wednesday
        assertEquals(LocalDateTime.of(2026, 10, 7, 18, 0), ReminderTime.next(setOf(1, 3, 5), 18, 0, now))
    }

    @Test
    fun reminderSkipsToNextTrainingDay() {
        val now = LocalDateTime.of(2026, 10, 7, 19, 0)
        assertEquals(LocalDateTime.of(2026, 10, 9, 18, 0), ReminderTime.next(setOf(1, 3, 5), 18, 0, now))
    }

    @Test
    fun reminderWrapsToNextWeek() {
        val now = LocalDateTime.of(2026, 10, 7, 19, 0)
        assertEquals(LocalDateTime.of(2026, 10, 14, 18, 0), ReminderTime.next(setOf(3), 18, 0, now))
    }

    @Test
    fun noDaysMeansNoReminder() {
        assertNull(ReminderTime.next(emptySet(), 18, 0, LocalDateTime.of(2026, 10, 7, 10, 0)))
    }
}
