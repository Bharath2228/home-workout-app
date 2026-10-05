package com.bharath.homeforge.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bharath.homeforge.domain.DayStatus
import com.bharath.homeforge.domain.Level
import com.bharath.homeforge.domain.LevelStatus
import com.bharath.homeforge.domain.PlanHelper
import com.bharath.homeforge.domain.ScheduleResult
import com.bharath.homeforge.domain.Split
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

const val CORE_TAB_LABEL = "Core (extra)"

/** Which day of the plan, or the optional core day, a tab index stands for. */
data class ActiveDay(val split: Split, val dayIndex: Int)

fun activeDay(program: Split, tabIndex: Int): ActiveDay =
    if (tabIndex >= program.dayNames.size) ActiveDay(Split.CORE_DAY, 0) else ActiveDay(program, tabIndex)

fun formatEpochDay(epochDay: Long): String =
    LocalDate.ofEpochDay(epochDay).format(DateTimeFormatter.ofPattern("EEE d MMM", Locale.getDefault()))

/** One line saying what to do today, or when the next workout is. */
fun scheduleHeadline(program: Split, schedule: ScheduleResult, startEpochDay: Long): String {
    val today = LocalDate.now().toEpochDay()
    fun name(dayIndex: Int) = program.dayNames[dayIndex.coerceIn(program.dayNames.indices)]

    val todays = schedule.entries.filter { it.epochDay == today }
    todays.firstOrNull { it.status == DayStatus.TODAY }?.let {
        return "Today, ${formatEpochDay(today)}: ${name(it.dayIndex)}"
    }
    val next = schedule.entries.firstOrNull { it.status == DayStatus.PLANNED }
        ?: return "No training days chosen. Pick them on the Calendar."
    val prefix = when {
        today < startEpochDay -> "Your plan starts ${formatEpochDay(startEpochDay)}."
        todays.isNotEmpty() -> "Done for today."
        else -> "Today is a rest day."
    }
    return "$prefix Next: ${formatEpochDay(next.epochDay)}, ${name(next.dayIndex)}"
}

fun levelText(status: LevelStatus): String {
    val next = status.nextLevel
    val toNext = status.workoutsToNext
    return "Level: ${status.level.label}." + when {
        !status.auto -> " Automatic level-ups are off."
        next != null && toNext != null -> " $toNext more workouts to reach ${next.label}."
        else -> " Top level."
    }
}

@Composable
fun LevelAndGearRow(
    status: LevelStatus,
    noEquipment: Boolean,
    onNoEquipmentChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(levelText(status), style = MaterialTheme.typography.bodyMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = !noEquipment,
                onClick = { onNoEquipmentChange(false) },
                label = { Text("Plates and rods") },
            )
            FilterChip(
                selected = noEquipment,
                onClick = { onNoEquipmentChange(true) },
                label = { Text("No equipment") },
            )
        }
    }
}

@Composable
fun LevelUpDialog(level: Level, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("You reached ${level.label}") },
        text = {
            Text(
                "Great work staying consistent. ${level.description} " +
                    "New exercises start from a heavier weight, and you can change your level any time in Settings.",
            )
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Nice") } },
    )
}

@Composable
fun PlanHeader(
    program: Split,
    headline: String,
    modifier: Modifier = Modifier,
    onChangePlan: (() -> Unit)? = null,
    onOpenCalendar: (() -> Unit)? = null,
) {
    Card(modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Your plan: ${program.label}", style = MaterialTheme.typography.titleMedium)
            Text(headline, style = MaterialTheme.typography.bodyMedium)
            Text(program.schedule, style = MaterialTheme.typography.bodySmall)
            if (onChangePlan != null || onOpenCalendar != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (onOpenCalendar != null) OutlinedButton(onClick = onOpenCalendar) { Text("Calendar") }
                    if (onChangePlan != null) OutlinedButton(onClick = onChangePlan) { Text("Change plan") }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanDayTabs(program: Split, selectedTab: Int, nextDay: Int, onSelect: (Int) -> Unit) {
    PrimaryScrollableTabRow(selectedTabIndex = selectedTab, edgePadding = 0.dp) {
        (program.dayNames + CORE_TAB_LABEL).forEachIndexed { index, name ->
            Tab(
                selected = index == selectedTab,
                onClick = { onSelect(index) },
                text = { Text(if (index == nextDay) "$name (next)" else name) },
            )
        }
    }
}

@Composable
fun DayInfo(active: ActiveDay, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(PlanHelper.focusText(active.split, active.dayIndex), style = MaterialTheme.typography.bodyMedium)
        if (active.split == Split.CORE_DAY) {
            Text(
                "An optional extra for a rest day. It doesn't count toward your rest-day warnings or streak.",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
fun ProgramPickerDialog(current: Split, onPick: (Split) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose your plan") },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    "Not sure? Pick by how many days a week you can train: 2 to 3 days, Full body. " +
                        "3 to 4 days, Push / Pull / Legs. 5 to 6 days, 6-day PPL.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Split.programs.forEach { program ->
                    val selected = program == current
                    Card(
                        onClick = { onPick(program) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (selected) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                        ),
                    ) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                program.label + if (selected) " (current)" else "",
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(program.tagline, style = MaterialTheme.typography.bodyMedium)
                            Text("Best for: ${program.bestFor}", style = MaterialTheme.typography.bodySmall)
                            Text("Schedule: ${program.schedule}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
    )
}
