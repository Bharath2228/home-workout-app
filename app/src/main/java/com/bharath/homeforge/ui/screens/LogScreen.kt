package com.bharath.homeforge.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
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
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
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
import kotlinx.coroutines.delay
import com.bharath.homeforge.ui.formatDuration
import com.bharath.homeforge.ui.formatKg
import com.bharath.homeforge.ui.ExerciseDraft
import com.bharath.homeforge.ui.LogViewModel
import com.bharath.homeforge.ui.SetDraft
import com.bharath.homeforge.ui.formatClock
import com.bharath.homeforge.ui.WorkoutDraft
import com.bharath.homeforge.ui.theme.Plate
import com.bharath.homeforge.ui.theme.PlateAccent
import com.bharath.homeforge.ui.theme.ReadoutTextStyle
import com.bharath.homeforge.ui.theme.ScreenHeader
import com.bharath.homeforge.ui.theme.StepBadge

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

    Column(Modifier.fillMaxSize().padding(top = 20.dp)) {
        ScreenHeader("Log a workout")
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
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            WarningsCard(warnings)
            Text(
                "Uses the exercises on the Routines tab, including your swaps. Change your plan there. " +
                    "Weights and sets adapt to how your last sessions went.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(onClick = { onStart(active.split, active.dayIndex) }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                Text("Start ${active.split.dayNames[active.dayIndex]}")
            }
            if (message != null) Text(message, style = MaterialTheme.typography.bodyMedium)
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
    LaunchedEffect(workout.startedAt, paused) {
        while (!paused) {
            now = System.currentTimeMillis()
            delay(1000)
        }
    }
    // Catches edits (typed weight/reps, difficulty taps) that don't otherwise trigger a save.
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
        Row(
            Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(workout.split.dayNames[workout.dayIndex], style = MaterialTheme.typography.headlineMedium)
                Text(
                    "$exercisesDone of ${workout.exercises.size} exercises done",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                shape = MaterialTheme.shapes.small,
            ) {
                Text(
                    formatDuration(elapsedSeconds),
                    style = ReadoutTextStyle,
                    color = if (paused) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                )
            }
        }
        LinearProgressIndicator(
            progress = { exerciseProgress },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Row(
            Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedButton(onClick = if (paused) onResume else onPause) {
                Icon(
                    if (paused) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 6.dp),
                )
                Text(if (paused) "Resume" else "Pause")
            }
            if (paused) {
                Text(
                    "Paused",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
            TextButton(
                onClick = onCancel,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
            ) { Text("Cancel") }
            Button(onClick = onFinish) { Text("Finish") }
        }
        if (restRemaining > 0) {
            Surface(color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.fillMaxWidth()) {
                Row(
                    Modifier.padding(start = 16.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "Rest" + if (paused) " (paused)" else "",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(end = 8.dp),
                    )
                    Text(
                        formatClock(restRemaining),
                        style = ReadoutTextStyle,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.weight(1f),
                    )
                    if (!paused) {
                        TextButton(onClick = { onAddRest(15) }) { Text("+15 s") }
                        TextButton(onClick = onSkipRest) { Text("Skip") }
                    }
                }
            }
        }
        if (message != null) {
            Text(
                message,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                color = MaterialTheme.colorScheme.error,
            )
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
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
                OutlinedButton(onClick = { picking = true }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                    Text("Add exercise")
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
private fun WarmUpCard(moves: List<WarmUpMove>, done: List<Boolean>, onToggle: (Int) -> Unit, onInfo: (String) -> Unit) {
    Plate(accent = PlateAccent.SPARK, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Filled.Whatshot, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                Text("Warm-up", style = MaterialTheme.typography.titleMedium)
            }
            moves.forEachIndexed { index, move ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = done.getOrElse(index) { false },
                        onCheckedChange = { onToggle(index) },
                        colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.secondary),
                    )
                    Column(Modifier.weight(1f)) {
                        Text(move.name, style = MaterialTheme.typography.bodyMedium)
                        Text(move.detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = { onInfo(move.name) }) {
                        Icon(Icons.Filled.Info, contentDescription = "How to do ${move.name}")
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
        title = { Text("Add exercise") },
        text = {
            LazyColumn(Modifier.height(360.dp)) {
                itemsIndexed(ExerciseLibrary.all.filter { !noEquipment || it.equipmentFree }.sortedBy { it.name }) { _, exercise ->
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
    // null until the person taps the header; until then, a finished exercise auto-collapses to a
    // slim summary row (Hevy/Strong-style), keeping the active exercise the thing taking up space.
    var manuallyExpanded by rememberSaveable(exercise.id) { mutableStateOf<Boolean?>(null) }
    val expanded = manuallyExpanded ?: !allDone

    Plate(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.animateContentSize()) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable { manuallyExpanded = !expanded }
                    .padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (allDone) {
                            Icon(
                                Icons.Filled.CheckCircle,
                                contentDescription = "Done",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                        Text(planned.exercise.name, style = MaterialTheme.typography.titleMedium)
                    }
                    if (!expanded) {
                        Text(
                            collapsedSummary(exercise, unit),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (expanded) {
                        Text(
                            "Target: ${planned.sets} x ${planned.reps.first}-${planned.reps.last} $unit",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        if (exercise.lastText != null) {
                            Text(exercise.lastText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (exercise.note != null) {
                            Text("Why: ${exercise.note}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (plateText != null) {
                            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(
                                    Icons.Filled.FitnessCenter,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 2.dp).size(14.dp),
                                )
                                Text(
                                    plateText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1f),
                                )
                            }
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
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
                Icon(
                    if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 12.dp, end = 2.dp).size(20.dp),
                )
                if (expanded) {
                    IconButton(onClick = onInfo, modifier = Modifier.size(36.dp)) {
                        Icon(
                            Icons.Filled.Info,
                            contentDescription = "How to do ${planned.exercise.name}",
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    IconButton(onClick = onMoveUp, enabled = canMoveUp, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Filled.ArrowUpward, contentDescription = "Move up", modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = onMoveDown, enabled = canMoveDown, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Filled.ArrowDownward, contentDescription = "Move down", modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = onRemove, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Filled.Delete, contentDescription = "Remove exercise", modifier = Modifier.size(20.dp))
                    }
                }
            }
            if (expanded) {
                Column(Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (canLinkNext || exercise.isSuperset) {
                        FilterChip(
                            selected = exercise.isSuperset,
                            onClick = onToggleSuperset,
                            label = { Text("Superset with next exercise: no rest in between") },
                            leadingIcon = { Icon(Icons.Filled.Link, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        )
                    }
                    exercise.sets.forEachIndexed { index, set ->
                        if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                        SetRow(index + 1, set, showWeight = planned.exercise.rig != null, unit = unit, onDone = onSetDone)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = onAddSet) {
                            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                            Text("Add set")
                        }
                        TextButton(onClick = onRemoveSet, enabled = exercise.sets.size > 1) { Text("Remove last set") }
                    }
                }
            }
        }
    }
}

/** "2 of 3 sets done • last 12 kg x 10 reps", for the collapsed, all-done summary row. */
private fun collapsedSummary(exercise: ExerciseDraft, unit: String): String {
    val doneCount = exercise.sets.count { it.done }
    val lastDone = exercise.sets.lastOrNull { it.done }
    val detail = when {
        lastDone == null -> null
        lastDone.weight.isNotBlank() -> "${lastDone.weight} kg x ${lastDone.reps} $unit"
        else -> "${lastDone.reps} $unit"
    }
    return "$doneCount of ${exercise.sets.size} sets done" + if (detail != null) " • last $detail" else ""
}

@Composable
private fun SetRow(number: Int, set: SetDraft, showWeight: Boolean, unit: String, onDone: () -> Unit) {
    Column(Modifier.animateContentSize().padding(vertical = 4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StepBadge(number)
            if (showWeight) {
                OutlinedTextField(
                    value = set.weight,
                    onValueChange = { set.weight = it },
                    label = { Text("kg") },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.titleMedium,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                )
            }
            OutlinedTextField(
                value = set.reps,
                onValueChange = { set.reps = it },
                label = { Text(unit) },
                singleLine = true,
                textStyle = MaterialTheme.typography.titleMedium,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
            )
            IconToggleButton(
                checked = set.done,
                onCheckedChange = {
                    set.done = it
                    if (it) onDone()
                },
            ) {
                Icon(
                    if (set.done) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                    contentDescription = if (set.done) "Set done" else "Mark set done",
                    tint = if (set.done) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        AnimatedVisibility(visible = set.done) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "How did it feel?",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = 4.dp),
                )
                Difficulty.entries.forEach { option ->
                    FilterChip(
                        selected = set.difficulty == option,
                        onClick = { set.difficulty = if (set.difficulty == option) null else option },
                        label = { Text(option.label) },
                        leadingIcon = { Icon(difficultyIcon(option), contentDescription = null, modifier = Modifier.size(18.dp)) },
                    )
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
