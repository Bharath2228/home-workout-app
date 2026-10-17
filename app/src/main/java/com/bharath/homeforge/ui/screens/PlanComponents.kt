package com.bharath.homeforge.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bharath.homeforge.domain.DayStatus
import com.bharath.homeforge.domain.Level
import com.bharath.homeforge.domain.LevelStatus
import com.bharath.homeforge.domain.PlanHelper
import com.bharath.homeforge.domain.ScheduleResult
import com.bharath.homeforge.domain.Split
import com.bharath.homeforge.ui.theme.ForgePalette
import com.bharath.homeforge.ui.theme.Plate
import com.bharath.homeforge.ui.theme.PlateAccent
import com.bharath.homeforge.ui.theme.TagPill
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
        return "Today: ${name(it.dayIndex)}"
    }
    val next = schedule.entries.firstOrNull { it.status == DayStatus.PLANNED }
        ?: return "No training days chosen. Pick them on the Calendar."
    val prefix = when {
        today < startEpochDay -> "Starts ${formatEpochDay(startEpochDay)}."
        todays.isNotEmpty() -> "Done for today."
        else -> "Rest day."
    }
    return "$prefix Next: ${formatEpochDay(next.epochDay)}, ${name(next.dayIndex)}"
}

fun levelText(status: LevelStatus): String {
    val next = status.nextLevel
    val toNext = status.workoutsToNext
    return "Level: ${status.level.label}." + when {
        !status.auto -> " Manual mode."
        next != null && toNext != null -> " $toNext more workouts to ${next.label}."
        else -> " Top level reached."
    }
}

@Composable
fun LevelUpDialog(level: Level, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.AutoMirrored.Filled.TrendingUp,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp),
            )
        },
        title = {
            Text(
                "You reached ${level.label}!",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Text(
                "Great work staying consistent! ${level.description} " +
                    "New exercises now start from a heavier suggested weight, and you can adjust your level anytime in Settings.",
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Let's Go", fontWeight = FontWeight.Bold)
            }
        },
    )
}

/**
 * Modern Hero Plan Summary Card:
 * Features prominent program badge, clean schedule headline, segmented equipment toggle,
 * and clear focus callout.
 */
@Composable
fun PlanSummary(
    program: Split,
    headline: String,
    rotationText: String?,
    status: LevelStatus,
    noEquipment: Boolean,
    onNoEquipmentChange: (Boolean) -> Unit,
    active: ActiveDay,
    modifier: Modifier = Modifier,
    onChangePlan: (() -> Unit)? = null,
    onOpenCalendar: (() -> Unit)? = null,
) {
    Plate(
        modifier = modifier.fillMaxWidth(),
        accent = PlateAccent.EMBER,
        cornerRadius = 18.dp,
    ) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Program Header Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            RoundedCornerShape(10.dp),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Filled.FitnessCenter,
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
                        text = headline,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (rotationText != null) {
                TagPill(
                    text = rotationText,
                    color = MaterialTheme.colorScheme.secondary,
                    icon = Icons.Filled.SwapHoriz,
                )
            }

            // Quick Actions: Calendar & Plan Switcher
            if (onChangePlan != null || onOpenCalendar != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (onOpenCalendar != null) {
                        OutlinedButton(
                            onClick = onOpenCalendar,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        ) {
                            Icon(
                                Icons.Filled.CalendarMonth,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp).padding(end = 4.dp),
                            )
                            Text("Calendar", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                    if (onChangePlan != null) {
                        OutlinedButton(
                            onClick = onChangePlan,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        ) {
                            Icon(
                                Icons.Filled.SwapHoriz,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp).padding(end = 4.dp),
                            )
                            Text("Change plan", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))

            // Level & Equipment Mode Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(Modifier.weight(1f).padding(end = 8.dp)) {
                    Text(
                        text = levelText(status),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Modern Segmented Control for Equipment Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
                        RoundedCornerShape(10.dp),
                    )
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                val withEqSelected = !noEquipment
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (withEqSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                        )
                        .clickable { onNoEquipmentChange(false) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "With Plates",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (withEqSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (withEqSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                val noEqSelected = noEquipment
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (noEqSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                        )
                        .clickable { onNoEquipmentChange(true) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Bodyweight Only",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (noEqSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (noEqSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Day Focus Callout
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.35f),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Focus: " + PlanHelper.focusText(active.split, active.dayIndex),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (active.split == Split.CORE_DAY) {
                        Text(
                            "An optional extra for a rest day. It doesn't count toward your rest-day warnings or streak.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

/**
 * Modern scrollable day tabs with pill indicator.
 */
@Composable
fun PlanDayTabs(program: Split, selectedTab: Int, nextDay: Int, onSelect: (Int) -> Unit) {
    ScrollableTabRow(
        selectedTabIndex = selectedTab,
        edgePadding = 16.dp,
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.primary,
        indicator = { tabPositions ->
            if (selectedTab < tabPositions.size) {
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    height = 3.dp,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        },
        divider = {
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
        },
    ) {
        (program.dayNames + CORE_TAB_LABEL).forEachIndexed { index, name ->
            val isSelected = index == selectedTab
            val isNext = index == nextDay
            Tab(
                selected = isSelected,
                onClick = { onSelect(index) },
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = name,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        if (isNext) {
                            Box(
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape)
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                            ) {
                                Text(
                                    "Next",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
                },
            )
        }
    }
}

/**
 * Modern Program Picker Dialog with clean selection cards.
 */
@Composable
fun ProgramPickerDialog(current: Split, onPick: (Split) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Choose Your Training Plan",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    "Select based on your weekly availability. Full Body is ideal for 2-3 sessions, while the Split program fits 5-6 training days.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Split.programs.forEach { program ->
                    val selected = program == current
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onPick(program) }
                            .border(
                                width = if (selected) 2.dp else 1.dp,
                                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(14.dp),
                            ),
                        color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(
                                    text = program.label,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                )
                                if (selected) {
                                    Icon(
                                        Icons.Filled.CheckCircle,
                                        contentDescription = "Selected",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                            }
                            Text(
                                text = program.tagline,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            TagPill(
                                text = "Best for: ${program.bestFor}",
                                color = MaterialTheme.colorScheme.secondary,
                            )
                            Text(
                                text = "Schedule: ${program.schedule}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
