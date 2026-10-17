package com.bharath.homeforge.reminders

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.bharath.homeforge.MainActivity
import com.bharath.homeforge.R
import com.bharath.homeforge.ui.formatClock

object RestTimerNotifier {

    private const val REST_CHANNEL_ID = "rest_timer_channel"
    private const val REST_ALERT_CHANNEL_ID = "rest_timer_alert_channel"
    private const val REST_NOTIFICATION_ID = 2001
    private const val ALERT_NOTIFICATION_ID = 2002

    fun notifyRest(context: Context, remainingSeconds: Int, exerciseName: String?) {
        if (!canPostNotifications(context)) return

        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                REST_CHANNEL_ID,
                "Rest Countdown",
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = "Live countdown of your rest interval between sets"
                setShowBadge(false)
            }
            manager.createNotificationChannel(channel)
        }

        val openIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val title = "Resting: ${formatClock(remainingSeconds)}"
        val content = if (exerciseName != null) "Up next: $exerciseName" else "Prepare for your next set"

        val notification = NotificationCompat.Builder(context, REST_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(content)
            .setContentIntent(openIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        runCatching {
            NotificationManagerCompat.from(context).notify(REST_NOTIFICATION_ID, notification)
        }
    }

    fun notifyRestFinished(context: Context, nextExercise: String?) {
        cancelRest(context)
        if (!canPostNotifications(context)) return

        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val alertChannel = NotificationChannel(
                REST_ALERT_CHANNEL_ID,
                "Rest Completed Alert",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "Notification when rest interval ends"
                enableVibration(true)
            }
            manager.createNotificationChannel(alertChannel)
        }

        val openIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, REST_ALERT_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Rest Complete! 🔥")
            .setContentText(if (nextExercise != null) "Ready for $nextExercise" else "Time for your next set!")
            .setContentIntent(openIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVibrate(longArrayOf(0, 400, 200, 400))
            .build()

        runCatching {
            NotificationManagerCompat.from(context).notify(ALERT_NOTIFICATION_ID, notification)
        }
    }

    fun cancelRest(context: Context) {
        runCatching {
            NotificationManagerCompat.from(context).cancel(REST_NOTIFICATION_ID)
        }
    }

    private fun canPostNotifications(context: Context): Boolean =
        Build.VERSION.SDK_INT < 33 ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
}
