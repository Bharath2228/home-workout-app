package com.bharath.homeforge.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bharath.homeforge.domain.Exercise
import com.bharath.homeforge.domain.ExerciseGuides
import com.bharath.homeforge.domain.ExerciseLibrary
import com.bharath.homeforge.domain.ExerciseMediaLibrary
import com.bharath.homeforge.domain.Rig
import com.bharath.homeforge.ui.theme.BackHeader
import com.bharath.homeforge.ui.theme.BulletDot
import com.bharath.homeforge.ui.theme.ForgePalette
import com.bharath.homeforge.ui.theme.Plate
import com.bharath.homeforge.ui.theme.StepBadge
import com.bharath.homeforge.ui.theme.TagPill

@Composable
fun ExerciseDetailScreen(name: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val exercise = remember(name) { ExerciseLibrary.all.find { it.name == name } }
    val guide = remember(name) { ExerciseGuides.forExercise(name) }
    val media = remember(name) { ExerciseMediaLibrary.forExercise(name) }

    Column(Modifier.fillMaxSize()) {
        BackHeader(name, onBack, modifier = Modifier.padding(top = 8.dp))

        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Movement Pattern & Equipment Tags
            if (exercise != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TagPill(
                        text = exercise.movement.label,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    TagPill(
                        text = equipmentText(exercise),
                        color = MaterialTheme.colorScheme.secondary,
                        icon = Icons.Filled.FitnessCenter,
                    )
                }
            }

            // Exercise Media / Images Card
            if (media != null) {
                Plate(
                    Modifier.fillMaxWidth(),
                    cornerRadius = 16.dp,
                ) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            color = Color.White,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                Modifier.padding(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                media.drawableNames.forEach { resourceName ->
                                    val id = remember(resourceName) {
                                        context.resources.getIdentifier(resourceName, "drawable", context.packageName)
                                    }
                                    if (id != 0) {
                                        Image(
                                            painter = painterResource(id),
                                            contentDescription = "$name form demo",
                                            contentScale = ContentScale.Fit,
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(210.dp),
                                        )
                                    }
                                }
                            }
                        }
                        Text(
                            text = "Photo credit: ${media.credit.author} (${media.credit.license}) via wger.de",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            // Step-by-Step Instructions
            if (guide != null) {
                Plate(Modifier.fillMaxWidth(), cornerRadius = 16.dp) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Execution Steps",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        guide.steps.forEachIndexed { index, step ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.Top,
                            ) {
                                StepBadge(index + 1)
                                Text(
                                    text = step,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                    }
                }

                // Form Cues Card (Emerald/Success Accent)
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = ForgePalette.Success.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, ForgePalette.Success.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .background(ForgePalette.Success.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(Icons.Filled.Check, contentDescription = null, tint = ForgePalette.Success, modifier = Modifier.size(16.dp))
                            }
                            Text(
                                "Key Form Cues",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = ForgePalette.Success,
                            )
                        }
                        guide.cues.forEach { cue ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.Top,
                            ) {
                                BulletDot(color = ForgePalette.Success)
                                Text(cue, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }

                // Common Mistakes Card (Warning Accent)
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(Icons.Filled.WarningAmber, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(16.dp))
                            }
                            Text(
                                "Common Mistakes to Avoid",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.tertiary,
                            )
                        }
                        guide.mistakes.forEach { mistake ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.Top,
                            ) {
                                BulletDot(color = MaterialTheme.colorScheme.tertiary)
                                Text(mistake, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }

            // Watch Demo Video CTA
            Button(
                onClick = { openDemoSearch(context, name) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            ) {
                Icon(
                    Icons.Filled.PlayCircle,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp).padding(end = 6.dp),
                )
                Text("Watch Video Demonstration", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }
        }
    }
}

private fun equipmentText(exercise: Exercise): String {
    val main = when (exercise.rig) {
        Rig.SINGLE_DUMBBELL -> "Single dumbbell"
        Rig.DUMBBELL_PAIR -> "Dumbbell pair"
        Rig.BARBELL -> "Barbell"
        null -> if (exercise.equipmentFree) "Bodyweight" else "Equipment-free"
    }
    return if (exercise.prop != null) "$main + ${exercise.prop}" else main
}

private fun openDemoSearch(context: Context, exerciseName: String) {
    val query = Uri.encode("$exerciseName exercise correct form tutorial")
    runCatching {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=$query")))
    }
}
