package com.bharath.homeforge.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bharath.homeforge.domain.Split
import com.bharath.homeforge.ui.ExerciseDraft
import com.bharath.homeforge.ui.LogViewModel
import com.bharath.homeforge.ui.SetDraft
import com.bharath.homeforge.ui.WorkoutDraft

@Composable
fun LogScreen(vm: LogViewModel = viewModel()) {
    val workout = vm.workout
    if (workout == null) {
        StartPane(message = vm.message, onStart = vm::start)
    } else {
        WorkoutPane(workout, message = vm.message, onFinish = vm::finish, onCancel = vm::cancel)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StartPane(message: String?, onStart: (Split, Int) -> Unit) {
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
            Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
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
        PrimaryTabRow(selectedTabIndex = dayIndex) {
            split.dayNames.forEachIndexed { index, name ->
                Tab(selected = index == dayIndex, onClick = { dayIndex = index }, text = { Text(name) })
            }
        }
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
    message: String?,
    onFinish: () -> Unit,
    onCancel: () -> Unit,
) {
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
        if (message != null) {
            Text(message, modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.error)
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(workout.exercises, key = { it.planned.slotIndex }) { ExerciseLog(it) }
        }
    }
}

@Composable
private fun ExerciseLog(exercise: ExerciseDraft) {
    val planned = exercise.planned
    val unit = if (planned.exercise.timed) "sec" else "reps"
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(planned.exercise.name, style = MaterialTheme.typography.titleMedium)
            Text(
                "Target: ${planned.sets} x ${planned.reps.first}-${planned.reps.last} $unit",
                style = MaterialTheme.typography.bodyMedium,
            )
            if (exercise.lastText != null) Text(exercise.lastText, style = MaterialTheme.typography.bodySmall)
            exercise.sets.forEach { SetRow(it, showWeight = planned.exercise.rig != null, unit = unit) }
        }
    }
}

@Composable
private fun SetRow(set: SetDraft, showWeight: Boolean, unit: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Set ${set.setNumber}", modifier = Modifier.padding(end = 4.dp))
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
        Checkbox(checked = set.done, onCheckedChange = { set.done = it })
    }
}
