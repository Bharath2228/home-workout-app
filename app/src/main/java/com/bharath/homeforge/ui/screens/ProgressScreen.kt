package com.bharath.homeforge.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bharath.homeforge.data.Measurement
import com.bharath.homeforge.data.SessionPoint
import com.bharath.homeforge.data.buildHistory
import com.bharath.homeforge.domain.Effort
import com.bharath.homeforge.domain.MeasurementType
import com.bharath.homeforge.domain.PersonalRecords
import com.bharath.homeforge.domain.Split
import com.bharath.homeforge.domain.Streaks
import com.bharath.homeforge.ui.ProgressViewModel
import com.bharath.homeforge.ui.formatKg
import com.bharath.homeforge.ui.theme.EmptyState
import com.bharath.homeforge.ui.theme.ForgePalette
import com.bharath.homeforge.ui.theme.Plate
import com.bharath.homeforge.ui.theme.PlateAccent
import com.bharath.homeforge.ui.theme.ReadoutTextStyle
import com.bharath.homeforge.ui.theme.ReadoutTextStyleSmall
import com.bharath.homeforge.ui.theme.ScreenHeader
import com.bharath.homeforge.ui.theme.StatTile
import com.bharath.homeforge.ui.theme.TagPill
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.util.Locale

private const val MODE_LIFTS = "lifts"
private const val MODE_BODY = "body"

@Composable
fun ProgressScreen(onOpenHistory: () -> Unit, vm: ProgressViewModel = viewModel()) {
    val sets by vm.sets.collectAsStateWithLifecycle()
    val sessions by vm.sessions.collectAsStateWithLifecycle()
    val measurements by vm.measurements.collectAsStateWithLifecycle()
    val sessionCount by vm.sessionCount.collectAsStateWithLifecycle()
    val warnings by vm.warnings.collectAsStateWithLifecycle()
    var mode by rememberSaveable { mutableStateOf(MODE_LIFTS) }

    val workoutDays = remember(sessions) {
        sessions.filter { it.splitName != Split.CORE_DAY.name }.map {
            Instant.ofEpochMilli(it.startedAt).atZone(ZoneId.systemDefault()).toLocalDate().toEpochDay()
        }
    }
    val today = LocalDate.now().toEpochDay()
    val streak = Streaks.weeklyStreak(workoutDays, today)
    val thisWeek = Streaks.workoutsThisWeek(workoutDays, today)

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(
            title = "Progress & Records",
            subtitle = "Track strength gains and body composition",
            modifier = Modifier.padding(top = 12.dp),
        )

        // Stat Badges Row
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            StatTile(
                value = sessionCount.toString(),
                label = "Total Sessions",
                modifier = Modifier.weight(1f),
                icon = Icons.AutoMirrored.Filled.ShowChart,
                valueColor = MaterialTheme.colorScheme.primary,
            )
            StatTile(
                value = weeksText(streak),
                label = "Streak",
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.LocalFireDepartment,
                valueColor = MaterialTheme.colorScheme.secondary,
            )
            StatTile(
                value = thisWeek.toString(),
                label = "This Week",
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.CalendarMonth,
                valueColor = MaterialTheme.colorScheme.tertiary,
            )
        }

        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.End,
        ) {
            TextButton(
                onClick = onOpenHistory,
                shape = RoundedCornerShape(8.dp),
            ) {
                Icon(
                    Icons.Filled.History,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp).padding(end = 4.dp),
                )
                Text("Workout History & Logs", style = MaterialTheme.typography.labelMedium)
            }
        }

        WarningsCard(warnings, Modifier.padding(horizontal = 16.dp, vertical = 4.dp))

        // Modern Segmented Toggle: Lifts vs Body
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
                    RoundedCornerShape(12.dp),
                )
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            val isLifts = mode == MODE_LIFTS
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(9.dp))
                    .background(if (isLifts) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                    .clickable { mode = MODE_LIFTS }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        Icons.Filled.FitnessCenter,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (isLifts) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        "Lifts & PRs",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isLifts) FontWeight.Bold else FontWeight.Medium,
                        color = if (isLifts) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            val isBody = mode == MODE_BODY
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(9.dp))
                    .background(if (isBody) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                    .clickable { mode = MODE_BODY }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        Icons.Filled.Straighten,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (isBody) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        "Body Measurements",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isBody) FontWeight.Bold else FontWeight.Medium,
                        color = if (isBody) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        if (mode == MODE_LIFTS) {
            LiftsPanel(sets)
        } else {
            BodyPanel(
                measurements = measurements,
                onAdd = vm::addMeasurement,
                onDelete = vm::deleteMeasurement,
            )
        }
    }
}

