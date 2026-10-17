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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SentimentDissatisfied
import androidx.compose.material.icons.filled.SentimentNeutral
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Visibility
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
import com.bharath.homeforge.domain.Movement
import com.bharath.homeforge.domain.PlateLoader
import com.bharath.homeforge.domain.Rig
import com.bharath.homeforge.domain.ScheduleResult
import com.bharath.homeforge.domain.Split
import com.bharath.homeforge.domain.WarmUp
import com.bharath.homeforge.domain.WarmUpMove
import com.bharath.homeforge.domain.WarmUpSet
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
            onAddWarmUpSets = vm::addWarmUpSets,
        )
    }

    vm.completedSummary?.let { summary ->
        WorkoutCompleteDialog(
            summary = summary,
            onDismiss = vm::dismissSummary,
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
    onAddWarmUpSets: (ExerciseDraft, List<WarmUpSet>) -> Unit,
) {
    var picking by remember { mutableStateOf(false) }
    var visualPlateTarget by remember { mutableStateOf<Pair<Rig, Double>?>(null) }
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
                    onShowPlates = { rig, kg -> visualPlateTarget = rig to kg },
                    onAddWarmUpSets = { warmUps -> onAddWarmUpSets(exercise, warmUps) },
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

    visualPlateTarget?.let { (rig, kg) ->
        PlateCalculatorDialog(
            equipment = equipment,
            rig = rig,
            initialWeightKg = kg,
            onDismiss = { visualPlateTarget = null },
        )
    }
}

