package com.bharath.homeforge.data

import android.content.Context
import com.bharath.homeforge.domain.Goal
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** [reminderDays] use 1 = Monday ... 7 = Sunday. */
data class UserPrefs(
    val goal: Goal = Goal.MUSCLE_GAIN,
    val restTimerEnabled: Boolean = true,
    val remindersEnabled: Boolean = false,
    val reminderDays: Set<Int> = setOf(1, 3, 5),
    val reminderHour: Int = 18,
    val reminderMinute: Int = 0,
)

class UserPrefsRepository private constructor(context: Context) {

    private val store = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    private val state = MutableStateFlow(load())

    val userPrefs: StateFlow<UserPrefs> = state.asStateFlow()

    fun update(transform: (UserPrefs) -> UserPrefs) {
        val updated = transform(state.value)
        state.value = updated
        store.edit()
            .putString(KEY_GOAL, updated.goal.name)
            .putBoolean(KEY_REST, updated.restTimerEnabled)
            .putBoolean(KEY_REMINDERS, updated.remindersEnabled)
            .putString(KEY_DAYS, updated.reminderDays.sorted().joinToString(","))
            .putInt(KEY_HOUR, updated.reminderHour)
            .putInt(KEY_MINUTE, updated.reminderMinute)
            .apply()
    }

    private fun load(): UserPrefs {
        val defaults = UserPrefs()
        return UserPrefs(
            goal = runCatching { Goal.valueOf(store.getString(KEY_GOAL, null).orEmpty()) }.getOrDefault(defaults.goal),
            restTimerEnabled = store.getBoolean(KEY_REST, defaults.restTimerEnabled),
            remindersEnabled = store.getBoolean(KEY_REMINDERS, defaults.remindersEnabled),
            reminderDays = store.getString(KEY_DAYS, null)
                ?.split(',')?.mapNotNull { it.toIntOrNull() }?.filter { it in 1..7 }?.toSet()
                ?: defaults.reminderDays,
            reminderHour = store.getInt(KEY_HOUR, defaults.reminderHour),
            reminderMinute = store.getInt(KEY_MINUTE, defaults.reminderMinute),
        )
    }

    companion object {
        private const val KEY_GOAL = "goal"
        private const val KEY_REST = "rest_timer"
        private const val KEY_REMINDERS = "reminders"
        private const val KEY_DAYS = "reminder_days"
        private const val KEY_HOUR = "reminder_hour"
        private const val KEY_MINUTE = "reminder_minute"

        @Volatile
        private var instance: UserPrefsRepository? = null

        fun get(context: Context): UserPrefsRepository =
            instance ?: synchronized(this) {
                instance ?: UserPrefsRepository(context.applicationContext).also { instance = it }
            }
    }
}
