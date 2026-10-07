package com.bharath.homeforge.ui.screens

import android.Manifest
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bharath.homeforge.data.HealthConnectManager
import com.bharath.homeforge.domain.Goal
import com.bharath.homeforge.domain.Level
import com.bharath.homeforge.domain.LevelProgress
import com.bharath.homeforge.domain.LoadCalculator
import com.bharath.homeforge.domain.Rig
import com.bharath.homeforge.domain.Rotation
import com.bharath.homeforge.ui.SettingsViewModel
import com.bharath.homeforge.ui.formatKg
import com.bharath.homeforge.ui.theme.Plate
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun SettingsScreen(onOpenCredits: () -> Unit, vm: SettingsViewModel = viewModel()) {
    val equipment by vm.equipment.collectAsStateWithLifecycle()
    val rotation by vm.rotation.collectAsStateWithLifecycle()
    val userPrefs by vm.userPrefs.collectAsStateWithLifecycle()
    val levelStatus by vm.levelStatus.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val notificationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) vm.setRemindersEnabled(true) else vm.onNotificationPermissionDenied()
    }
    var pendingImport by remember { mutableStateOf<Uri?>(null) }
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) vm.exportBackup(uri)
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        pendingImport = uri
    }

    pendingImport?.let { uri ->
        AlertDialog(
            onDismissRequest = { pendingImport = null },
            title = { Text("Replace your data?") },
            text = { Text("Importing replaces your current workouts, measurements and equipment with the backup.") },
            confirmButton = {
                TextButton(onClick = {
                    vm.importBackup(uri)
                    pendingImport = null
                }) { Text("Replace") }
            },
            dismissButton = { TextButton(onClick = { pendingImport = null }) { Text("Cancel") } },
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        PermissionController.createRequestPermissionResultContract(),
    ) { vm.refresh() }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { vm.refresh() }

    LazyColumn(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { Text("Settings", style = MaterialTheme.typography.headlineMedium) }

        item {
            Plate(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Health Connect", style = MaterialTheme.typography.titleMedium)
                    when {
                        vm.sdkStatus == HealthConnectClient.SDK_UNAVAILABLE ->
                            Text("Health Connect is not supported on this device.")

                        vm.sdkStatus == HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> {
                            Text("Install or update the Health Connect app to sync workouts.")
                            Button(onClick = { openHealthConnectInStore(context) }) { Text("Open Play Store") }
                        }

                        !vm.granted -> {
                            Text("Allow HomeForge to write your workouts to Health Connect.")
                            Button(onClick = { permissionLauncher.launch(vm.permissions) }) { Text("Connect") }
                        }

                        else -> {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Text("Sync new workouts", modifier = Modifier.weight(1f))
                                Switch(checked = vm.syncEnabled, onCheckedChange = vm::updateSyncEnabled)
                            }
                            OutlinedButton(onClick = vm::syncPast) { Text("Sync past workouts") }
                        }
                    }
                    if (vm.info != null) Text(vm.info.orEmpty(), style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        item {
            Plate(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Body weight", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Used to estimate calories burned. The estimate is rough, not a measurement.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = vm.bodyWeightText,
                            onValueChange = vm::onBodyWeightChange,
                            label = { Text("kg") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                        )
                        Button(onClick = vm::saveBodyWeight) { Text("Save") }
                    }
                }
            }
        }

        item {
            Plate(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("About the pictures", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Exercise pictures come from the wger.de exercise database under Creative Commons licenses.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    OutlinedButton(onClick = onOpenCredits) { Text("Picture credits") }
                }
            }
        }

        item {
            Plate(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Backup", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Your workouts, measurements and equipment live only on this phone. " +
                            "Save a backup file somewhere safe, such as Drive.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { exportLauncher.launch("homeforge-backup-${LocalDate.now()}.json") }) {
                            Text("Export")
                        }
                        OutlinedButton(onClick = { importLauncher.launch(arrayOf("*/*")) }) { Text("Import") }
                    }
                    if (vm.backupMessage != null) Text(vm.backupMessage.orEmpty(), style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        item {
            Plate(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Level and equipment", style = MaterialTheme.typography.titleMedium)
                    Text(levelText(levelStatus), style = MaterialTheme.typography.bodyMedium)
                    Text(levelStatus.level.description, style = MaterialTheme.typography.bodySmall)
                    Text("Your starting level", style = MaterialTheme.typography.labelLarge)
                    Row(
                        Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Level.entries.forEach { option ->
                            FilterChip(
                                selected = option == userPrefs.level,
                                onClick = { vm.setLevel(option) },
                                label = { Text(option.label) },
                            )
                        }
                    }
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Level up automatically: ${LevelProgress.INTERMEDIATE_AT} workouts to Intermediate, " +
                                "${LevelProgress.ADVANCED_AT} to Advanced",
                            modifier = Modifier.weight(1f),
                        )
                        Switch(checked = userPrefs.autoLevel, onCheckedChange = vm::setAutoLevel)
                    }
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("No equipment (bodyweight exercises only)", modifier = Modifier.weight(1f))
                        Switch(checked = userPrefs.noEquipment, onCheckedChange = vm::setNoEquipment)
                    }
                }
            }
        }

        item {
            Plate(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Training goal", style = MaterialTheme.typography.titleMedium)
                    Row(
                        Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Goal.entries.forEach { goal ->
                            FilterChip(
                                selected = goal == userPrefs.goal,
                                onClick = { vm.setGoal(goal) },
                                label = { Text(goal.label) },
                            )
                        }
                    }
                    Text(userPrefs.goal.description, style = MaterialTheme.typography.bodySmall)
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("Rest timer after each set", modifier = Modifier.weight(1f))
                        Switch(checked = userPrefs.restTimerEnabled, onCheckedChange = vm::setRestTimerEnabled)
                    }
                }
            }
        }

        item {
            Plate(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Reminders", style = MaterialTheme.typography.titleMedium)
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("Remind me on training days", modifier = Modifier.weight(1f))
                        Switch(
                            checked = userPrefs.remindersEnabled,
                            onCheckedChange = { enable ->
                                if (enable && needsNotificationPermission(context)) {
                                    notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    vm.setRemindersEnabled(enable)
                                }
                            },
                        )
                    }
                    if (userPrefs.remindersEnabled) {
                        Text(
                            "Reminders come on your training days. Change those days on the Calendar.",
                            style = MaterialTheme.typography.bodySmall,
                        )
                        OutlinedButton(onClick = {
                            TimePickerDialog(
                                context,
                                { _, hour, minute -> vm.setReminderTime(hour, minute) },
                                userPrefs.reminderHour,
                                userPrefs.reminderMinute,
                                true,
                            ).show()
                        }) {
                            Text("Time: %02d:%02d".format(userPrefs.reminderHour, userPrefs.reminderMinute))
                        }
                        Text(
                            "Reminders can arrive a few minutes late, and they don't check whether you already trained.",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    if (vm.reminderMessage != null) {
                        Text(vm.reminderMessage.orEmpty(), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        item {
            Plate(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Exercise rotation", style = MaterialTheme.typography.titleMedium)
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Switch to new exercise variations every ${Rotation.WEEKS_PER_BLOCK} weeks",
                            modifier = Modifier.weight(1f),
                        )
                        Switch(checked = rotation.enabled, onCheckedChange = vm::setRotationEnabled)
                    }
                    if (rotation.enabled) {
                        Text(
                            "Now in block ${rotation.block() + 1}, week ${rotation.week()} of ${Rotation.WEEKS_PER_BLOCK}.",
                            style = MaterialTheme.typography.bodySmall,
                        )
                        OutlinedButton(onClick = vm::restartRotation) { Text("Restart cycle from today") }
                    } else {
                        Text("Off: the base plan stays the same every week.", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        item {
            Plate(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Plates you own", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Enter the total count of each plate size, like the pair you'd slide onto one weight. " +
                            "A dumbbell pair or barbell splits that count evenly across its two sides, so 4 plates of " +
                            "5 kg means 2 per side.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    if (equipment.plates.isEmpty()) {
                        Text(
                            "No plates added yet — add your first one below.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                    equipment.plates.forEach { plate ->
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text("${formatKg(plate.weightKg)} kg plate", modifier = Modifier.weight(1f))
                            IconButton(onClick = { vm.changePlateCount(plate.weightKg, -1) }) {
                                Icon(Icons.Filled.Remove, contentDescription = "Fewer ${formatKg(plate.weightKg)} kg plates")
                            }
                            Text("${plate.count} total")
                            IconButton(onClick = { vm.changePlateCount(plate.weightKg, 1) }) {
                                Icon(Icons.Filled.Add, contentDescription = "More ${formatKg(plate.weightKg)} kg plates")
                            }
                            IconButton(onClick = { vm.removePlate(plate.weightKg) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Remove ${formatKg(plate.weightKg)} kg plates")
                            }
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = vm.newPlateText,
                            onValueChange = vm::onNewPlateChange,
                            label = { Text("New plate size (kg)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                        )
                        Button(onClick = vm::addPlate) { Text("Add") }
                    }
                    if (vm.equipmentMessage != null) {
                        Text(vm.equipmentMessage.orEmpty(), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        items(Rig.entries) { rig ->
            val weights = remember(equipment, rig) { LoadCalculator.achievableWeights(equipment, rig) }
            Plate(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(rig.label, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "With these plates: ${weights.size} different weights, from ${formatKg(weights.first())} " +
                            "to ${formatKg(weights.last())} kg.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

private fun needsNotificationPermission(context: Context): Boolean =
    Build.VERSION.SDK_INT >= 33 &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
        PackageManager.PERMISSION_GRANTED

private fun openHealthConnectInStore(context: Context) {
    val uri = Uri.parse(
        "market://details?id=${HealthConnectManager.PROVIDER_PACKAGE}&url=healthconnect%3A%2F%2Fonboarding",
    )
    runCatching {
        context.startActivity(
            Intent(Intent.ACTION_VIEW).apply {
                setPackage("com.android.vending")
                data = uri
                putExtra("overlay", true)
                putExtra("callerId", context.packageName)
            },
        )
    }
}
