package com.bharath.homeforge.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.SentimentDissatisfied
import androidx.compose.material.icons.filled.SentimentNeutral
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bharath.homeforge.domain.Difficulty
import com.bharath.homeforge.domain.Equipment
import com.bharath.homeforge.domain.Exercise
import com.bharath.homeforge.domain.ExerciseLibrary
import com.bharath.homeforge.domain.LevelStatus
import com.bharath.homeforge.domain.PlateLoader
import com.bharath.homeforge.domain.ScheduleResult
import com.bharath.homeforge.domain.Split
import com.bharath.homeforge.domain.WarmUp
import com.bharath.homeforge.domain.WarmUpMove
import com.bharath.homeforge.ui.ExerciseDraft
import com.bharath.homeforge.ui.LogViewModel
import com.bharath.homeforge.ui.SetDraft
import com.bharath.homeforge.ui.WorkoutDraft
import com.bharath.homeforge.ui.formatClock
import com.bharath.homeforge.ui.formatDuration
import com.bharath.homeforge.ui.formatKg
import com.bharath.homeforge.ui.theme.ForgePalette
import com.bharath.homeforge.ui.theme.Plate
import com.bharath.homeforge.ui.theme.PlateAccent
import com.bharath.homeforge.ui.theme.ReadoutTextStyle
import com.bharath.homeforge.ui.theme.ReadoutTextStyleMedium
import com.bharath.homeforge.ui.theme.ScreenHeader
import com.bharath.homeforge.ui.theme.StepBadge
import com.bharath.homeforge.ui.theme.TagPill
import kotlinx.coroutines.delay

@Composable
fun LogScreen(onOpenExercise: (String) -> Unit, vm: LogViewModel = viewModel()) {
    val workout = vm.workout
    val equipment by vm.equipment.collectAsStateWithLifecycle()
    val warnings by vm.warnings.collectAsStateWithLifecycle()
    val userPrefs by vm.userPrefs.collectAsStateWithLifecycle()
    val schedule by vm.schedule.collectAsStateWithLifecycle()
    val levelStatus by vm.levelStatus.collectAsStateWithLifecycle()

    if (workout == null) {
        StartPane(
            program = userPrefs.program,
            schedule = schedule,
            startEpochDay = userPrefs.startEpochDay,
            levelStatus = levelStatus,
            noEquipment = userPrefs.noEquipment,
            onNoEquipmentChange = vm::setNoEquipment,
            message = vm.message,
            warnings = warnings,
            onStart = vm::start,
        )
    } else {
        WorkoutPane(
            workout = workout,
            equipment = equipment,
            noEquipment = userPrefs.noEquipment,
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
            onSetDone = vm::setDone,
            onOpenExercise = onOpenExercise,
            onToggleWarmUp = vm::toggleWarmUp,
            onToggleSuperset = vm::toggleSuperset,
            onPause = vm::pause,
            onResume = vm::resume,
            onAutosave = vm::persistActiveWorkout,
        )
    }
}

