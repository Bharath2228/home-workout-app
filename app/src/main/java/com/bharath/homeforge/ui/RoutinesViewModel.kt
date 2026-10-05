package com.bharath.homeforge.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bharath.homeforge.data.EquipmentRepository
import com.bharath.homeforge.data.HomeForgeDatabase
import com.bharath.homeforge.data.RotationRepository
import com.bharath.homeforge.data.RotationState
import com.bharath.homeforge.data.SwapRepository
import com.bharath.homeforge.data.UserPrefs
import com.bharath.homeforge.data.UserPrefsRepository
import com.bharath.homeforge.data.observeLevelStatus
import com.bharath.homeforge.data.observeSchedule
import com.bharath.homeforge.domain.Equipment
import com.bharath.homeforge.domain.Level
import com.bharath.homeforge.domain.LevelStatus
import com.bharath.homeforge.domain.ScheduleResult
import com.bharath.homeforge.domain.Split
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class RoutinesViewModel(app: Application) : AndroidViewModel(app) {

    private val swapRepo = SwapRepository.get(app)
    private val userPrefsRepo = UserPrefsRepository.get(app)
    private val dao = HomeForgeDatabase.get(app).workoutDao()

    val equipment: StateFlow<Equipment> = EquipmentRepository.get(app).equipment
    val swaps: StateFlow<Map<String, Int>> = swapRepo.swaps
    val rotation: StateFlow<RotationState> = RotationRepository.get(app).rotation
    val userPrefs: StateFlow<UserPrefs> = userPrefsRepo.userPrefs

    val schedule: StateFlow<ScheduleResult> = dao
        .observeSchedule(userPrefsRepo.userPrefs)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ScheduleResult.Empty)

    val levelStatus: StateFlow<LevelStatus> = dao
        .observeLevelStatus(userPrefsRepo.userPrefs)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LevelStatus.Initial)

    fun setNoEquipment(value: Boolean) = userPrefsRepo.update { it.copy(noEquipment = value) }

    fun acknowledgeLevel(level: Level) = userPrefsRepo.update { it.copy(lastAnnouncedLevel = level) }

    fun swap(split: Split, dayIndex: Int, slotIndex: Int) = swapRepo.swap(split, dayIndex, slotIndex)

    fun setProgram(program: Split) = userPrefsRepo.choosePlan(program)
}
