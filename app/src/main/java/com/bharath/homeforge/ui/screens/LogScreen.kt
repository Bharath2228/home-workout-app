package com.bharath.homeforge.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.bharath.homeforge.domain.Equipment
import com.bharath.homeforge.domain.Exercise
import com.bharath.homeforge.domain.ExerciseLibrary
import com.bharath.homeforge.domain.PlateLoader
import com.bharath.homeforge.domain.Split
import com.bharath.homeforge.domain.WarmUp
import com.bharath.homeforge.ui.formatKg
import com.bharath.homeforge.ui.ExerciseDraft
import com.bharath.homeforge.ui.LogViewModel
import com.bharath.homeforge.ui.SetDraft
import com.bharath.homeforge.ui.formatClock
import com.bharath.homeforge.ui.WorkoutDraft

@Composable
fun LogScreen(vm: LogViewModel = viewModel()) {
    val workout = vm.workout
    val equipment by vm.equipment.collectAsStateWithLifecycle()
    val warnings by vm.warnings.collectAsStateWithLifecycle()
    if (workout == null) {
        StartPane(message = vm.message, warnings = warnings, onStart = vm::start)
    } else {
        WorkoutPane(
            workout = workout,
            equipment = equipment,
            message = vm.message,
            onFinish = vm::finish,
            onCancel = vm::cancel,
            onMove = vm::moveExercise,
            onRemove = vm::removeExercise,
            onAddSet = vm::addSet,
            onRemoveSet = vm::removeLastSet,
            onAddExercise = vm::addExercise,
            restRemaining = vm.restRemaining,
            onAddRest = vm::addRest,
            onSkipRest = vm::skipRest,
            onSetDone = { vm.startRest(it.planned.restSeconds) },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StartPane(message: String?, warnings: List<String>, onStart: (Split, Int) -> Unit) {
    var splitName by rememberSaveable { mutableStateOf(Split.FULL_BODY.name) }
    var dayIndex by rememberSaveable { mutableIntStateOf(0) }
    val split = Split.valueOf(splitName)

    Column(Modifier.fillMaxSize().padding(top = 16.dp)) {
        Text(
            "Log a workout",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        Row(
            Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Split.entries.forEach { option ->
                FilterChip(
                    selected = option == split,
                    onClick = {
                        splitName = option.name
                        dayIndex = 0
                    },
                    label = { Text(option.label) },
                )
            }
        }
        PrimaryScrollableTabRow(selectedTabIndex = dayIndex, edgePadding = 0.dp) {
            split.dayNames.forEachIndexed { index, name ->
                Tab(selected = index == dayIndex, onClick = { dayIndex = index }, text = { Text(name) })
            }
        }
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            WarningsCard(warnings)
            Text(
                "Uses the exercises shown on the Routines tab, including any swaps you made. " +
                    "Weights and sets adapt to how your last sessions went.",
                style = MaterialTheme.typography.bodySmall,
            )
            Button(onClick = { onStart(split, dayIndex) }, modifier = Modifier.fillMaxWidth()) {
                Text("Start workout")
            }
            if (message != null) Text(message, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun WorkoutPane(
    workout: WorkoutDraft,
    equipment: Equipment,
    message: String?,
    onFinish: () -> Unit,
    onCancel: () -> Unit,
    onMove: (Int, Int) -> Unit,
    onRemove: (Int) -> Unit,
    onAddSet: (ExerciseDraft) -> Unit,
    onRemoveSet: (ExerciseDraft) -> Unit,
    onAddExercise: (Exercise) -> Unit,
    restRemaining: Int,
    onAddRest: (Int) -> Unit,
    onSkipRest: () -> Unit,
    onSetDone: (ExerciseDraft) -> Unit,
) {
    var picking by remember { mutableStateOf(false) }
    val warmUpIndex = workout.exercises.indexOfFirst { WarmUp.eligible(it.planned.exercise) }

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp, top = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                workout.split.dayNames[workout.dayIndex],
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onCancel) { Text("Cancel") }
            Button(onClick = onFinish) { Text("Finish") }
        }
        if (restRemaining > 0) {
            Surface(color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.fillMaxWidth()) {
                Row(
                    Modifier.padding(start = 16.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "Rest ${formatClock(restRemaining)}",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(onClick = { onAddRest(15) }) { Text("+15 s") }
                    TextButton(onClick = onSkipRest) { Text("Skip") }
                }
            }
        }
        if (message != null) {
            Text(message, modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.error)
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            itemsIndexed(workout.exercises, key = { _, item -> item.id }) { index, exercise ->
                ExerciseLog(
                    exercise = exercise,
                    equipment = equipment,
                    showWarmUp = index == warmUpIndex,
                    canMoveUp = index > 0,
                    canMoveDown = index < workout.exercises.lastIndex,
                    onMoveUp = { onMove(index, -1) },
                    onMoveDown = { onMove(index, 1) },
                    onRemove = { onRemove(index) },
                    onAddSet = { onAddSet(exercise) },
                    onRemoveSet = { onRemoveSet(exercise) },
                    onSetDone = { onSetDone(exercise) },
                )
            }
            item {
                OutlinedButton(onClick = { picking = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("Add exercise")
                }
            }
        }
    }

    if (picking) {
        ExercisePicker(
            onPick = {
                onAddExercise(it)
                picking = false
            },
            onDismiss = { picking = false },
        )
    }
}

@Composable
private fun ExercisePicker(onPick: (Exercise) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add exercise") },
        text = {
            LazyColumn(Modifier.height(360.dp)) {
                itemsIndexed(ExerciseLibrary.all.sortedBy { it.name }) { _, exercise ->
                    TextButton(onClick = { onPick(exercise) }, modifier = Modifier.fillMaxWidth()) {
                        Text(exercise.name, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
    )
}

@Composable
private fun ExerciseLog(
    exercise: ExerciseDraft,
    equipment: Equipment,
    showWarmUp: Boolean,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onRemove: () -> Unit,
    onAddSet: () -> Unit,
    onRemoveSet: () -> Unit,
    onSetDone: () -> Unit,
) {
    val planned = exercise.planned
    val unit = if (planned.exercise.timed) "sec" else "reps"
    val rig = planned.exercise.rig
    val firstWeight = exercise.sets.firstOrNull()?.weight?.replace(',', '.')?.toDoubleOrNull()
    val plateText = if (rig != null && firstWeight != null) PlateLoader.describe(equipment, rig, firstWeight) else null
    val warmUps = if (showWarmUp && firstWeight != null) WarmUp.sets(equipment, planned.exercise, firstWeight) else emptyList()
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(planned.exercise.name, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Target: ${planned.sets} x ${planned.reps.first}-${planned.reps.last} $unit",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    if (exercise.lastText != null) Text(exercise.lastText, style = MaterialTheme.typography.bodySmall)
                    if (exercise.note != null) Text("Why: ${exercise.note}", style = MaterialTheme.typography.bodySmall)
                    if (plateText != null) {
                        Text(plateText, style = MaterialTheme.typography.bodySmall)
                    } else if (rig != null && firstWeight != null) {
                        Text(
                            "That exact weight can't be built with your plates.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                    if (warmUps.isNotEmpty()) {
                        Text(
                            "Warm-up: " + warmUps.joinToString(", ") { "${formatKg(it.weightKg)} kg x ${it.reps}" },
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
                IconButton(onClick = onMoveUp, enabled = canMoveUp) {
                    Icon(Icons.Filled.ArrowUpward, contentDescription = "Move up")
                }
                IconButton(onClick = onMoveDown, enabled = canMoveDown) {
                    Icon(Icons.Filled.ArrowDownward, contentDescription = "Move down")
                }
                IconButton(onClick = onRemove) {
                    Icon(Icons.Filled.Delete, contentDescription = "Remove exercise")
                }
            }
            exercise.sets.forEachIndexed { index, set ->
                SetRow(index + 1, set, showWeight = planned.exercise.rig != null, unit = unit, onDone = onSetDone)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onAddSet) { Text("Add set") }
                TextButton(onClick = onRemoveSet, enabled = exercise.sets.size > 1) { Text("Remove last set") }
            }
        }
    }
}

@Composable
private fun SetRow(number: Int, set: SetDraft, showWeight: Boolean, unit: String, onDone: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Set $number", modifier = Modifier.padding(end = 4.dp))
        if (showWeight) {
            OutlinedTextField(
                value = set.weight,
                onValueChange = { set.weight = it },
                label = { Text("kg") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
            )
        }
        OutlinedTextField(
            value = set.reps,
            onValueChange = { set.reps = it },
            label = { Text(unit) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f),
        )
        Checkbox(
            checked = set.done,
            onCheckedChange = {
                set.done = it
                if (it) onDone()
            },
        )
    }
}
