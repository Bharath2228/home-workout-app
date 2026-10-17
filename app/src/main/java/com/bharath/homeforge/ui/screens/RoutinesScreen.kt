package com.bharath.homeforge.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
import com.bharath.homeforge.ui.theme.Plate
import com.bharath.homeforge.ui.theme.ScreenHeader
import com.bharath.homeforge.ui.theme.StepBadge
import com.bharath.homeforge.ui.theme.TagPill

@Composable
fun RoutinesScreen(
    onOpenExercise: (String) -> Unit,
    onOpenCalendar: () -> Unit,
    vm: RoutinesViewModel = viewModel(),
) {
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
    var visualPlateTarget by remember { mutableStateOf<Pair<Rig, Double>?>(null) }

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

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(
            title = "Routines",
            subtitle = "Your structured progression plan",
            modifier = Modifier.padding(top = 12.dp),
        )

        PlanSummary(
            program = program,
            headline = scheduleHeadline(program, schedule, userPrefs.startEpochDay),
            rotationText = if (rotation.enabled) {
                "Exercise block ${rotation.block() + 1}, week ${rotation.week()} of ${Rotation.WEEKS_PER_BLOCK}"
            } else {
                null
            },
            status = levelStatus,
            noEquipment = userPrefs.noEquipment,
            onNoEquipmentChange = vm::setNoEquipment,
            active = active,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            onChangePlan = { picking = true },
            onOpenCalendar = onOpenCalendar,
        )

        PlanDayTabs(program, tab, nextDay) { chosenTab = it }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            itemsIndexed(routine.items, key = { _, item -> item.slotIndex }) { index, item ->
                ExerciseCard(
                    step = index + 1,
                    item = item,
                    equipment = equipment,
                    onInfo = { onOpenExercise(item.exercise.name) },
                    onSwap = { vm.swap(active.split, active.dayIndex, item.slotIndex) },
                    onShowPlates = { rig, kg -> visualPlateTarget = rig to kg },
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

    visualPlateTarget?.let { (rig, kg) ->
        PlateCalculatorDialog(
            equipment = equipment,
            rig = rig,
            initialWeightKg = kg,
            onDismiss = { visualPlateTarget = null },
        )
    }
}

@Composable
private fun ExerciseCard(
    step: Int,
    item: PlannedExercise,
    equipment: Equipment,
    onInfo: () -> Unit,
    onSwap: () -> Unit,
    onShowPlates: (Rig, Double) -> Unit,
) {
    Plate(
        Modifier.fillMaxWidth(),
        cornerRadius = 16.dp,
    ) {
        Row(
            Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StepBadge(step, modifier = Modifier.padding(end = 12.dp))

            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = item.exercise.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )

                val unit = if (item.exercise.timed) "sec" else "reps"
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    TagPill(
                        text = "${item.sets} × ${item.reps.first}-${item.reps.last} $unit",
                        color = MaterialTheme.colorScheme.primary,
                    )

                    TagPill(
                        text = "${item.restSeconds}s rest",
                        color = MaterialTheme.colorScheme.secondary,
                        icon = Icons.Filled.Timer,
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        Icons.Filled.FitnessCenter,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        loadText(item),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                val rig = item.exercise.rig
                val weightKg = item.weightKg

                if (rig != null && weightKg != null) {
                    val plates = PlateLoader.describe(equipment, rig, weightKg)
                    if (plates != null) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { onShowPlates(rig, weightKg) },
                        ) {
                            Row(
                                Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Icon(
                                    Icons.Filled.FitnessCenter,
                                    contentDescription = "View plate setup",
                                    modifier = Modifier.size(11.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    text = plates,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }

            // Quick actions
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(
                    onClick = onInfo,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.4f),
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                    modifier = Modifier.size(36.dp),
                ) {
                    Icon(
                        Icons.Filled.Info,
                        contentDescription = "How to do ${item.exercise.name}",
                        modifier = Modifier.size(18.dp),
                    )
                }
                IconButton(
                    onClick = onSwap,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.4f),
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                    modifier = Modifier.size(36.dp),
                ) {
                    Icon(
                        Icons.Filled.SwapHoriz,
                        contentDescription = "Swap exercise",
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}


private fun loadText(item: PlannedExercise): String {
    val kg = item.weightKg ?: return "Bodyweight"
    return when (item.exercise.rig) {
        Rig.DUMBBELL_PAIR -> "$kg kg / dumbbell"
        Rig.SINGLE_DUMBBELL -> "$kg kg dumbbell"
        Rig.BARBELL -> "$kg kg on bar"
        null -> "Bodyweight"
    }
}
