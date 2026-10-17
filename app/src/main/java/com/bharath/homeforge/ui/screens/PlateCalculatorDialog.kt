package com.bharath.homeforge.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bharath.homeforge.domain.Equipment
import com.bharath.homeforge.domain.LoadCalculator
import com.bharath.homeforge.domain.PlateLoader
import com.bharath.homeforge.domain.Rig
import com.bharath.homeforge.ui.formatKg
import com.bharath.homeforge.ui.theme.Plate
import com.bharath.homeforge.ui.theme.ReadoutTextStyle
import com.bharath.homeforge.ui.theme.TagPill

@Composable
fun PlateCalculatorDialog(
    equipment: Equipment,
    rig: Rig,
    initialWeightKg: Double,
    onDismiss: () -> Unit,
) {
    val achievable = remember(equipment, rig) {
        LoadCalculator.achievableWeights(equipment, rig)
    }
    var currentWeight by remember(initialWeightKg) {
        mutableDoubleStateOf(if (initialWeightKg in achievable) initialWeightKg else achievable.firstOrNull() ?: initialWeightKg)
    }

    val plates = remember(equipment, rig, currentWeight) {
        PlateLoader.platesFor(equipment, rig, currentWeight)
    }

    val description = remember(equipment, rig, currentWeight) {
        PlateLoader.describe(equipment, rig, currentWeight)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Filled.FitnessCenter,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp),
                    )
                }
                Column {
                    Text(
                        "Plate Loading Visualizer",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        rig.label,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Weight Stepper
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    val currentIndex = achievable.indexOf(currentWeight)
                    IconButton(
                        onClick = {
                            if (currentIndex > 0) currentWeight = achievable[currentIndex - 1]
                        },
                        enabled = currentIndex > 0,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        ),
                    ) {
                        Icon(Icons.Filled.Remove, contentDescription = "Decrease load")
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${formatKg(currentWeight)} kg",
                            style = ReadoutTextStyle,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            text = if (rig == Rig.DUMBBELL_PAIR) "per dumbbell" else "total load",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    IconButton(
                        onClick = {
                            if (currentIndex < achievable.lastIndex) currentWeight = achievable[currentIndex + 1]
                        },
                        enabled = currentIndex < achievable.lastIndex,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        ),
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Increase load")
                    }
                }

                // VISUAL BAR SLEEVE GRAPHIC
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.35f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        // Barbell shaft line
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .background(Color(0xFF64748B), RoundedCornerShape(5.dp))
                        )

                        // Bar collar stopper on the left
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.align(Alignment.CenterStart),
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(14.dp)
                                    .height(70.dp)
                                    .background(Color(0xFF334155), RoundedCornerShape(3.dp))
                            )

                            Spacer(Modifier.width(4.dp))

                            // Stacked Plates on Sleeve
                            if (plates.isNullOrEmpty()) {
                                Text(
                                    "No plates needed\n(Bar weight only)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(start = 24.dp),
                                )
                            } else {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    plates.forEach { plateKg ->
                                        VisualPlateDisc(plateKg)
                                    }
                                }
                            }
                        }
                    }
                }

                // Text Breakdown Description
                if (description != null) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    ) {
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done", fontWeight = FontWeight.Bold)
            }
        },
    )
}

/**
 * A visually styled Olympic / fitness plate disc with proportional height and colors.
 */
@Composable
private fun VisualPlateDisc(weightKg: Double) {
    val (height, color, textColor) = when {
        weightKg >= 20.0 -> Triple(95.dp, Color(0xFF3B82F6), Color.White)    // Blue 20kg
        weightKg >= 15.0 -> Triple(85.dp, Color(0xFFEAB308), Color.Black)    // Yellow 15kg
        weightKg >= 10.0 -> Triple(75.dp, Color(0xFF10B981), Color.White)    // Green 10kg
        weightKg >= 5.0 -> Triple(62.dp, Color(0xFFE2E8F0), Color.Black)     // White 5kg
        weightKg >= 2.5 -> Triple(50.dp, Color(0xFF1E293B), Color.White)     // Black 2.5kg
        weightKg >= 1.25 -> Triple(40.dp, Color(0xFF00C9FF), Color.Black)    // Cyan 1.25kg
        else -> Triple(32.dp, Color(0xFF94A3B8), Color.Black)                // Silver fractional
    }

    val width = when {
        weightKg >= 15.0 -> 20.dp
        weightKg >= 5.0 -> 16.dp
        else -> 12.dp
    }

    Box(
        modifier = Modifier
            .width(width)
            .height(height)
            .clip(RoundedCornerShape(3.dp))
            .background(color)
            .border(1.dp, Color.Black.copy(alpha = 0.25f), RoundedCornerShape(3.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = formatKg(weightKg),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold),
            color = textColor,
            maxLines = 1,
        )
    }
}
