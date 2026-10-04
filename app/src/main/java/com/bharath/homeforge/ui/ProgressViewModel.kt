package com.bharath.homeforge.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bharath.homeforge.data.HomeForgeDatabase
import com.bharath.homeforge.data.LoggedSet
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class ProgressViewModel(app: Application) : AndroidViewModel(app) {

    private val dao = HomeForgeDatabase.get(app).workoutDao()

    val sets: StateFlow<List<LoggedSet>> = dao.observeAllSets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val sessionCount: StateFlow<Int> = dao.observeSessionCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)
}
