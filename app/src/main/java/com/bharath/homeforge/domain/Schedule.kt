package com.bharath.homeforge.domain

import java.time.LocalDate

enum class DayStatus {
    /** A workout logged on a scheduled training day. */
    DONE,

    /** A workout logged on a day that wasn't a training day. */
    EXTRA,

    /** A training day in the past with no workout. The workout stays pending and slides forward. */
    MISSED,

    /** Today is a training day and the workout isn't done yet. */
    TODAY,

    /** A future training day. */
    PLANNED,
}

data class ScheduleEntry(val epochDay: Long, val status: DayStatus, val dayIndex: Int)

data class ScheduleResult(
    /** In date order. Only dates that have a workout, a miss, or a plan appear; plain rest days don't. */
    val entries: List<ScheduleEntry>,
    /** Index within the plan of the workout to do next. */
    val nextDayIndex: Int,
    /** 1 for the first week after the start date, 0 if the plan hasn't started yet. */
    val weekNumber: Int,
    val doneCount: Int,
    val missedCount: Int,
) {
    companion object {
        val Empty = ScheduleResult(emptyList(), 0, 0, 0, 0)
    }
}

/**
 * Lays a plan out on the calendar. Training weekdays are fixed, but the order of workouts follows what you
 * actually did: a missed workout stays next, so it is never skipped and later dates slide back.
 */
object Schedule {

    data class Done(val epochDay: Long, val dayIndex: Int)

    fun build(
        program: Split,
        startEpochDay: Long,
        trainingDays: Set<Int>,
        completed: List<Done>,
        todayEpochDay: Long,
        horizonDays: Int = 21,
    ): ScheduleResult {
        val size = program.dayNames.size
        val byDay = completed.filter { it.epochDay >= startEpochDay }.groupBy { it.epochDay }
        fun isTrainingDay(epochDay: Long) = LocalDate.ofEpochDay(epochDay).dayOfWeek.value in trainingDays

        val entries = mutableListOf<ScheduleEntry>()
        var next = 0
        var done = 0
        var missed = 0
        var todayPending = false

        var day = startEpochDay
        while (day <= todayEpochDay) {
            val sessions = byDay[day].orEmpty()
            val scheduled = isTrainingDay(day)
            sessions.forEach { session ->
                entries += ScheduleEntry(day, if (scheduled) DayStatus.DONE else DayStatus.EXTRA, session.dayIndex)
                next = (session.dayIndex + 1) % size
                done++
            }
            if (sessions.isEmpty() && scheduled) {
                if (day == todayEpochDay) {
                    entries += ScheduleEntry(day, DayStatus.TODAY, next)
                    todayPending = true
                } else {
                    entries += ScheduleEntry(day, DayStatus.MISSED, next)
                    missed++
                }
            }
            day++
        }

        var pointer = if (todayPending) (next + 1) % size else next
        var future = maxOf(todayEpochDay + 1, startEpochDay)
        val last = maxOf(todayEpochDay, startEpochDay) + horizonDays
        while (future <= last) {
            if (isTrainingDay(future)) {
                entries += ScheduleEntry(future, DayStatus.PLANNED, pointer)
                pointer = (pointer + 1) % size
            }
            future++
        }

        val week = if (todayEpochDay < startEpochDay) 0 else ((todayEpochDay - startEpochDay) / 7 + 1).toInt()
        return ScheduleResult(entries, next, week, done, missed)
    }
}
