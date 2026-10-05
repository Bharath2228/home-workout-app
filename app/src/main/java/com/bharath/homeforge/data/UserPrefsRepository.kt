package com.bharath.homeforge.data

import android.content.Context
import com.bharath.homeforge.domain.Goal
import com.bharath.homeforge.domain.Level
import com.bharath.homeforge.domain.PlanHelper
import com.bharath.homeforge.domain.Split
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate

/** [trainingDays] use 1 = Monday ... 7 = Sunday. [startEpochDay] is when the current plan began. */
data class UserPrefs(
    val program: Split = Split.FULL_BODY,
    val startEpochDay: Long = LocalDate.now().toEpochDay(),
    val trainingDays: Set<Int> = PlanHelper.defaultTrainingDays(Split.FULL_BODY),
    val goal: Goal = Goal.MUSCLE_GAIN,
    /** The level the user chose. The level in use can be higher once automatic level-ups kick in. */
    val level: Level = Level.BEGINNER,
    val autoLevel: Boolean = true,
    val lastAnnouncedLevel: Level = Level.BEGINNER,
    val noEquipment: Boolean = false,
    val restTimerEnabled: Boolean = true,
    val remindersEnabled: Boolean = false,
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
            .putString(KEY_PROGRAM, updated.program.name)
            .putLong(KEY_START, updated.startEpochDay)
            .putString(KEY_DAYS, updated.trainingDays.sorted().joinToString(","))
            .putString(KEY_GOAL, updated.goal.name)
            .putString(KEY_LEVEL, updated.level.name)
            .putBoolean(KEY_AUTO_LEVEL, updated.autoLevel)
            .putString(KEY_LAST_LEVEL, updated.lastAnnouncedLevel.name)
            .putBoolean(KEY_NO_EQUIPMENT, updated.noEquipment)
            .putBoolean(KEY_REST, updated.restTimerEnabled)
            .putBoolean(KEY_REMINDERS, updated.remindersEnabled)
            .putInt(KEY_HOUR, updated.reminderHour)
            .putInt(KEY_MINUTE, updated.reminderMinute)
            .apply()
    }

    /** Starts a new plan today, with that plan's usual training weekdays. */
    fun choosePlan(program: Split) = update {
        it.copy(
            program = program,
            startEpochDay = LocalDate.now().toEpochDay(),
            trainingDays = PlanHelper.defaultTrainingDays(program),
        )
    }

    private fun load(): UserPrefs {
        val defaults = UserPrefs()
        val program = runCatching { Split.valueOf(store.getString(KEY_PROGRAM, null).orEmpty()) }
            .getOrDefault(defaults.program)
            .takeIf { it in Split.programs } ?: defaults.program
        val start = if (store.contains(KEY_START)) {
            store.getLong(KEY_START, defaults.startEpochDay)
        } else {
            defaults.startEpochDay.also { store.edit().putLong(KEY_START, it).apply() }
        }
        // Reminders used to have their own weekday list; fall back to it so an existing choice isn't lost.
        val days = (store.getString(KEY_DAYS, null) ?: store.getString(KEY_OLD_REMINDER_DAYS, null))
            ?.split(',')?.mapNotNull { it.toIntOrNull() }?.filter { it in 1..7 }?.toSet()
            ?: PlanHelper.defaultTrainingDays(program)
        return UserPrefs(
            program = program,
            startEpochDay = start,
            trainingDays = days,
            goal = runCatching { Goal.valueOf(store.getString(KEY_GOAL, null).orEmpty()) }.getOrDefault(defaults.goal),
            level = runCatching { Level.valueOf(store.getString(KEY_LEVEL, null).orEmpty()) }.getOrDefault(defaults.level),
            autoLevel = store.getBoolean(KEY_AUTO_LEVEL, defaults.autoLevel),
            lastAnnouncedLevel = runCatching { Level.valueOf(store.getString(KEY_LAST_LEVEL, null).orEmpty()) }
                .getOrDefault(defaults.lastAnnouncedLevel),
            noEquipment = store.getBoolean(KEY_NO_EQUIPMENT, defaults.noEquipment),
            restTimerEnabled = store.getBoolean(KEY_REST, defaults.restTimerEnabled),
            remindersEnabled = store.getBoolean(KEY_REMINDERS, defaults.remindersEnabled),
            reminderHour = store.getInt(KEY_HOUR, defaults.reminderHour),
            reminderMinute = store.getInt(KEY_MINUTE, defaults.reminderMinute),
        )
    }

    companion object {
        private const val KEY_PROGRAM = "program"
        private const val KEY_START = "plan_start_epoch_day"
        private const val KEY_DAYS = "training_days"
        private const val KEY_OLD_REMINDER_DAYS = "reminder_days"
        private const val KEY_GOAL = "goal"
        private const val KEY_LEVEL = "level"
        private const val KEY_AUTO_LEVEL = "auto_level"
        private const val KEY_LAST_LEVEL = "last_announced_level"
        private const val KEY_NO_EQUIPMENT = "no_equipment"
        private const val KEY_REST = "rest_timer"
        private const val KEY_REMINDERS = "reminders"
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
