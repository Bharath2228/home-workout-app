package com.bharath.homeforge.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bharath.homeforge.domain.Equipment
import com.bharath.homeforge.domain.PlannedExercise
import com.bharath.homeforge.domain.Rig
import com.bharath.homeforge.domain.RoutineGenerator
import com.bharath.homeforge.domain.Split

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutinesScreen() {
    val equipment = remember { Equipment.Default }
    var splitName by rememberSaveable { mutableStateOf(Split.FULL_BODY.name) }
    var dayIndex by rememberSaveable { mutableIntStateOf(0) }
    val swaps = remember { mutableStateMapOf<String, Int>() }

    val split = Split.valueOf(splitName)
    val offsets = swaps.entries
        .filter { it.key.startsWith("${split.name}:$dayIndex:") }
        .associate { it.key.substringAfterLast(':').toInt() to it.value }
    val routine = RoutineGenerator.generate(split, dayIndex, equipment, offsets)

    Column(Modifier.fillMaxSize().padding(top = 16.dp)) {
        Text(
            "Routines",
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
                Tab(
                    selected = index == dayIndex,
                    onClick = { dayIndex = index },
                    text = { Text(name) },
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(routine.items, key = { it.slotIndex }) { item ->
                ExerciseCard(item) {
                    val key = "${split.name}:$dayIndex:${item.slotIndex}"
                    swaps[key] = (swaps[key] ?: 0) + 1
                }
            }
        }
    }
}

@Composable
private fun ExerciseCard(item: PlannedExercise, onSwap: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(item.exercise.name, style = MaterialTheme.typography.titleMedium)
                val unit = if (item.exercise.timed) " sec" else " reps"
                Text(
                    "${item.sets} x ${item.reps.first}-${item.reps.last}$unit",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(loadText(item), style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onSwap) {
                Icon(Icons.Filled.SwapHoriz, contentDescription = "Swap exercise")
            }
        }
    }
}

private fun loadText(item: PlannedExercise): String {
    val kg = item.weightKg ?: return "Bodyweight"
    return when (item.exercise.rig) {
        Rig.DUMBBELL_PAIR -> "$kg kg per dumbbell"
        Rig.SINGLE_DUMBBELL -> "$kg kg dumbbell"
        Rig.BARBELL -> "$kg kg total on the bar"
        null -> "Bodyweight"
    }
}
