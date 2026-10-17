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
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
import com.bharath.homeforge.ui.theme.ScreenHeader
import com.bharath.homeforge.ui.theme.TagPill
import java.time.LocalDate

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
            title = { Text("Replace your data?", fontWeight = FontWeight.Bold) },
            text = { Text("Importing will replace your existing workouts, body measurements, and equipment settings with the backup file.") },
            confirmButton = {
                TextButton(onClick = {
                    vm.importBackup(uri)
                    pendingImport = null
                }) { Text("Import & Replace", fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { pendingImport = null }) { Text("Cancel") } },
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        PermissionController.createRequestPermissionResultContract(),
    ) { vm.refresh() }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { vm.refresh() }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            ScreenHeader(
                title = "Settings",
                subtitle = "Manage equipment, routines, and app preferences",
                modifier = Modifier.padding(bottom = 4.dp),
            )
        }

        // Equipment & Plates
        item {
            Plate(Modifier.fillMaxWidth(), cornerRadius = 16.dp) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SectionTitle(Icons.Filled.FitnessCenter, "Plates & Equipment")
                    Text(
                        text = "Specify the total count for each plate size you own. Plates are distributed symmetrically across both sides of barbells and dumbbells.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    if (equipment.plates.isEmpty()) {
                        Text(
                            "No plates registered yet. Add your plate inventory below.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }

                    equipment.plates.forEach { plate ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.35f),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                TagPill(
                                    text = "${formatKg(plate.weightKg)} kg",
                                    color = MaterialTheme.colorScheme.primary,
                                )

                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "${plate.count} plates total",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.weight(1f),
                                )

                                IconButton(
                                    onClick = { vm.changePlateCount(plate.weightKg, -1) },
                                    modifier = Modifier.size(34.dp),
                                ) {
                                    Icon(Icons.Filled.Remove, contentDescription = "Decrease count", modifier = Modifier.size(18.dp))
                                }

                                IconButton(
                                    onClick = { vm.changePlateCount(plate.weightKg, 1) },
                                    modifier = Modifier.size(34.dp),
                                ) {
                                    Icon(Icons.Filled.Add, contentDescription = "Increase count", modifier = Modifier.size(18.dp))
                                }

                                IconButton(
                                    onClick = { vm.removePlate(plate.weightKg) },
                                    modifier = Modifier.size(34.dp),
                                ) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Remove plate", modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }

                    // Add Plate Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OutlinedTextField(
                            value = vm.newPlateText,
                            onValueChange = vm::onNewPlateChange,
                            label = { Text("Plate size (kg)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary),
                        )
                        Button(
                            onClick = vm::addPlate,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(52.dp),
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp).padding(end = 4.dp))
                            Text("Add")
                        }
                    }

                    if (vm.equipmentMessage != null) {
                        Text(vm.equipmentMessage.orEmpty(), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        // Achievable Loads Summary
        item {
            Plate(Modifier.fillMaxWidth(), cornerRadius = 16.dp) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionTitle(Icons.Filled.Tune, "Achievable Equipment Loads")
                    Rig.entries.forEach { rig ->
                        val weights = remember(equipment, rig) { LoadCalculator.achievableWeights(equipment, rig) }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.3f),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(rig.label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text(
                                    "${weights.size} possible increments, from ${formatKg(weights.first())} kg to ${formatKg(weights.last())} kg",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }

        // Training Level & Progression
        item {
            Plate(Modifier.fillMaxWidth(), cornerRadius = 16.dp) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionTitle(Icons.Filled.FitnessCenter, "Level & Progression")
                    Text(levelText(levelStatus), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Text(levelStatus.level.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Text("Selected Level", style = MaterialTheme.typography.labelLarge)
                    Row(
                        Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Level.entries.forEach { option ->
                            val isSelected = option == userPrefs.level
                            FilterChip(
                                selected = isSelected,
                                onClick = { vm.setLevel(option) },
                                label = { Text(option.label) },
                                shape = RoundedCornerShape(10.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                ),
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))

                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f).padding(end = 8.dp)) {
                            Text("Automatic Level-ups", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text(
                                "${LevelProgress.INTERMEDIATE_AT} workouts to Intermediate, ${LevelProgress.ADVANCED_AT} to Advanced",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Switch(
                            checked = userPrefs.autoLevel,
                            onCheckedChange = vm::setAutoLevel,
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = MaterialTheme.colorScheme.primary),
                        )
                    }

                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f).padding(end = 8.dp)) {
                            Text("No-Equipment Mode", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text("Build all workouts with bodyweight variations only", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = userPrefs.noEquipment,
                            onCheckedChange = vm::setNoEquipment,
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = MaterialTheme.colorScheme.primary),
                        )
                    }
                }
            }
        }

        // Training Goal & Timers
        item {
            Plate(Modifier.fillMaxWidth(), cornerRadius = 16.dp) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionTitle(Icons.Filled.Flag, "Training Goal")
                    Row(
                        Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Goal.entries.forEach { goal ->
                            val isSelected = goal == userPrefs.goal
                            FilterChip(
                                selected = isSelected,
                                onClick = { vm.setGoal(goal) },
                                label = { Text(goal.label) },
                                shape = RoundedCornerShape(10.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                ),
                            )
                        }
                    }
                    Text(userPrefs.goal.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))

                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Rest Timer", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text("Automatic rest countdown after checking a set", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = userPrefs.restTimerEnabled,
                            onCheckedChange = vm::setRestTimerEnabled,
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = MaterialTheme.colorScheme.primary),
                        )
                    }
                }
            }
        }

        // 4-Week Exercise Variation Cycle
        item {
            Plate(Modifier.fillMaxWidth(), cornerRadius = 16.dp) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionTitle(Icons.Filled.Autorenew, "Exercise Variation Cycle")
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f).padding(end = 8.dp)) {
                            Text("4-Week Rotation", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text("Rotates exercise variations every ${Rotation.WEEKS_PER_BLOCK} weeks for balanced development", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = rotation.enabled,
                            onCheckedChange = vm::setRotationEnabled,
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = MaterialTheme.colorScheme.primary),
                        )
                    }
                    if (rotation.enabled) {
                        TagPill(
                            text = "Active: Block ${rotation.block() + 1}, Week ${rotation.week()} of ${Rotation.WEEKS_PER_BLOCK}",
                            color = MaterialTheme.colorScheme.secondary,
                        )
                        OutlinedButton(onClick = vm::restartRotation, shape = RoundedCornerShape(10.dp)) {
                            Text("Restart Cycle from Today")
                        }
                    }
                }
            }
        }

        // Reminders
        item {
            Plate(Modifier.fillMaxWidth(), cornerRadius = 16.dp) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionTitle(Icons.Filled.Notifications, "Training Reminders")
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Daily Workout Alerts", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text("Prompt on planned training days", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = userPrefs.remindersEnabled,
                            onCheckedChange = { enable ->
                                if (enable && needsNotificationPermission(context)) {
                                    notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    vm.setRemindersEnabled(enable)
                                }
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = MaterialTheme.colorScheme.primary),
                        )
                    }
                    if (userPrefs.remindersEnabled) {
                        OutlinedButton(
                            onClick = {
                                TimePickerDialog(
                                    context,
                                    { _, hour, minute -> vm.setReminderTime(hour, minute) },
                                    userPrefs.reminderHour,
                                    userPrefs.reminderMinute,
                                    true,
                                ).show()
                            },
                            shape = RoundedCornerShape(10.dp),
                        ) {
                            Icon(Icons.Filled.Notifications, contentDescription = null, modifier = Modifier.size(16.dp).padding(end = 4.dp))
                            Text("Reminder Time: %02d:%02d".format(userPrefs.reminderHour, userPrefs.reminderMinute))
                        }
                    }
                    if (vm.reminderMessage != null) {
                        Text(vm.reminderMessage.orEmpty(), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        // Body Weight & Calorie Estimate
        item {
            Plate(Modifier.fillMaxWidth(), cornerRadius = 16.dp) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionTitle(Icons.Filled.MonitorWeight, "Body Weight")
                    Text(
                        "Used to calculate estimated calorie burn for workouts.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = vm.bodyWeightText,
                            onValueChange = vm::onBodyWeightChange,
                            label = { Text("Weight (kg)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary),
                        )
                        Button(
                            onClick = vm::saveBodyWeight,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(52.dp),
                        ) {
                            Text("Save", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Health Connect Integration
        item {
            Plate(Modifier.fillMaxWidth(), cornerRadius = 16.dp) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionTitle(Icons.Filled.MonitorHeart, "Health Connect Sync")
                    when {
                        vm.sdkStatus == HealthConnectClient.SDK_UNAVAILABLE ->
                            Text("Health Connect is not supported on this device.", style = MaterialTheme.typography.bodySmall)

                        vm.sdkStatus == HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> {
                            Text("Install or update Health Connect to sync workouts.", style = MaterialTheme.typography.bodySmall)
                            Button(onClick = { openHealthConnectInStore(context) }, shape = RoundedCornerShape(10.dp)) {
                                Text("Open Google Play Store")
                            }
                        }

                        !vm.granted -> {
                            Text("Sync finished sessions and calorie burn to your central health dashboard.", style = MaterialTheme.typography.bodySmall)
                            Button(onClick = { permissionLauncher.launch(vm.permissions) }, shape = RoundedCornerShape(10.dp)) {
                                Text("Grant Permission")
                            }
                        }

                        else -> {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Text("Sync New Workouts", modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                                Switch(
                                    checked = vm.syncEnabled,
                                    onCheckedChange = vm::updateSyncEnabled,
                                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = MaterialTheme.colorScheme.primary),
                                )
                            }
                            OutlinedButton(onClick = vm::syncPast, shape = RoundedCornerShape(10.dp)) {
                                Text("Sync Past Workouts")
                            }
                        }
                    }
                    if (vm.info != null) Text(vm.info.orEmpty(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                }
            }
        }

        // Data Backup & Export
        item {
            Plate(Modifier.fillMaxWidth(), cornerRadius = 16.dp) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionTitle(Icons.Filled.CloudUpload, "Data Backup & Portability")
                    Text(
                        "All workout history and equipment configurations are stored securely on this phone. Export a JSON backup to keep your data safe.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { exportLauncher.launch("homeforge-backup-${LocalDate.now()}.json") },
                            shape = RoundedCornerShape(10.dp),
                        ) {
                            Icon(Icons.Filled.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp).padding(end = 4.dp))
                            Text("Export Backup")
                        }
                        OutlinedButton(
                            onClick = { importLauncher.launch(arrayOf("*/*")) },
                            shape = RoundedCornerShape(10.dp),
                        ) {
                            Icon(Icons.Filled.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp).padding(end = 4.dp))
                            Text("Import Backup")
                        }
                    }
                    if (vm.backupMessage != null) {
                        Text(vm.backupMessage.orEmpty(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }
        }

        // About & Credits
        item {
            Plate(Modifier.fillMaxWidth(), cornerRadius = 16.dp) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionTitle(Icons.Filled.PhotoLibrary, "About & Media Credits")
                    Text(
                        "Exercise demonstration photos are sourced from the wger.de database under Creative Commons licenses.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    OutlinedButton(onClick = onOpenCredits, shape = RoundedCornerShape(10.dp)) {
                        Text("View Picture Credits")
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp),
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
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
