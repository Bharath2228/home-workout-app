package com.bharath.homeforge.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bharath.homeforge.domain.Equipment
import com.bharath.homeforge.domain.PlateLoader
import com.bharath.homeforge.domain.Rig
import com.bharath.homeforge.domain.WarmUp
import com.bharath.homeforge.domain.WarmUpSet
import com.bharath.homeforge.ui.ExerciseDraft
import com.bharath.homeforge.ui.formatKg
import com.bharath.homeforge.ui.theme.ForgePalette
import com.bharath.homeforge.ui.theme.Plate
import com.bharath.homeforge.ui.theme.PlateAccent
import com.bharath.homeforge.ui.theme.TagPill

@Composable
fun ExerciseLog(
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
    onShowPlates: (Rig, Double) -> Unit,
    onAddWarmUpSets: (List<WarmUpSet>) -> Unit,
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
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { onShowPlates(rig!!, firstWeight!!) },
                            ) {
                                Row(
                                    Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Icon(
                                        Icons.Filled.FitnessCenter,
                                        contentDescription = "View plates",
                                        modifier = Modifier.size(11.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Text(
                                        text = plateText,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        } else if (rig != null && firstWeight != null) {
                            Text(
                                "Weight cannot be built with your current plates.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }

                        if (warmUps.isNotEmpty()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Text(
                                    "Warm-up: " + warmUps.joinToString(", ") { "${formatKg(it.weightKg)} kg × ${it.reps}" },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.weight(1f),
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable { onAddWarmUpSets(warmUps) },
                                ) {
                                    Text(
                                        "+ Add",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.secondary,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    )
                                }
                            }
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

internal fun collapsedSummary(exercise: ExerciseDraft, unit: String): String {
    val doneCount = exercise.sets.count { it.done }
    val lastDone = exercise.sets.lastOrNull { it.done }
    val detail = when {
        lastDone == null -> null
        lastDone.weight.isNotBlank() -> "${lastDone.weight} kg × ${lastDone.reps} $unit"
        else -> "${lastDone.reps} $unit"
    }
    return "$doneCount of ${exercise.sets.size} sets done" + if (detail != null) " • $detail" else ""
}