private fun weeksText(weeks: Int): String = if (weeks == 1) "1 wk" else "$weeks wks"

@Composable
private fun LiftsPanel(sets: List<com.bharath.homeforge.data.LoggedSet>) {
    val history = remember(sets) { buildHistory(sets) }
    val names = remember(history) { history.keys.sorted() }
    var selected by rememberSaveable { mutableStateOf<String?>(null) }
    val current = selected?.takeIf { it in history } ?: names.firstOrNull()

    if (current == null) {
        EmptyState(
            icon = Icons.AutoMirrored.Filled.ShowChart,
            title = "No lift data recorded yet",
            message = "Complete a workout from the Workout tab and your lift progressions will chart here.",
        )
        return
    }

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(names) { name ->
            val isSelected = name == current
            FilterChip(
                selected = isSelected,
                onClick = { selected = name },
                label = { Text(name, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                shape = RoundedCornerShape(10.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            )
        }
    }

    val points = history.getValue(current)
    val weighted = points.any { it.topWeightKg != null }
    val values = points.map { (if (weighted) it.topWeightKg ?: 0.0 else it.totalReps.toDouble()).toFloat() }
    val best = remember(sets, current) {
        PersonalRecords.best(sets.filter { it.exerciseName == current }.map { Effort(it.exerciseName, it.weightKg, it.reps) })
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Plate(
                Modifier.fillMaxWidth(),
                accent = PlateAccent.EMBER,
                cornerRadius = 18.dp,
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column {
                            Text(
                                text = current,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = if (weighted) "Top load per session (kg)" else "Total reps per session",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        if (best != null) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                            ) {
                                Column(Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                                    Text(
                                        text = "ALL-TIME BEST",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                    Text(
                                        text = if (best.weightKg != null) "${formatKg(best.weightKg)} kg × ${best.reps}" else "${best.reps} reps",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    )
                                }
                            }
                        }
                    }

                    LineChart(values)
                }
            }
        }

        item {
            Text(
                text = "Session History (${points.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 4.dp),
            )
        }

        items(points.reversed()) { point ->
            SessionRow(point, weighted)
        }
    }
}

