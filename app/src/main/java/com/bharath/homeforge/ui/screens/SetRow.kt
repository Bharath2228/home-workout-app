package com.bharath.homeforge.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.SentimentDissatisfied
import androidx.compose.material.icons.filled.SentimentNeutral
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bharath.homeforge.domain.Difficulty
import com.bharath.homeforge.ui.SetDraft
import com.bharath.homeforge.ui.theme.ForgePalette

@Composable
fun SetRow(
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
