package com.bharath.homeforge.data

import android.content.Context
import com.bharath.homeforge.domain.Rotation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate

data class RotationState(val enabled: Boolean, val startEpochDay: Long) {

    /** Shift applied to every exercise slot; 0 when rotation is off. */
    fun block(todayEpochDay: Long = LocalDate.now().toEpochDay()): Int =
        if (enabled) Rotation.blockIndex(startEpochDay, todayEpochDay) else 0

    fun week(todayEpochDay: Long = LocalDate.now().toEpochDay()): Int =
        Rotation.weekInBlock(startEpochDay, todayEpochDay)
}

class RotationRepository private constructor(context: Context) {

    private val prefs = context.getSharedPreferences("rotation", Context.MODE_PRIVATE)
    private val state = MutableStateFlow(load())

    val rotation: StateFlow<RotationState> = state.asStateFlow()

    fun setEnabled(enabled: Boolean) {
        state.value = state.value.copy(enabled = enabled)
        persist()
    }

    fun restart() {
        state.value = state.value.copy(startEpochDay = LocalDate.now().toEpochDay())
        persist()
    }

    private fun persist() {
        prefs.edit()
            .putBoolean(KEY_ENABLED, state.value.enabled)
            .putLong(KEY_START, state.value.startEpochDay)
            .apply()
    }

    private fun load(): RotationState {
        val start = if (prefs.contains(KEY_START)) {
            prefs.getLong(KEY_START, 0L)
        } else {
            LocalDate.now().toEpochDay().also { prefs.edit().putLong(KEY_START, it).apply() }
        }
        return RotationState(prefs.getBoolean(KEY_ENABLED, true), start)
    }

    companion object {
        private const val KEY_ENABLED = "enabled"
        private const val KEY_START = "start_epoch_day"

        @Volatile
        private var instance: RotationRepository? = null

        fun get(context: Context): RotationRepository =
            instance ?: synchronized(this) {
                instance ?: RotationRepository(context.applicationContext).also { instance = it }
            }
    }
}
