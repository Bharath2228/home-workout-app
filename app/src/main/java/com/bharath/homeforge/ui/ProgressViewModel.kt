package com.bharath.homeforge.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bharath.homeforge.data.HealthSync
import com.bharath.homeforge.data.HomeForgeDatabase
import com.bharath.homeforge.data.LoggedSet
import com.bharath.homeforge.data.Measurement
import com.bharath.homeforge.data.WorkoutSession
import com.bharath.homeforge.data.observeReadiness
import com.bharath.homeforge.domain.MeasurementType
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProgressViewModel(app: Application) : AndroidViewModel(app) {

    private val dao = HomeForgeDatabase.get(app).workoutDao()
    private val health = HealthSync(app, dao)

    val sets: StateFlow<List<LoggedSet>> = dao.observeAllSets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val sessions: StateFlow<List<WorkoutSession>> = dao.observeSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val measurements: StateFlow<List<Measurement>> = dao.observeMeasurements()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val warnings: StateFlow<List<String>> = dao.observeReadiness()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val sessionCount: StateFlow<Int> = dao.observeSessionCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    fun addMeasurement(type: MeasurementType, value: Double) {
        viewModelScope.launch {
            if (type == MeasurementType.BODY_WEIGHT) {
                // Also stores the weight for calorie estimates and sends it to Health Connect when connected.
                runCatching { health.saveBodyWeight(value) }
            } else {
                dao.insertMeasurement(Measurement(time = System.currentTimeMillis(), type = type.name, value = value))
            }
        }
    }

    fun deleteMeasurement(measurement: Measurement) {
        viewModelScope.launch { dao.deleteMeasurement(measurement) }
    }
}
