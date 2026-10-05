package com.bharath.homeforge.data

import com.bharath.homeforge.domain.Schedule
import com.bharath.homeforge.domain.ScheduleResult
import com.bharath.homeforge.domain.Split
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

private data class PlanKey(val program: Split, val startEpochDay: Long, val trainingDays: Set<Int>)

/** The calendar for the chosen plan: scheduled dates, what was done or missed, and which workout is next. */
@OptIn(ExperimentalCoroutinesApi::class)
fun WorkoutDao.observeSchedule(prefs: Flow<UserPrefs>): Flow<ScheduleResult> =
    prefs.map { PlanKey(it.program, it.startEpochDay, it.trainingDays) }
        .distinctUntilChanged()
        .flatMapLatest { key ->
            val zone = ZoneId.systemDefault()
            val sinceMillis = LocalDate.ofEpochDay(key.startEpochDay).atStartOfDay(zone).toInstant().toEpochMilli()
            observeProgramSessions(key.program.name, sinceMillis).map { sessions ->
                val completed = sessions.map {
                    Schedule.Done(Instant.ofEpochMilli(it.startedAt).atZone(zone).toLocalDate().toEpochDay(), it.dayIndex)
                }
                Schedule.build(key.program, key.startEpochDay, key.trainingDays, completed, LocalDate.now().toEpochDay())
            }
        }
