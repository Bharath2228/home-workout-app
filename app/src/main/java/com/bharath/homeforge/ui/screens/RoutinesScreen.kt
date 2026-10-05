package com.bharath.homeforge.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.bharath.homeforge.ui.RoutinesViewModel
import com.bharath.homeforge.ui.formatClock

@Composable
fun RoutinesScreen(onOpenExercise: (String) -> Unit, onOpenCalendar: () -> Unit, vm: RoutinesViewModel = viewModel()) {
    val equipment by vm.equipment.collectAsStateWithLifecycle()
    val swaps by vm.swaps.collectAsStateWithLifecycle()
    val rotation by vm.rotation.collectAsStateWithLifecycle()
    val userPrefs by vm.userPrefs.collectAsStateWithLifecycle()
    val schedule by vm.schedule.collectAsStateWithLifecycle()
    val levelStatus by vm.levelStatus.collectAsStateWithLifecycle()
    val nextDay = schedule.nextDayIndex

    val program = userPrefs.program
    var chosenTab by rememberSaveable(program) { mutableStateOf<Int?>(null) }
    var picking by remember { mutableStateOf(false) }

    val tab = chosenTab ?: nextDay
    val active = activeDay(program, tab)
    val routine = RoutineGenerator.generate(
        active.split,
        active.dayIndex,
        equipment,
        SwapRepository.offsetsFor(swaps, active.split, active.dayIndex),
        rotation.block(),
        userPrefs.goal,
        levelStatus.level,
        userPrefs.noEquipment,
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

        PlanHeader(
            program = program,
            headline = scheduleHeadline(program, schedule, userPrefs.startEpochDay),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            onChangePlan = { picking = true },
            onOpenCalendar = onOpenCalendar,
        )

        LevelAndGearRow(
            status = levelStatus,
            noEquipment = userPrefs.noEquipment,
            onNoEquipmentChange = vm::setNoEquipment,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        PlanDayTabs(program, tab, nextDay) { chosenTab = it }
        DayInfo(active, Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(routine.items, key = { it.slotIndex }) { item ->
                ExerciseCard(
                    item = item,
                    equipment = equipment,
                    onInfo = { onOpenExercise(item.exercise.name) },
                    onSwap = { vm.swap(active.split, active.dayIndex, item.slotIndex) },
                )
            }
        }
    }

    if (levelStatus.level.ordinal > userPrefs.lastAnnouncedLevel.ordinal) {
        LevelUpDialog(levelStatus.level) { vm.acknowledgeLevel(levelStatus.level) }
    }

    if (picking) {
        ProgramPickerDialog(
            current = program,
            onPick = {
                vm.setProgram(it)
                picking = false
            },
            onDismiss = { picking = false },
        )
    }
}

@Composable
private fun ExerciseCard(item: PlannedExercise, equipment: Equipment, onInfo: () -> Unit, onSwap: () -> Unit) {
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
            IconButton(onClick = onInfo) {
                Icon(Icons.Filled.Info, contentDescription = "How to do ${item.exercise.name}")
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
