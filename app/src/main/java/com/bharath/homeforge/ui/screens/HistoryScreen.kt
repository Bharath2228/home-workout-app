package com.bharath.homeforge.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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

    Column(Modifier.fillMaxSize().padding(top = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text("Workouts", style = MaterialTheme.typography.headlineMedium)
        }

        if (sessions.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No workouts logged yet.")
            }
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
            title = { Text("Delete workout?") },
            text = { Text("This removes the workout and its sets. If synced, it is also removed from Health Connect.") },
            confirmButton = {
                TextButton(onClick = {
                    vm.deleteSession(session.id)
                    pendingDelete = null
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text("Cancel") } },
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
        SimpleDateFormat("EEE d MMM yyyy, HH:mm", Locale.getDefault()).format(Date(session.startedAt))
    }
    val split = runCatching { Split.valueOf(session.splitName) }.getOrNull()
    val title = listOfNotNull(split?.label, split?.dayNames?.getOrNull(session.dayIndex)).joinToString(" - ")

    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(date, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "$title, ${sets.size} sets, ${durationText(session.endedAt - session.startedAt)}",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                TextButton(onClick = onToggle) { Text(if (expanded) "Hide" else "Edit") }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete workout")
                }
            }
            if (expanded) {
                sets.forEach { set -> SetEditRow(set, onSaveSet, onDeleteSet) }
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

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("${set.exerciseName}, set ${set.setNumber}", style = MaterialTheme.typography.bodyMedium)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (set.weightKg != null) {
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text("kg") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                )
            }
            OutlinedTextField(
                value = reps,
                onValueChange = { reps = it },
                label = { Text("reps") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
            )
            OutlinedButton(onClick = {
                val newReps = reps.toIntOrNull()
                val newWeight = weight.replace(',', '.').toDoubleOrNull()
                val weightOk = set.weightKg == null || newWeight != null
                if (newReps != null && newReps > 0 && weightOk) {
                    onSave(set, if (set.weightKg == null) null else newWeight, newReps)
                }
            }) { Text("Save") }
            IconButton(onClick = { onDelete(set) }) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete set")
            }
        }
    }
}
