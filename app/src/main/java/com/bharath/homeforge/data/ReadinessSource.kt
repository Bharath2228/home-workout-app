package com.bharath.homeforge.data

import com.bharath.homeforge.domain.DayLoad
import com.bharath.homeforge.domain.Readiness
import com.bharath.homeforge.domain.Split
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

fun WorkoutDao.observeReadiness(): Flow<List<String>> =
    combine(observeSessions(), observeAllSets()) { sessions, sets ->
        val setCounts = sets.groupingBy { it.sessionId }.eachCount()
        // The optional core day is an extra, so it doesn't count toward rest-day or volume warnings.
        val days = sessions.filter { it.splitName != Split.CORE_DAY.name }.map {
            val day = Instant.ofEpochMilli(it.startedAt).atZone(ZoneId.systemDefault()).toLocalDate().toEpochDay()
            DayLoad(day, setCounts[it.id] ?: 0)
        }
        Readiness.assess(days, LocalDate.now().toEpochDay())
    }
