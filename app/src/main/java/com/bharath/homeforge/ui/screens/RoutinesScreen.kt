package com.bharath.homeforge.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bharath.homeforge.data.SwapRepository
import com.bharath.homeforge.domain.Equipment
import com.bharath.homeforge.domain.PlannedExercise
import com.bharath.homeforge.domain.PlateLoader
import com.bharath.homeforge.domain.Rig
import com.bharath.homeforge.domain.Rotation
import com.bharath.homeforge.domain.RoutineGenerator
import com.bharath.homeforge.domain.Split
import com.bharath.homeforge.ui.RoutinesViewModel
import com.bharath.homeforge.ui.formatClock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutinesScreen(vm: RoutinesViewModel = viewModel()) {
    val equipment by vm.equipment.collectAsStateWithLifecycle()
    val swaps by vm.swaps.collectAsStateWithLifecycle()
    val rotation by vm.rotation.collectAsStateWithLifecycle()
    val userPrefs by vm.userPrefs.collectAsStateWithLifecycle()
    var splitName by rememberSaveable { mutableStateOf(Split.FULL_BODY.name) }
    var dayIndex by rememberSaveable { mutableIntStateOf(0) }

    val split = Split.valueOf(splitName)
    val routine = RoutineGenerator.generate(
        split,
        dayIndex,
        equipment,
        SwapRepository.offsetsFor(swaps, split, dayIndex),
        rotation.block(),
        userPrefs.goal,
    )

    Column(Modifier.fillMaxSize().padding(top = 16.dp)) {
        Text(
            "Routines",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        if (rotation.enabled) {
            Text(
                "Exercise block ${rotation.block() + 1}, week ${rotation.week()} of ${Rotation.WEEKS_PER_BLOCK}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }

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

        if (split == Split.CORE_DAY) {
            Text(
                "An optional extra session for a rest day. It doesn't count toward your rest-day warnings or streak.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }

        PrimaryScrollableTabRow(selectedTabIndex = dayIndex, edgePadding = 0.dp) {
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
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(routine.items, key = { it.slotIndex }) { item ->
                ExerciseCard(item, equipment) { vm.swap(split, dayIndex, item.slotIndex) }
            }
        }
    }
}

@Composable
private fun ExerciseCard(item: PlannedExercise, equipment: Equipment, onSwap: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(item.exercise.name, style = MaterialTheme.typography.titleMedium)
                val unit = if (item.exercise.timed) " sec" else " reps"
                Text(
                    "${item.sets} x ${item.reps.first}-${item.reps.last}$unit",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text("${loadText(item)}, rest ${formatClock(item.restSeconds)}", style = MaterialTheme.typography.bodySmall)
                val rig = item.exercise.rig
                val plates = if (rig != null && item.weightKg != null) PlateLoader.describe(equipment, rig, item.weightKg) else null
                if (plates != null) Text(plates, style = MaterialTheme.typography.bodySmall)
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
