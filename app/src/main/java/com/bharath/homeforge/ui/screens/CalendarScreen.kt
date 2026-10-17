package com.bharath.homeforge.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bharath.homeforge.domain.DayStatus
import com.bharath.homeforge.domain.PlanHelper
import com.bharath.homeforge.domain.ScheduleEntry
import com.bharath.homeforge.ui.CalendarViewModel
import com.bharath.homeforge.ui.theme.BackHeader
import com.bharath.homeforge.ui.theme.ForgePalette
import com.bharath.homeforge.ui.theme.Plate
import com.bharath.homeforge.ui.theme.PlateAccent
import com.bharath.homeforge.ui.theme.TagPill
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

    Column(Modifier.fillMaxSize()) {
        BackHeader("Training Calendar", onBack, modifier = Modifier.padding(top = 8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Plan Overview Hero
            item {
                Plate(
                    Modifier.fillMaxWidth(),
                    accent = PlateAccent.EMBER,
                    cornerRadius = 18.dp,
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    Icons.Filled.CalendarMonth,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                            Column(Modifier.weight(1f)) {
                                Text(
                                    text = program.label,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    text = if (schedule.weekNumber == 0) {
                                        "Starts ${formatEpochDay(prefs.startEpochDay)}"
                                    } else {
                                        "Week ${schedule.weekNumber} • ${schedule.doneCount} done • ${schedule.missedCount} missed"
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }

                        Text(
                            text = "A missed session stays next in line so your full routine stays sequenced. Later workouts slide forward smoothly.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )

                        OutlinedButton(
                            onClick = {
                                val start = LocalDate.ofEpochDay(prefs.startEpochDay)
                                DatePickerDialog(
                                    context,
                                    { _, year, month, day -> vm.setStartDate(LocalDate.of(year, month + 1, day).toEpochDay()) },
                                    start.year,
                                    start.monthValue - 1,
                                    start.dayOfMonth,
                                ).show()
                            },
                            shape = RoundedCornerShape(10.dp),
                        ) {
                            Icon(
                                Icons.Filled.EditCalendar,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp).padding(end = 4.dp),
                            )
                            Text("Start Date: ${formatEpochDay(prefs.startEpochDay)} (Change)")
                        }
                    }
                }
            }

            // Training Days Selector Card
            item {
                Plate(
                    Modifier.fillMaxWidth(),
                    cornerRadius = 16.dp,
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                Icons.Filled.CalendarToday,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(20.dp),
                            )
                            Text(
                                "Weekly Training Days",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                        }

                        // Modern Circular / Squircle Day Buttons
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            (1..7).forEach { day ->
                                val selected = day in prefs.trainingDays
                                val dayName = DayOfWeek.of(day).getDisplayName(TextStyle.SHORT, Locale.getDefault()).take(2)
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest,
                                        )
                                        .clickable { vm.toggleTrainingDay(day) },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = dayName,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }

                        val hint = when {
                            prefs.trainingDays.isEmpty() -> "Select your weekly training days to schedule reminders."
                            prefs.trainingDays.size != PlanHelper.defaultTrainingDays(program).size ->
                                "${program.label} is best designed for ${PlanHelper.defaultTrainingDays(program).size} days per week."
                            else -> "Reminders will be delivered on selected days."
                        }
                        Text(
                            text = hint,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            // Upcoming Schedule Section
            item {
                Text(
                    text = "Upcoming Schedule",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }

            if (upcoming.isEmpty()) {
                item {
                    Text(
                        "No sessions scheduled. Pick your weekly training days above.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            items(upcoming, key = { "up-${it.epochDay}" }) {
                EntryRow(it, program, today)
            }

            // Past History Section
            if (history.isNotEmpty()) {
                item {
                    Text(
                        text = "Recent Calendar History",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }

                items(history, key = { "hist-${it.epochDay}-${it.dayIndex}-${it.status}" }) {
                    EntryRow(it, program, today)
                }
            }
        }
    }
}

@Composable
private fun EntryRow(entry: ScheduleEntry, program: com.bharath.homeforge.domain.Split, today: Long) {
    val (label, color, icon) = when (entry.status) {
        DayStatus.DONE -> Triple("Completed", ForgePalette.Success, Icons.Filled.CheckCircle)
        DayStatus.EXTRA -> Triple("Extra Workout", ForgePalette.Success, Icons.Filled.CheckCircle)
        DayStatus.MISSED -> Triple("Missed", MaterialTheme.colorScheme.error, Icons.Filled.Cancel)
        DayStatus.TODAY -> Triple("Today", MaterialTheme.colorScheme.primary, Icons.Filled.Schedule)
        DayStatus.PLANNED -> Triple("Planned", MaterialTheme.colorScheme.tertiary, Icons.Filled.Schedule)
    }
    val isToday = entry.epochDay == today

    Plate(
        Modifier.fillMaxWidth(),
        accent = if (isToday) PlateAccent.EMBER else PlateAccent.NONE,
        cornerRadius = 14.dp,
        containerColor = if (isToday) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Row(
            Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = "${formatEpochDay(entry.epochDay)}: ${program.dayNames[entry.dayIndex.coerceIn(program.dayNames.indices)]}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = PlanHelper.focusText(program, entry.dayIndex),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            TagPill(
                text = label,
                color = color,
                icon = icon,
            )
        }
    }
}
