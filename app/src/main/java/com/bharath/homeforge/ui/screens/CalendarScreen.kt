package com.bharath.homeforge.ui.screens

import android.app.DatePickerDialog
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bharath.homeforge.domain.DayStatus
import com.bharath.homeforge.domain.PlanHelper
import com.bharath.homeforge.domain.ScheduleEntry
import com.bharath.homeforge.ui.CalendarViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarScreen(onBack: () -> Unit, vm: CalendarViewModel = viewModel()) {
    val context = LocalContext.current
    val prefs by vm.userPrefs.collectAsStateWithLifecycle()
    val schedule by vm.schedule.collectAsStateWithLifecycle()
    val program = prefs.program
    val today = LocalDate.now().toEpochDay()

    val upcoming = schedule.entries.filter { it.status == DayStatus.TODAY || it.status == DayStatus.PLANNED }
    val history = schedule.entries.filter { it.status != DayStatus.TODAY && it.status != DayStatus.PLANNED }.reversed()

    Column(Modifier.fillMaxSize().padding(top = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text("Calendar", style = MaterialTheme.typography.headlineMedium)
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(program.label, style = MaterialTheme.typography.titleMedium)
                        Text(
                            if (schedule.weekNumber == 0) {
                                "Starts ${formatEpochDay(prefs.startEpochDay)}"
                            } else {
                                "Week ${schedule.weekNumber}. ${schedule.doneCount} workouts done, ${schedule.missedCount} missed."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(
                            "A missed workout stays next, so you never skip a part of the plan. " +
                                "The dates after it slide back.",
                            style = MaterialTheme.typography.bodySmall,
                        )
                        OutlinedButton(onClick = {
                            val start = LocalDate.ofEpochDay(prefs.startEpochDay)
                            DatePickerDialog(
                                context,
                                { _, year, month, day -> vm.setStartDate(LocalDate.of(year, month + 1, day).toEpochDay()) },
                                start.year,
                                start.monthValue - 1,
                                start.dayOfMonth,
                            ).show()
                        }) { Text("Started: ${formatEpochDay(prefs.startEpochDay)} (change)") }
                    }
                }
            }

            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Training days", style = MaterialTheme.typography.titleMedium)
                        Row(
                            Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            (1..7).forEach { day ->
                                FilterChip(
                                    selected = day in prefs.trainingDays,
                                    onClick = { vm.toggleTrainingDay(day) },
                                    label = { Text(DayOfWeek.of(day).getDisplayName(TextStyle.SHORT, Locale.getDefault())) },
                                )
                            }
                        }
                        val hint = when {
                            prefs.trainingDays.isEmpty() -> "Pick at least one training day."
                            prefs.trainingDays.size != PlanHelper.defaultTrainingDays(program).size ->
                                "${program.label} is built for ${PlanHelper.defaultTrainingDays(program).size} days a week."
                            else -> "Reminders arrive on these days."
                        }
                        Text(hint, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            item { Text("Coming up", style = MaterialTheme.typography.titleMedium) }
            if (upcoming.isEmpty()) {
                item { Text("Nothing scheduled. Choose your training days above.", style = MaterialTheme.typography.bodyMedium) }
            }
            items(upcoming, key = { "up-${it.epochDay}" }) { EntryRow(it, program, today) }

            if (history.isNotEmpty()) {
                item { Text("History", style = MaterialTheme.typography.titleMedium) }
                items(history, key = { "hist-${it.epochDay}-${it.dayIndex}-${it.status}" }) { EntryRow(it, program, today) }
            }
        }
    }
}

@Composable
private fun EntryRow(entry: ScheduleEntry, program: com.bharath.homeforge.domain.Split, today: Long) {
    val (label, color) = when (entry.status) {
        DayStatus.DONE -> "Done" to MaterialTheme.colorScheme.primary
        DayStatus.EXTRA -> "Done (extra)" to MaterialTheme.colorScheme.primary
        DayStatus.MISSED -> "Missed" to MaterialTheme.colorScheme.error
        DayStatus.TODAY -> "Today" to MaterialTheme.colorScheme.tertiary
        DayStatus.PLANNED -> "Planned" to MaterialTheme.colorScheme.onSurfaceVariant
    }
    val highlight = entry.epochDay == today
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (highlight) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    "${formatEpochDay(entry.epochDay)}: ${program.dayNames[entry.dayIndex.coerceIn(program.dayNames.indices)]}",
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(PlanHelper.focusText(program, entry.dayIndex), style = MaterialTheme.typography.bodySmall)
            }
            Text(label, color = if (color == Color.Unspecified) MaterialTheme.colorScheme.onSurface else color, style = MaterialTheme.typography.labelLarge)
        }
    }
}
