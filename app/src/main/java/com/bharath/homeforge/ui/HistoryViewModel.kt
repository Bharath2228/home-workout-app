package com.bharath.homeforge.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bharath.homeforge.data.HealthSync
import com.bharath.homeforge.data.HomeForgeDatabase
import com.bharath.homeforge.data.LoggedSet
import com.bharath.homeforge.data.WorkoutSession
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel(app: Application) : AndroidViewModel(app) {

    private val dao = HomeForgeDatabase.get(app).workoutDao()
    private val health = HealthSync(app, dao)

    val sessions: StateFlow<List<WorkoutSession>> = dao.observeSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val sets: StateFlow<List<LoggedSet>> = dao.observeAllSets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun deleteSession(sessionId: Long) {
        viewModelScope.launch {
            dao.deleteSession(sessionId)
            runCatching { health.deleteSession(sessionId) }
        }
    }

    fun updateSet(set: LoggedSet, weightKg: Double?, reps: Int) {
        viewModelScope.launch {
            dao.updateSet(set.copy(weightKg = weightKg, reps = reps))
            resync(set.sessionId)
        }
    }

    fun deleteSet(set: LoggedSet) {
        viewModelScope.launch {
            dao.deleteSet(set)
            if (dao.setCount(set.sessionId) == 0) {
                dao.deleteSession(set.sessionId)
                runCatching { health.deleteSession(set.sessionId) }
            } else {
                resync(set.sessionId)
            }
        }
    }

    private suspend fun resync(sessionId: Long) {
        runCatching { health.syncSessionIfEnabled(sessionId) }
    }
}
