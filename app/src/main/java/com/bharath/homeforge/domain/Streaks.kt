package com.bharath.homeforge.domain

object Streaks {

    /** Monday-based week number; epoch day 0 (1970-01-01) was a Thursday. */
    private fun weekIndex(epochDay: Long): Long = Math.floorDiv(epochDay + 3, 7L)

    /** Consecutive weeks with at least one workout. An empty current week doesn't break the streak yet. */
    fun weeklyStreak(workoutEpochDays: Collection<Long>, todayEpochDay: Long): Int {
        val weeks = workoutEpochDays.map { weekIndex(it) }.toSet()
        var week = weekIndex(todayEpochDay)
        if (week !in weeks) week -= 1
        var streak = 0
        while (week in weeks) {
            streak++
            week--
        }
        return streak
    }

    fun workoutsThisWeek(workoutEpochDays: Collection<Long>, todayEpochDay: Long): Int =
        workoutEpochDays.count { weekIndex(it) == weekIndex(todayEpochDay) }
}
