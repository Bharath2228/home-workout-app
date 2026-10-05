package com.bharath.homeforge.domain

import java.time.LocalDateTime

object ReminderTime {

    /** Next time after [now] that falls on one of [days] (1 = Monday ... 7 = Sunday) at [hour]:[minute]. */
    fun next(days: Set<Int>, hour: Int, minute: Int, now: LocalDateTime): LocalDateTime? {
        if (days.isEmpty()) return null
        for (offset in 0L..7L) {
            val candidate = now.toLocalDate().plusDays(offset).atTime(hour, minute)
            if (candidate.dayOfWeek.value in days && candidate.isAfter(now)) return candidate
        }
        return null
    }
}