@Composable
private fun BodyPanel(
    measurements: List<Measurement>,
    onAdd: (MeasurementType, Double) -> Unit,
    onDelete: (Measurement) -> Unit,
) {
    var typeName by rememberSaveable { mutableStateOf(MeasurementType.BODY_WEIGHT.name) }
    val type = MeasurementType.valueOf(typeName)
    var input by remember(type) { mutableStateOf("") }
    var error by remember(type) { mutableStateOf<String?>(null) }
    val entries = remember(measurements, type) { measurements.filter { it.type == type.name } }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(MeasurementType.entries) { option ->
                    val isSelected = option == type
                    FilterChip(
                        selected = isSelected,
                        onClick = { typeName = option.name },
                        label = { Text(option.label) },
                        shape = RoundedCornerShape(10.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        ),
                    )
                }
            }
        }

        item {
            Plate(
                Modifier.fillMaxWidth(),
                cornerRadius = 14.dp,
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Log New Entry: ${type.label}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OutlinedTextField(
                            value = input,
                            onValueChange = { input = it },
                            label = { Text("Value (${type.unit})") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                            ),
                        )
                        Button(
                            onClick = {
                                val value = input.replace(',', '.').toDoubleOrNull()
                                if (value == null || value < type.min || value > type.max) {
                                    error = "Enter ${formatKg(type.min)} to ${formatKg(type.max)} ${type.unit}."
                                } else {
                                    onAdd(type, value)
                                    input = ""
                                    error = null
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(52.dp),
                        ) {
                            Text("Save", fontWeight = FontWeight.Bold)
                        }
                    }
                    if (error != null) {
                        Text(
                            error.orEmpty(),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        }

        if (entries.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Filled.Straighten,
                    title = "No ${type.label.lowercase()} entries",
                    message = "Add your first measurement above to start tracking your physique progress.",
                )
            }
        } else {
            item {
                Plate(
                    Modifier.fillMaxWidth(),
                    accent = PlateAccent.EMBER,
                    cornerRadius = 16.dp,
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "${type.label} Trend (${type.unit})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        val change = entries.last().value - entries.first().value
                        Text(
                            text = "Latest: ${formatKg(entries.last().value)} ${type.unit}" +
                                if (entries.size > 1) " • ${if (change >= 0) "+" else ""}${"%.1f".format(change)} since start" else "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        LineChart(entries.map { it.value.toFloat() })
                    }
                }
            }

            items(entries.reversed(), key = { it.id }) { entry ->
                Plate(
                    Modifier.fillMaxWidth(),
                    cornerRadius = 12.dp,
                ) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column {
                            Text(
                                text = formatDate(entry.time),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = "${formatKg(entry.value)} ${type.unit}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        IconButton(
                            onClick = { onDelete(entry) },
                            colors = IconButtonDefaults.iconButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                        ) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete entry", modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LineChart(values: List<Float>) {
    val color = MaterialTheme.colorScheme.primary
    Canvas(
        Modifier
            .fillMaxWidth()
            .height(170.dp)
            .padding(vertical = 8.dp)
    ) {
        if (values.isEmpty()) return@Canvas
        val min = values.min()
        val max = values.max()
        val range = if (max - min < 1e-6f) 1f else max - min
        val pad = 14.dp.toPx()
        val w = size.width - 2 * pad
        val h = size.height - 2 * pad

        fun point(i: Int): Offset {
            val x = if (values.size == 1) size.width / 2 else pad + w * i / (values.size - 1)
            val y = if (max - min < 1e-6f) size.height / 2 else pad + h * (1 - (values[i] - min) / range)
            return Offset(x, y)
        }

        if (values.size > 1) {
            val path = Path()
            values.indices.forEach { i ->
                val p = point(i)
                if (i == 0) path.moveTo(p.x, p.y) else path.lineTo(p.x, p.y)
            }
            val fill = Path().apply {
                addPath(path)
                lineTo(point(values.lastIndex).x, size.height)
                lineTo(point(0).x, size.height)
                close()
            }
            drawPath(
                fill,
                brush = Brush.verticalGradient(
                    colors = listOf(color.copy(alpha = 0.3f), color.copy(alpha = 0.02f)),
                    startY = 0f,
                    endY = size.height,
                ),
            )
            drawPath(path, color, style = Stroke(width = 3.dp.toPx()))
        }
        values.indices.forEach {
            val p = point(it)
            drawCircle(Color.White, radius = 5.dp.toPx(), center = p)
            drawCircle(color, radius = 3.5.dp.toPx(), center = p)
        }
    }
}

@Composable
private fun SessionRow(point: SessionPoint, weighted: Boolean) {
    val date = remember(point.time) { formatDate(point.time) }
    Plate(
        Modifier.fillMaxWidth(),
        cornerRadius = 12.dp,
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = date,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                if (weighted) {
                    Text(
                        text = "Total Volume: ${formatKg(point.volumeKg)} kg",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            TagPill(
                text = if (weighted) "Top: ${formatKg(point.topWeightKg ?: 0.0)} kg" else "${point.totalReps} reps",
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

private fun formatDate(millis: Long): String =
    SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(millis))