@Composable
private fun StartPane(
    program: Split,
    schedule: ScheduleResult,
    startEpochDay: Long,
    levelStatus: LevelStatus,
    noEquipment: Boolean,
    onNoEquipmentChange: (Boolean) -> Unit,
    message: String?,
    warnings: List<String>,
    onStart: (Split, Int) -> Unit,
) {
    val nextDay = schedule.nextDayIndex
    var chosenTab by rememberSaveable(program) { mutableStateOf<Int?>(null) }
    val tab = chosenTab ?: nextDay
    val active = activeDay(program, tab)

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(
            title = "Start Workout",
            subtitle = "Weights & sets dynamically adapt to your performance",
            modifier = Modifier.padding(top = 12.dp),
        )

        PlanSummary(
            program = program,
            headline = scheduleHeadline(program, schedule, startEpochDay),
            rotationText = null,
            status = levelStatus,
            noEquipment = noEquipment,
            onNoEquipmentChange = onNoEquipmentChange,
            active = active,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )

        PlanDayTabs(program, tab, nextDay) { chosenTab = it }

        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            WarningsCard(warnings)

            // High Energy Start Action Card
            Plate(
                modifier = Modifier.fillMaxWidth(),
                accent = PlateAccent.EMBER,
                cornerRadius = 18.dp,
            ) {
                Column(
                    Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                    CircleShape,
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                Icons.Filled.Whatshot,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = "Ready to Train?",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = "Session: ${active.split.dayNames[active.dayIndex]}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }

                    Button(
                        onClick = { onStart(active.split, active.dayIndex) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White,
                        ),
                    ) {
                        Icon(
                            Icons.Filled.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp).padding(end = 6.dp),
                        )
                        Text(
                            text = "START ${active.split.dayNames[active.dayIndex].uppercase()}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            if (message != null) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(12.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun WorkoutPane(
    workout: WorkoutDraft,
    equipment: Equipment,
    noEquipment: Boolean,
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
    onOpenExercise: (String) -> Unit,
    onToggleWarmUp: (Int) -> Unit,
    onToggleSuperset: (ExerciseDraft) -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onAutosave: () -> Unit,
) {
    var picking by remember { mutableStateOf(false) }
    val warmUpIndex = workout.exercises.indexOfFirst { WarmUp.eligible(it.planned.exercise) }
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val paused = workout.pausedAt != null

    val view = LocalView.current
    DisposableEffect(Unit) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }

    LaunchedEffect(workout.startedAt, paused) {
        while (!paused) {
            now = System.currentTimeMillis()
            delay(1000)
        }
    }

    LaunchedEffect(workout) {
        while (true) {
            delay(5000)
            onAutosave()
        }
    }

    val elapsedSeconds = ((workout.pausedAt ?: now) - workout.startedAt - workout.pausedMs) / 1000
    val exercisesDone = workout.exercises.count { it.sets.isNotEmpty() && it.sets.all { set -> set.done } }
    val exerciseProgress = if (workout.exercises.isEmpty()) 0f else exercisesDone / workout.exercises.size.toFloat()

    Column(Modifier.fillMaxSize()) {
        // TOP HUD: Header, Timer Pill, Controls
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shadowElevation = 4.dp,
        ) {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = workout.split.dayNames[workout.dayIndex],
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "$exercisesDone of ${workout.exercises.size} exercises completed",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    // Digital Timer Pill
                    Surface(
                        color = if (paused) MaterialTheme.colorScheme.surfaceContainerHighest else MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (paused) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                        ),
                    ) {
                        Row(
                            Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(
                                Icons.Filled.Timer,
                                contentDescription = null,
                                tint = if (paused) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp),
                            )
                            Text(
                                text = formatDuration(elapsedSeconds),
                                style = ReadoutTextStyleMedium,
                                color = if (paused) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }

                // Progress Bar
                LinearProgressIndicator(
                    progress = { exerciseProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, bottom = 4.dp)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                )

                // HUD Controls: Pause / Finish / Cancel
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedButton(
                        onClick = if (paused) onResume else onPause,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    ) {
                        Icon(
                            if (paused) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp).padding(end = 4.dp),
                        )
                        Text(if (paused) "Resume" else "Pause", style = MaterialTheme.typography.labelMedium)
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    TextButton(
                        onClick = onCancel,
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    ) {
                        Text("Cancel", style = MaterialTheme.typography.labelMedium)
                    }

                    Button(
                        onClick = onFinish,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ForgePalette.Success),
                    ) {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp).padding(end = 4.dp),
                        )
                        Text("Finish", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // REST COUNTDOWN HUD
        AnimatedVisibility(
            visible = restRemaining > 0,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Filled.Timer,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    Column(Modifier.padding(start = 10.dp).weight(1f)) {
                        Text(
                            text = if (paused) "REST (PAUSED)" else "REST TIMER",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                        Text(
                            text = formatClock(restRemaining),
                            style = ReadoutTextStyle,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    }
                    if (!paused) {
                        OutlinedButton(
                            onClick = { onAddRest(15) },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.padding(end = 6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        ) {
                            Text("+15s", style = MaterialTheme.typography.labelMedium)
                        }
                        Button(
                            onClick = onSkipRest,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        ) {
                            Text("Skip", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }

        if (message != null) {
            Surface(
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text(
                    text = message,
                    modifier = Modifier.padding(10.dp),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }

        // EXERCISE LOG LIST
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                WarmUpCard(
                    moves = workout.warmUpMoves,
                    done = workout.warmUpDone,
                    onToggle = onToggleWarmUp,
                    onInfo = onOpenExercise,
                )
            }

            itemsIndexed(workout.exercises, key = { _, item -> item.id }) { index, exercise ->
                ExerciseLog(
                    exercise = exercise,
                    equipment = equipment,
                    showWarmUp = index == warmUpIndex,
                    canMoveUp = index > 0,
                    canMoveDown = index < workout.exercises.lastIndex,
                    canLinkNext = index < workout.exercises.lastIndex,
                    onMoveUp = { onMove(index, -1) },
                    onMoveDown = { onMove(index, 1) },
                    onRemove = { onRemove(index) },
                    onAddSet = { onAddSet(exercise) },
                    onRemoveSet = { onRemoveSet(exercise) },
                    onSetDone = { onSetDone(exercise) },
                    onInfo = { onOpenExercise(exercise.planned.exercise.name) },
                    onToggleSuperset = { onToggleSuperset(exercise) },
                )
            }

            item {
                OutlinedButton(
                    onClick = { picking = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp).padding(end = 6.dp),
                    )
                    Text("Add Exercise", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }

    if (picking) {
        ExercisePicker(
            noEquipment = noEquipment,
            onPick = {
                onAddExercise(it)
                picking = false
            },
            onDismiss = { picking = false },
        )
    }
}

@Composable
private fun WarmUpCard(
    moves: List<WarmUpMove>,
    done: List<Boolean>,
    onToggle: (Int) -> Unit,
    onInfo: (String) -> Unit,
) {
    Plate(
        accent = PlateAccent.SPARK,
        cornerRadius = 16.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Filled.Whatshot,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(18.dp),
                    )
                }
                Text(
                    text = "Mobility Warm-up",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }

            moves.forEachIndexed { index, move ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onToggle(index) }
                        .padding(vertical = 4.dp),
                ) {
                    Checkbox(
                        checked = done.getOrElse(index) { false },
                        onCheckedChange = { onToggle(index) },
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.secondary,
                        ),
                    )
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = move.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = move.detail,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    IconButton(
                        onClick = { onInfo(move.name) },
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            Icons.Filled.Info,
                            contentDescription = "How to do ${move.name}",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExercisePicker(noEquipment: Boolean, onPick: (Exercise) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Add Exercise to Workout",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            LazyColumn(Modifier.height(380.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                itemsIndexed(ExerciseLibrary.all.filter { !noEquipment || it.equipmentFree }.sortedBy { it.name }) { _, exercise ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onPick(exercise) },
                        color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(10.dp),
                    ) {
                        Row(
                            Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = exercise.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            TagPill(
                                text = exercise.movement.label,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        },
    )
}

@Composable
private fun ExerciseLog(
    exercise: ExerciseDraft,
    equipment: Equipment,
    showWarmUp: Boolean,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    canLinkNext: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onRemove: () -> Unit,
    onAddSet: () -> Unit,
    onRemoveSet: () -> Unit,
    onSetDone: () -> Unit,
    onInfo: () -> Unit,
    onToggleSuperset: () -> Unit,
) {
    val planned = exercise.planned
    val unit = if (planned.exercise.timed) "sec" else "reps"
    val rig = planned.exercise.rig
    val firstWeight = exercise.sets.firstOrNull()?.weight?.replace(',', '.')?.toDoubleOrNull()
    val plateText = if (rig != null && firstWeight != null) PlateLoader.describe(equipment, rig, firstWeight) else null
    val warmUps = if (showWarmUp && firstWeight != null) WarmUp.sets(equipment, planned.exercise, firstWeight) else emptyList()
    val allDone = exercise.sets.isNotEmpty() && exercise.sets.all { it.done }

    var manuallyExpanded by rememberSaveable(exercise.id) { mutableStateOf<Boolean?>(null) }
    val expanded = manuallyExpanded ?: !allDone

    Plate(
        modifier = Modifier.fillMaxWidth(),
        accent = if (allDone) PlateAccent.SUCCESS else PlateAccent.NONE,
        cornerRadius = 16.dp,
    ) {
        Column(Modifier.animateContentSize()) {
            // Exercise Card Header
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable { manuallyExpanded = !expanded }
                    .padding(start = 14.dp, top = 12.dp, bottom = 12.dp, end = 6.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        if (allDone) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .background(ForgePalette.Success.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    Icons.Filled.Check,
                                    contentDescription = "Completed",
                                    tint = ForgePalette.Success,
                                    modifier = Modifier.size(14.dp),
                                )
                            }
                        }
                        Text(
                            text = planned.exercise.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }

                    if (!expanded) {
                        Text(
                            text = collapsedSummary(exercise, unit),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    if (expanded) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            TagPill(
                                text = "Target: ${planned.sets} × ${planned.reps.first}-${planned.reps.last} $unit",
                                color = MaterialTheme.colorScheme.primary,
                            )
                            if (exercise.isSuperset) {
                                TagPill(
                                    text = "Superset",
                                    color = MaterialTheme.colorScheme.secondary,
                                    icon = Icons.Filled.Link,
                                )
                            }
                        }

                        if (exercise.lastText != null) {
                            Text(
                                text = "Prior: ${exercise.lastText}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        if (exercise.note != null) {
                            Text(
                                text = "Adaptation: ${exercise.note}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary,
                            )
                        }

                        if (plateText != null) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
                            ) {
                                Text(
                                    text = plateText,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                )
                            }
                        } else if (rig != null && firstWeight != null) {
                            Text(
                                "Weight cannot be built with your current plates.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }

                        if (warmUps.isNotEmpty()) {
                            Text(
                                "Warm-up: " + warmUps.joinToString(", ") { "${formatKg(it.weightKg)} kg × ${it.reps}" },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary,
                            )
                        }
                    }
                }

                // Quick Header Actions
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (expanded) {
                        IconButton(onClick = onInfo, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Filled.Info, contentDescription = "Exercise info", modifier = Modifier.size(18.dp))
                        }
                        IconButton(onClick = onMoveUp, enabled = canMoveUp, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Filled.ArrowUpward, contentDescription = "Move up", modifier = Modifier.size(18.dp))
                        }
                        IconButton(onClick = onMoveDown, enabled = canMoveDown, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Filled.ArrowDownward, contentDescription = "Move down", modifier = Modifier.size(18.dp))
                        }
                        IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Filled.Delete, contentDescription = "Remove exercise", modifier = Modifier.size(18.dp))
                        }
                    }
                    Icon(
                        if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 2.dp).size(22.dp),
                    )
                }
            }

            // Sets Table (Expanded)
            if (expanded) {
                Column(
                    Modifier.padding(start = 14.dp, end = 14.dp, bottom = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (canLinkNext || exercise.isSuperset) {
                        FilterChip(
                            selected = exercise.isSuperset,
                            onClick = onToggleSuperset,
                            label = { Text("Superset with next exercise (no rest in-between)") },
                            leadingIcon = { Icon(Icons.Filled.Link, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            shape = RoundedCornerShape(8.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                                selectedLabelColor = MaterialTheme.colorScheme.secondary,
                            ),
                        )
                    }

                    // Table Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            "SET",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(32.dp),
                            textAlign = TextAlign.Center,
                        )
                        if (planned.exercise.rig != null) {
                            Text(
                                "KG",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                            )
                        }
                        Text(
                            unit.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            "DONE",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(44.dp),
                            textAlign = TextAlign.Center,
                        )
                    }

                    exercise.sets.forEachIndexed { index, set ->
                        SetRow(
                            number = index + 1,
                            set = set,
                            showWeight = planned.exercise.rig != null,
                            unit = unit,
                            onDone = onSetDone,
                        )
                    }

                    // Set management buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OutlinedButton(
                            onClick = onAddSet,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp).padding(end = 4.dp))
                            Text("Add Set", style = MaterialTheme.typography.labelMedium)
                        }

                        if (exercise.sets.size > 1) {
                            TextButton(
                                onClick = onRemoveSet,
                                shape = RoundedCornerShape(10.dp),
                            ) {
                                Text("Remove Set", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun collapsedSummary(exercise: ExerciseDraft, unit: String): String {
    val doneCount = exercise.sets.count { it.done }
    val lastDone = exercise.sets.lastOrNull { it.done }
    val detail = when {
        lastDone == null -> null
        lastDone.weight.isNotBlank() -> "${lastDone.weight} kg × ${lastDone.reps} $unit"
        else -> "${lastDone.reps} $unit"
    }
    return "$doneCount of ${exercise.sets.size} sets done" + if (detail != null) " • $detail" else ""
}

@Composable
private fun SetRow(
    number: Int,
    set: SetDraft,
    showWeight: Boolean,
    unit: String,
    onDone: () -> Unit,
) {
    Surface(
        color = if (set.done) MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.35f) else Color.Transparent,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            Modifier
                .animateContentSize()
                .padding(horizontal = 4.dp, vertical = 4.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Set Number Badge
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            if (set.done) ForgePalette.Success.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceContainerHighest,
                            CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "$number",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (set.done) ForgePalette.Success else MaterialTheme.colorScheme.onSurface,
                    )
                }

                if (showWeight) {
                    OutlinedTextField(
                        value = set.weight,
                        onValueChange = { set.weight = it },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.titleMedium.copy(textAlign = TextAlign.Center),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                        ),
                    )
                }

                OutlinedTextField(
                    value = set.reps,
                    onValueChange = { set.reps = it },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.titleMedium.copy(textAlign = TextAlign.Center),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                    ),
                )

                // Check button
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            if (set.done) ForgePalette.Success else MaterialTheme.colorScheme.surfaceContainerHighest,
                        )
                        .clickable {
                            val next = !set.done
                            set.done = next
                            if (next) onDone()
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        if (set.done) Icons.Filled.Check else Icons.Filled.RadioButtonUnchecked,
                        contentDescription = if (set.done) "Completed" else "Mark set completed",
                        tint = if (set.done) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            // Perceived difficulty rating
            AnimatedVisibility(visible = set.done) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        "Effort:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp, end = 2.dp),
                    )

                    Difficulty.entries.forEach { option ->
                        val selected = set.difficulty == option
                        val color = when (option) {
                            Difficulty.EASY -> ForgePalette.Success
                            Difficulty.OK -> MaterialTheme.colorScheme.secondary
                            Difficulty.HARD -> MaterialTheme.colorScheme.primary
                        }

                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    set.difficulty = if (selected) null else option
                                }
                                .border(
                                    width = 1.dp,
                                    color = if (selected) color else color.copy(alpha = 0.25f),
                                    shape = RoundedCornerShape(8.dp),
                                ),
                            color = if (selected) color.copy(alpha = 0.18f) else Color.Transparent,
                            shape = RoundedCornerShape(8.dp),
                        ) {
                            Row(
                                Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Icon(
                                    difficultyIcon(option),
                                    contentDescription = null,
                                    tint = if (selected) color else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(14.dp),
                                )
                                Text(
                                    text = option.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (selected) color else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun difficultyIcon(difficulty: Difficulty) = when (difficulty) {
    Difficulty.EASY -> Icons.Filled.SentimentSatisfied
    Difficulty.OK -> Icons.Filled.SentimentNeutral
    Difficulty.HARD -> Icons.Filled.SentimentDissatisfied
}
