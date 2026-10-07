package com.bharath.homeforge.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
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
import com.bharath.homeforge.ui.theme.Plate
import com.bharath.homeforge.ui.theme.PlateAccent
import com.bharath.homeforge.ui.theme.ReadoutTextStyle
import com.bharath.homeforge.ui.theme.ReadoutTextStyleSmall
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
        sessions.filter { it.splitName != Split.CORE_DAY.name }.map { Instant.ofEpochMilli(it.startedAt).atZone(ZoneId.systemDefault()).toLocalDate().toEpochDay() }
    }
    val today = LocalDate.now().toEpochDay()
    val streak = Streaks.weeklyStreak(workoutDays, today)
    val thisWeek = Streaks.workoutsThisWeek(workoutDays, today)

    Column(Modifier.fillMaxSize().padding(top = 16.dp)) {
        Text(
            "Progress",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StatGauge("Logged", sessionCount.toString(), Modifier.weight(1f))
            StatGauge("Streak", weeksText(streak), Modifier.weight(1f))
            StatGauge("This week", thisWeek.toString(), Modifier.weight(1f))
        }
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.End,
        ) {
            TextButton(onClick = onOpenHistory) { Text("Manage workouts") }
        }
        WarningsCard(warnings, Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip(selected = mode == MODE_LIFTS, onClick = { mode = MODE_LIFTS }, label = { Text("Lifts") })
            FilterChip(selected = mode == MODE_BODY, onClick = { mode = MODE_BODY }, label = { Text("Body") })
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

private fun weeksText(weeks: Int): String = if (weeks == 1) "1 week" else "$weeks weeks"

@Composable
private fun StatGauge(label: String, value: String, modifier: Modifier = Modifier) {
    Plate(modifier) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Text(value, style = ReadoutTextStyleSmall, color = MaterialTheme.colorScheme.primary)
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun LiftsPanel(sets: List<com.bharath.homeforge.data.LoggedSet>) {
    val history = remember(sets) { buildHistory(sets) }
    val names = remember(history) { history.keys.sorted() }
    var selected by rememberSaveable { mutableStateOf<String?>(null) }
    val current = selected?.takeIf { it in history } ?: names.firstOrNull()

    if (current == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Log a workout to see your progress here.")
        }
        return
    }

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(names) { name ->
            FilterChip(selected = name == current, onClick = { selected = name }, label = { Text(name) })
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
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Plate(Modifier.fillMaxWidth(), accent = PlateAccent.EMBER) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        if (weighted) "Top weight per workout (kg)" else "Total reps per workout",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    if (best != null) {
                        Text(
                            (if (best.weightKg != null) "${formatKg(best.weightKg)} kg x ${best.reps}" else "${best.reps} reps"),
                            style = ReadoutTextStyleSmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text("Best set", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    LineChart(values)
                }
            }
        }
        items(points.reversed()) { point -> SessionRow(point, weighted) }
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
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(MeasurementType.entries) { option ->
                    FilterChip(selected = option == type, onClick = { typeName = option.name }, label = { Text(option.label) })
                }
            }
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    label = { Text("${type.label} (${type.unit})") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                )
                Button(onClick = {
                    val value = input.replace(',', '.').toDoubleOrNull()
                    if (value == null || value < type.min || value > type.max) {
                        error = "Enter ${formatKg(type.min)} to ${formatKg(type.max)} ${type.unit}."
                    } else {
                        onAdd(type, value)
                        input = ""
                        error = null
                    }
                }) { Text("Save") }
            }
            if (error != null) {
                Text(error.orEmpty(), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
        }

        if (entries.isEmpty()) {
            item { Text("No ${type.label.lowercase()} entries yet.") }
        } else {
            item {
                Plate(Modifier.fillMaxWidth(), accent = PlateAccent.EMBER) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("${type.label} over time (${type.unit})", style = MaterialTheme.typography.titleMedium)
                        val change = entries.last().value - entries.first().value
                        Text(
                            "Latest ${formatKg(entries.last().value)} ${type.unit}" +
                                if (entries.size > 1) ", ${if (change >= 0) "+" else ""}${"%.1f".format(change)} since the first entry" else "",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        LineChart(entries.map { it.value.toFloat() })
                    }
                }
            }
            items(entries.reversed(), key = { it.id }) { entry ->
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(formatDate(entry.time), modifier = Modifier.weight(1f))
                    Text("${formatKg(entry.value)} ${type.unit}", style = MaterialTheme.typography.bodyMedium)
                    IconButton(onClick = { onDelete(entry) }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete entry")
                    }
                }
            }
        }
    }
}

@Composable
private fun LineChart(values: List<Float>) {
    val color = MaterialTheme.colorScheme.primary
    Canvas(Modifier.fillMaxWidth().height(160.dp)) {
        if (values.isEmpty()) return@Canvas
        val min = values.min()
        val max = values.max()
        val range = if (max - min < 1e-6f) 1f else max - min
        val pad = 12.dp.toPx()
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
            drawPath(path, color, style = Stroke(width = 3.dp.toPx()))
        }
        values.indices.forEach { drawCircle(color, radius = 5.dp.toPx(), center = point(it)) }
    }
}

@Composable
private fun SessionRow(point: SessionPoint, weighted: Boolean) {
    val date = remember(point.time) { formatDate(point.time) }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(date)
        Text(
            if (weighted) {
                "top ${formatKg(point.topWeightKg ?: 0.0)} kg, volume ${formatKg(point.volumeKg)} kg"
            } else {
                "${point.totalReps} reps"
            },
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

private fun formatDate(millis: Long): String =
    SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(millis))
