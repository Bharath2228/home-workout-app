package com.bharath.homeforge.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bharath.homeforge.data.HealthConnectManager
import com.bharath.homeforge.domain.Equipment
import com.bharath.homeforge.domain.LoadCalculator
import com.bharath.homeforge.domain.Rig
import com.bharath.homeforge.ui.SettingsViewModel

@Composable
fun SettingsScreen(vm: SettingsViewModel = viewModel()) {
    val equipment = remember { Equipment.Default }
    val context = LocalContext.current
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
            Card(Modifier.fillMaxWidth()) {
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
                            if (vm.info != null) Text(vm.info.orEmpty(), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }

        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Plates", style = MaterialTheme.typography.titleMedium)
                    equipment.plates.forEach { plate ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${plate.weightKg} kg")
                            Text("x ${plate.count}")
                        }
                    }
                    HorizontalDivider()
                    Text("Rod weight: ${equipment.rodWeightKg} kg", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        items(Rig.entries) { rig ->
            val weights = remember(rig) { LoadCalculator.achievableWeights(equipment, rig) }
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(rig.label, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "${weights.size} loads, ${weights.first()} to ${weights.last()} kg",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

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
