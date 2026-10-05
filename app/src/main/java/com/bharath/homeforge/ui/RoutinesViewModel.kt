package com.bharath.homeforge.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.bharath.homeforge.data.EquipmentRepository
import com.bharath.homeforge.data.RotationRepository
import com.bharath.homeforge.data.RotationState
import com.bharath.homeforge.data.SwapRepository
import com.bharath.homeforge.data.UserPrefs
import com.bharath.homeforge.data.UserPrefsRepository
import com.bharath.homeforge.domain.Equipment
import com.bharath.homeforge.domain.Split
import kotlinx.coroutines.flow.StateFlow

class RoutinesViewModel(app: Application) : AndroidViewModel(app) {

    private val swapRepo = SwapRepository.get(app)

    val equipment: StateFlow<Equipment> = EquipmentRepository.get(app).equipment
    val swaps: StateFlow<Map<String, Int>> = swapRepo.swaps
    val rotation: StateFlow<RotationState> = RotationRepository.get(app).rotation
    val userPrefs: StateFlow<UserPrefs> = UserPrefsRepository.get(app).userPrefs

    fun swap(split: Split, dayIndex: Int, slotIndex: Int) = swapRepo.swap(split, dayIndex, slotIndex)
}
