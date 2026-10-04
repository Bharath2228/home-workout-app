package com.bharath.homeforge.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.health.connect.client.HealthConnectClient
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bharath.homeforge.data.HealthSync
import com.bharath.homeforge.data.HomeForgeDatabase
import kotlinx.coroutines.launch

class SettingsViewModel(app: Application) : AndroidViewModel(app) {

    private val health = HealthSync(app, HomeForgeDatabase.get(app).workoutDao())

    val permissions: Set<String> = health.manager.permissions

    var sdkStatus by mutableIntStateOf(health.manager.sdkStatus())
        private set

    var granted by mutableStateOf(false)
        private set

    var syncEnabled by mutableStateOf(health.enabled)
        private set

    var info by mutableStateOf<String?>(null)
        private set

    val available: Boolean get() = sdkStatus == HealthConnectClient.SDK_AVAILABLE

    fun refresh() {
        sdkStatus = health.manager.sdkStatus()
        viewModelScope.launch {
            granted = runCatching { health.manager.hasAllPermissions() }.getOrDefault(false)
            if (!granted && syncEnabled) updateSyncEnabled(false)
        }
    }

    fun updateSyncEnabled(value: Boolean) {
        syncEnabled = value
        health.enabled = value
    }

    fun syncPast() {
        viewModelScope.launch {
            info = "Syncing..."
            info = runCatching {
                val sent = health.syncAll()
                val count = health.manager.countFromThisAppLast30Days()
                "Sent $sent workouts. Health Connect shows $count from HomeForge in the last 30 days."
            }.getOrElse { "Sync failed: ${it.message}" }
        }
    }
}
