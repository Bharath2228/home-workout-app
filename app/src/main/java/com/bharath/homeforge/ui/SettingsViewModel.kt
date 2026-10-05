package com.bharath.homeforge.ui

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.health.connect.client.HealthConnectClient
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bharath.homeforge.data.Backup
import com.bharath.homeforge.data.BackupCodec
import com.bharath.homeforge.data.EquipmentRepository
import com.bharath.homeforge.data.HealthSync
import com.bharath.homeforge.data.HomeForgeDatabase
import com.bharath.homeforge.data.RotationRepository
import com.bharath.homeforge.data.RotationState
import com.bharath.homeforge.data.UserPrefs
import com.bharath.homeforge.data.UserPrefsRepository
import com.bharath.homeforge.data.observeLevelStatus
import com.bharath.homeforge.domain.Equipment
import com.bharath.homeforge.domain.Goal
import com.bharath.homeforge.domain.Level
import com.bharath.homeforge.domain.LevelProgress
import com.bharath.homeforge.domain.LevelStatus
import com.bharath.homeforge.reminders.ReminderScheduler
import com.bharath.homeforge.domain.Plate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsViewModel(app: Application) : AndroidViewModel(app) {

    private val dao = HomeForgeDatabase.get(app).workoutDao()
    private val health = HealthSync(app, dao)
    private val equipmentRepo = EquipmentRepository.get(app)

    var backupMessage by mutableStateOf<String?>(null)
        private set

    fun exportBackup(uri: Uri) {
        viewModelScope.launch {
            backupMessage = runCatching {
                val equipment = equipmentRepo.equipment.value
                val backup = Backup(
                    exportedAt = System.currentTimeMillis(),
                    sessions = dao.allSessions(),
                    sets = dao.allSetsOnce(),
                    measurements = dao.allMeasurementsOnce(),
                    plates = equipment.plates,
                    rodWeightKg = equipment.rodWeightKg,
                )
                withContext(Dispatchers.IO) {
                    val stream = getApplication<Application>().contentResolver.openOutputStream(uri, "wt")
                        ?: error("Could not open the file.")
                    stream.use { it.write(BackupCodec.encode(backup).toByteArray()) }
                }
                "Backup saved: ${backup.sessions.size} workouts, ${backup.measurements.size} measurements."
            }.getOrElse { "Export failed: ${it.message}" }
        }
    }

    fun importBackup(uri: Uri) {
        viewModelScope.launch {
            backupMessage = runCatching {
                val text = withContext(Dispatchers.IO) {
                    getApplication<Application>().contentResolver.openInputStream(uri)
                        ?.bufferedReader()?.use { it.readText() }
                        ?: error("Could not open the file.")
                }
                val backup = BackupCodec.decode(text)
                dao.replaceAll(backup.sessions, backup.sets, backup.measurements)
                equipmentRepo.save(Equipment(backup.plates, backup.rodWeightKg))
                rodText = formatKg(backup.rodWeightKg)
                "Restored ${backup.sessions.size} workouts. Use \"Sync past workouts\" to resend them to Health Connect."
            }.getOrElse { "Import failed: ${it.message}" }
        }
    }

    val permissions: Set<String> = health.manager.permissions
    val equipment: StateFlow<Equipment> = equipmentRepo.equipment

    private val userPrefsRepo = UserPrefsRepository.get(app)
    val userPrefs: StateFlow<UserPrefs> = userPrefsRepo.userPrefs

    val levelStatus: StateFlow<LevelStatus> = dao
        .observeLevelStatus(userPrefsRepo.userPrefs)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LevelStatus.Initial)

    fun setLevel(level: Level) {
        userPrefsRepo.update { it.copy(level = level) }
        announceCurrentLevel()
    }

    fun setAutoLevel(enabled: Boolean) {
        userPrefsRepo.update { it.copy(autoLevel = enabled) }
        announceCurrentLevel()
    }

    fun setNoEquipment(value: Boolean) = userPrefsRepo.update { it.copy(noEquipment = value) }

    /** Changing the level by hand shouldn't trigger the "you reached a new level" message. */
    private fun announceCurrentLevel() {
        val prefs = userPrefsRepo.userPrefs.value
        val current = LevelProgress.status(prefs.level, prefs.autoLevel, levelStatus.value.workouts).level
        userPrefsRepo.update { it.copy(lastAnnouncedLevel = current) }
    }

    var reminderMessage by mutableStateOf<String?>(null)
        private set

    fun setGoal(goal: Goal) = userPrefsRepo.update { it.copy(goal = goal) }

    fun setRestTimerEnabled(enabled: Boolean) = userPrefsRepo.update { it.copy(restTimerEnabled = enabled) }

    fun setRemindersEnabled(enabled: Boolean) {
        reminderMessage = null
        userPrefsRepo.update { it.copy(remindersEnabled = enabled) }
        ReminderScheduler.schedule(getApplication())
    }

    fun onNotificationPermissionDenied() {
        reminderMessage = "Notifications are blocked. Allow them in the phone's app settings to get reminders."
    }

    fun setReminderTime(hour: Int, minute: Int) {
        userPrefsRepo.update { it.copy(reminderHour = hour, reminderMinute = minute) }
        ReminderScheduler.schedule(getApplication())
    }

    private val rotationRepo = RotationRepository.get(app)
    val rotation: StateFlow<RotationState> = rotationRepo.rotation

    fun setRotationEnabled(enabled: Boolean) = rotationRepo.setEnabled(enabled)

    fun restartRotation() = rotationRepo.restart()

    var sdkStatus by mutableIntStateOf(health.manager.sdkStatus())
        private set

    var granted by mutableStateOf(false)
        private set

    var syncEnabled by mutableStateOf(health.enabled)
        private set

    var info by mutableStateOf<String?>(null)
        private set

    var bodyWeightText by mutableStateOf(health.bodyWeightKg?.let(::formatKg).orEmpty())
        private set

    var newPlateText by mutableStateOf("")
        private set

    var rodText by mutableStateOf(formatKg(equipmentRepo.equipment.value.rodWeightKg))
        private set

    var equipmentMessage by mutableStateOf<String?>(null)
        private set

    val available: Boolean get() = sdkStatus == HealthConnectClient.SDK_AVAILABLE

    fun onBodyWeightChange(value: String) {
        bodyWeightText = value
    }

    fun saveBodyWeight() {
        val kg = bodyWeightText.replace(',', '.').toDoubleOrNull()
        if (kg == null || kg < 20.0 || kg > 300.0) {
            info = "Enter a body weight between 20 and 300 kg."
            return
        }
        viewModelScope.launch {
            info = runCatching {
                if (health.saveBodyWeight(kg)) "Body weight saved and sent to Health Connect."
                else "Body weight saved. Connect Health Connect to send it there."
            }.getOrElse { "Saved locally, Health Connect write failed: ${it.message}" }
        }
    }

    fun onNewPlateChange(value: String) {
        newPlateText = value
    }

    fun onRodChange(value: String) {
        rodText = value
        val kg = value.replace(',', '.').toDoubleOrNull() ?: return
        if (kg in 0.0..30.0) equipmentRepo.save(equipment.value.copy(rodWeightKg = kg))
    }

    fun changePlateCount(weightKg: Double, delta: Int) {
        val current = equipment.value
        val plates = current.plates
            .map { if (it.weightKg == weightKg) it.copy(count = (it.count + delta).coerceAtMost(MAX_PLATE_COUNT)) else it }
            .filter { it.count > 0 }
        equipmentRepo.save(current.copy(plates = plates))
    }

    fun removePlate(weightKg: Double) {
        val current = equipment.value
        equipmentRepo.save(current.copy(plates = current.plates.filter { it.weightKg != weightKg }))
    }

    fun addPlate() {
        val kg = newPlateText.replace(',', '.').toDoubleOrNull()
        if (kg == null || kg < 0.25 || kg > 50.0) {
            equipmentMessage = "Enter a plate weight between 0.25 and 50 kg."
            return
        }
        val current = equipment.value
        val plates = if (current.plates.any { it.weightKg == kg }) {
            current.plates.map { if (it.weightKg == kg) it.copy(count = (it.count + 1).coerceAtMost(MAX_PLATE_COUNT)) else it }
        } else {
            (current.plates + Plate(kg, 2)).sortedByDescending { it.weightKg }
        }
        equipmentRepo.save(current.copy(plates = plates))
        newPlateText = ""
        equipmentMessage = null
    }

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

    private companion object {
        const val MAX_PLATE_COUNT = 40
    }
}
