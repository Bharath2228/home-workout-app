package com.bharath.homeforge.domain

import kotlin.math.roundToInt

data class DayLoad(val epochDay: Long, val sets: Int)

/** Simple rest-day and load warnings from your own logged workouts. */
object Readiness {

    private const val MAX_DAYS_IN_A_ROW = 6
    private const val SPIKE_FACTOR = 1.5
    private const val MIN_WEEKLY_SETS_FOR_SPIKE = 20.0
    private const val LONG_GAP_DAYS = 10

    fun assess(days: List<DayLoad>, today: Long): List<String> {
        if (days.isEmpty()) return emptyList()
        val warnings = mutableListOf<String>()
        val trained = days.map { it.epochDay }.toSet()

        var cursor = if (today in trained) today else today - 1
        var inARow = 0
        while (cursor in trained) {
            inARow++
            cursor--
        }
        if (inARow >= MAX_DAYS_IN_A_ROW) {
            warnings += "You have trained $inARow days in a row. A rest day will help you recover."
        }

        val thisWeek = days.filter { it.epochDay in (today - 6)..today }.sumOf { it.sets }
        val priorWeeklyAverage = days.filter { it.epochDay in (today - 27)..(today - 7) }.sumOf { it.sets } / 3.0
        if (priorWeeklyAverage >= MIN_WEEKLY_SETS_FOR_SPIKE && thisWeek > priorWeeklyAverage * SPIKE_FACTOR) {
            val percent = ((thisWeek / priorWeeklyAverage - 1) * 100).roundToInt()
            warnings += "Your sets this week are $percent% above your recent weekly average. Watch your recovery."
        }

        val gap = today - trained.max()
        if (gap >= LONG_GAP_DAYS) {
            warnings += "It has been $gap days since your last workout. Consider starting a bit lighter."
        }
        return warnings
    }
}
