package com.bharath.homeforge.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ScheduleTest {

    // Epoch day 4 is Monday 1970-01-05, so 6 is Wednesday, 8 is Friday, 11 is the next Monday.
    private val monWedFri = setOf(1, 3, 5)
    private val program = Split.FULL_BODY

    private fun build(start: Long, today: Long, done: List<Schedule.Done> = emptyList(), days: Set<Int> = monWedFri) =
        Schedule.build(program, start, days, done, today)

    private fun ScheduleResult.at(day: Long) = entries.filter { it.epochDay == day }

    @Test
    fun freshStartOnATrainingDayIsTodayThenPlanned() {
        val result = build(start = 4, today = 4)
        assertEquals(ScheduleEntry(4, DayStatus.TODAY, 0), result.entries[0])
        assertEquals(ScheduleEntry(6, DayStatus.PLANNED, 1), result.entries[1])
        assertEquals(ScheduleEntry(8, DayStatus.PLANNED, 2), result.entries[2])
        assertEquals(ScheduleEntry(11, DayStatus.PLANNED, 0), result.entries[3])
        assertEquals(0, result.nextDayIndex)
        assertEquals(1, result.weekNumber)
    }

    @Test
    fun finishingTodaysWorkoutAdvancesThePlan() {
        val result = build(4, 4, listOf(Schedule.Done(4, 0)))
        assertEquals(listOf(ScheduleEntry(4, DayStatus.DONE, 0)), result.at(4))
        assertEquals(ScheduleEntry(6, DayStatus.PLANNED, 1), result.entries[1])
        assertEquals(1, result.nextDayIndex)
        assertEquals(1, result.doneCount)
    }

    @Test
    fun aMissedWorkoutStaysNextAndLaterDatesShift() {
        // Did Day 1 on Monday, skipped Wednesday, and it is now Friday.
        val result = build(4, 8, listOf(Schedule.Done(4, 0)))
        assertEquals(ScheduleEntry(6, DayStatus.MISSED, 1), result.at(6).single())
        assertEquals(ScheduleEntry(8, DayStatus.TODAY, 1), result.at(8).single())
        assertEquals(ScheduleEntry(11, DayStatus.PLANNED, 2), result.at(11).single())
        assertEquals(1, result.missedCount)
        assertEquals(1, result.nextDayIndex)
    }

    @Test
    fun aWorkoutOnARestDayCountsAsExtraAndAdvancesThePlan() {
        // Day 1 on Monday, Day 2 on Tuesday (not a training day), and it is now Wednesday.
        val result = build(4, 6, listOf(Schedule.Done(4, 0), Schedule.Done(5, 1)))
        assertEquals(ScheduleEntry(5, DayStatus.EXTRA, 1), result.at(5).single())
        assertEquals(ScheduleEntry(6, DayStatus.TODAY, 2), result.at(6).single())
        assertEquals(0, result.missedCount)
    }

    @Test
    fun aPlanThatStartsInTheFutureHasNoHistory() {
        val result = build(start = 11, today = 4)
        assertEquals(ScheduleEntry(11, DayStatus.PLANNED, 0), result.entries.first())
        assertEquals(0, result.weekNumber)
        assertEquals(0, result.doneCount + result.missedCount)
    }

    @Test
    fun weekNumberCountsFromTheStartDate() {
        assertEquals(1, build(4, 10).weekNumber)
        assertEquals(2, build(4, 11).weekNumber)
        assertEquals(3, build(4, 18).weekNumber)
    }

    @Test
    fun noTrainingDaysMeansNoPlannedDates() {
        val result = build(4, 8, days = emptySet())
        assertTrue(result.entries.isEmpty())
    }

    @Test
    fun theWorkoutOrderWrapsAroundThePlan() {
        val result = build(4, 4)
        assertEquals(listOf(0, 1, 2, 0, 1, 2), result.entries.take(6).map { it.dayIndex })
    }

    @Test
    fun workoutsBeforeTheStartDateAreIgnored() {
        val result = build(8, 8, listOf(Schedule.Done(4, 2)))
        assertEquals(0, result.nextDayIndex)
        assertEquals(0, result.doneCount)
    }
}
