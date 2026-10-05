package com.bharath.homeforge.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bharath.homeforge.data.HomeForgeDatabase
import com.bharath.homeforge.data.UserPrefs
import com.bharath.homeforge.data.UserPrefsRepository
import com.bharath.homeforge.data.observeSchedule
import com.bharath.homeforge.domain.ScheduleResult
import com.bharath.homeforge.reminders.ReminderScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class CalendarViewModel(app: Application) : AndroidViewModel(app) {

    private val userPrefsRepo = UserPrefsRepository.get(app)

    val userPrefs: StateFlow<UserPrefs> = userPrefsRepo.userPrefs

    val schedule: StateFlow<ScheduleResult> = HomeForgeDatabase.get(app).workoutDao()
        .observeSchedule(userPrefsRepo.userPrefs)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ScheduleResult.Empty)

    fun setStartDate(epochDay: Long) {
        userPrefsRepo.update { it.copy(startEpochDay = epochDay) }
    }

    fun toggleTrainingDay(day: Int) {
        userPrefsRepo.update {
            val days = if (day in it.trainingDays) it.trainingDays - day else it.trainingDays + day
            it.copy(trainingDays = days)
        }
        ReminderScheduler.schedule(getApplication())
    }
}
