package com.bharath.homeforge.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.bharath.homeforge.data.UserPrefsRepository
import com.bharath.homeforge.domain.ReminderTime
import java.time.LocalDateTime
import java.time.ZoneId

object ReminderScheduler {

    /** Replaces any pending alarm with one for the next training-day reminder, if reminders are on. */
    fun schedule(context: Context) {
        val app = context.applicationContext
        val alarmManager = app.getSystemService(AlarmManager::class.java) ?: return
        val pending = pendingIntent(app)
        alarmManager.cancel(pending)

        val prefs = UserPrefsRepository.get(app).userPrefs.value
        if (!prefs.remindersEnabled) return
        val next = ReminderTime.next(prefs.trainingDays, prefs.reminderHour, prefs.reminderMinute, LocalDateTime.now())
            ?: return
        val millis = next.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        // Inexact on purpose: exact alarms need a special permission, and a few minutes of drift is fine here.
        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, pending)
    }

    private fun pendingIntent(context: Context): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            0,
            Intent(context, ReminderReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
}
