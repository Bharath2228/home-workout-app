package com.bharath.homeforge.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bharath.homeforge.data.LoggedSet
import com.bharath.homeforge.data.WorkoutSession
import com.bharath.homeforge.domain.Split
import com.bharath.homeforge.ui.HistoryViewModel
import com.bharath.homeforge.ui.durationText
import com.bharath.homeforge.ui.formatKg
import com.bharath.homeforge.ui.theme.BackHeader
import com.bharath.homeforge.ui.theme.EmptyState
import com.bharath.homeforge.ui.theme.Plate
import com.bharath.homeforge.ui.theme.TagPill
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(onBack: () -> Unit, vm: HistoryViewModel = viewModel()) {
    val sessions by vm.sessions.collectAsStateWithLifecycle()
    val sets by vm.sets.collectAsStateWithLifecycle()
    val setsBySession = remember(sets) { sets.groupBy { it.sessionId } }
    var expandedId by rememberSaveable { mutableStateOf<Long?>(null) }
    var pendingDelete by remember { mutableStateOf<WorkoutSession?>(null) }

    Column(Modifier.fillMaxSize()) {
        BackHeader("Workout History", onBack, modifier = Modifier.padding(top = 8.dp))

        if (sessions.isEmpty()) {
            EmptyState(
                icon = Icons.Filled.History,
                title = "No workout history",
                message = "Your finished workout sessions will be safely preserved here.",
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(sessions, key = { it.id }) { session ->
                    SessionCard(
                        session = session,
                        sets = setsBySession[session.id].orEmpty().sortedBy { it.id },
                        expanded = expandedId == session.id,
                        onToggle = { expandedId = if (expandedId == session.id) null else session.id },
                        onDelete = { pendingDelete = session },
                        onSaveSet = vm::updateSet,
                        onDeleteSet = vm::deleteSet,
                    )
                }
            }
        }
    }

    pendingDelete?.let { session ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = {
                Text(
                    "Delete Workout?",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Text(
                    "This permanently removes this workout session and all logged sets. If synced, it will also be deleted from Health Connect.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.deleteSession(session.id)
                        pendingDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) { Text("Delete", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun SessionCard(
    session: WorkoutSession,
    sets: List<LoggedSet>,
    expanded: Boolean,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onSaveSet: (LoggedSet, Double?, Int) -> Unit,
    onDeleteSet: (LoggedSet) -> Unit,
) {
    val date = remember(session.startedAt) {
        SimpleDateFormat("EEE, d MMM yyyy • HH:mm", Locale.getDefault()).format(Date(session.startedAt))
    }
    val split = runCatching { Split.valueOf(session.splitName) }.getOrNull()
    val title = listOfNotNull(split?.label, split?.dayNames?.getOrNull(session.dayIndex)).joinToString(" - ")

    Plate(
        Modifier.fillMaxWidth(),
        cornerRadius = 16.dp,
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = date,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TagPill(
                            text = "${sets.size} sets",
                            color = MaterialTheme.colorScheme.primary,
                        )
                        TagPill(
                            text = durationText(session.endedAt - session.startedAt),
                            color = MaterialTheme.colorScheme.secondary,
                            icon = Icons.Filled.Timer,
                        )
                        if (session.warmUpDone) {
                            TagPill(
                                text = "Warmed Up",
                                color = MaterialTheme.colorScheme.tertiary,
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = onToggle) {
                        Text(if (expanded) "Done" else "Edit", fontWeight = FontWeight.Bold)
                    }
                    IconButton(
                        onClick = onDelete,
                        colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
                    ) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete workout", modifier = Modifier.size(20.dp))
                    }
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    sets.forEach { set ->
                        SetEditRow(set, onSaveSet, onDeleteSet)
                    }
                }
            }
        }
    }
}

@Composable
private fun SetEditRow(
    set: LoggedSet,
    onSave: (LoggedSet, Double?, Int) -> Unit,
    onDelete: (LoggedSet) -> Unit,
) {
    var weight by remember(set) { mutableStateOf(set.weightKg?.let(::formatKg).orEmpty()) }
    var reps by remember(set) { mutableStateOf(set.reps.toString()) }

    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.35f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "${set.exerciseName} • Set ${set.setNumber}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (set.weightKg != null) {
                    OutlinedTextField(
                        value = weight,
                        onValueChange = { weight = it },
                        label = { Text("kg") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary),
                    )
                }
                OutlinedTextField(
                    value = reps,
                    onValueChange = { reps = it },
                    label = { Text("reps") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary),
                )
                OutlinedButton(
                    onClick = {
                        val newReps = reps.toIntOrNull()
                        val newWeight = weight.replace(',', '.').toDoubleOrNull()
                        val weightOk = set.weightKg == null || newWeight != null
                        if (newReps != null && newReps > 0 && weightOk) {
                            onSave(set, if (set.weightKg == null) null else newWeight, newReps)
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(16.dp).padding(end = 2.dp))
                    Text("Save")
                }
                IconButton(
                    onClick = { onDelete(set) },
                    modifier = Modifier.size(36.dp),
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete set", modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
