package com.bharath.homeforge.ui

import kotlin.math.roundToInt

/** A running clock such as "12:34", or "1:05:09" once it passes an hour. */
fun formatDuration(totalSeconds: Long): String {
    val seconds = totalSeconds.coerceAtLeast(0)
    val hours = seconds / 3600
    val minutes = seconds % 3600 / 60
    val rest = seconds % 60
    return if (hours > 0) "%d:%02d:%02d".format(hours, minutes, rest) else "%d:%02d".format(minutes, rest)
}

/** How long a finished workout took, in plain words, such as "45 min" or "1 h 05 min". */
fun durationText(millis: Long): String {
    val minutes = (millis.coerceAtLeast(0) / 60_000.0).roundToInt()
    return when {
        minutes < 1 -> "under 1 min"
        minutes < 60 -> "$minutes min"
        else -> "%d h %02d min".format(minutes / 60, minutes % 60)
    }
}
