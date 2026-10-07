package com.bharath.homeforge.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bharath.homeforge.domain.Goal
import com.bharath.homeforge.domain.Level
import com.bharath.homeforge.domain.LevelProgress
import com.bharath.homeforge.domain.Split

@Composable
fun OnboardingScreen(onDone: (openSettings: Boolean, goal: Goal, program: Split, level: Level) -> Unit) {
    var level by remember { mutableStateOf(Level.BEGINNER) }
    val experienceLabels = listOf("New to lifting", "Trained on and off", "Train regularly for a year or more")
    var goal by remember { mutableStateOf(Goal.MUSCLE_GAIN) }
    var program by remember { mutableStateOf(Split.FULL_BODY) }
    val daysLabels = listOf("2 to 3 days a week", "3 to 4 days a week", "5 to 6 days a week")

    Surface(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Filled.Whatshot, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text("Welcome to HomeForge", style = MaterialTheme.typography.headlineLarge)
            }
            Text(
                "A workout planner and tracker built around the plates and rods you actually own.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Section(
                "Weights you can really load",
                "Suggested weights are always ones you can build with your plates. Two rods can be loaded as a " +
                    "pair of dumbbells, or joined into one barbell. The barbell takes plates on both sides, " +
                    "so its weight moves in bigger steps than a dumbbell does.",
            )
            Section(
                "Plan, log, progress",
                "Pick a plan, and the app tells you which workout is next. Log your sets, and it suggests a " +
                    "heavier weight once you hit the top of the rep range on every set. Swap, reorder or add " +
                    "exercises whenever you like.",
            )
            Section(
                "Health Connect (optional)",
                "Send finished workouts to Health Connect from Settings. Calories are a rough estimate " +
                    "based on your body weight.",
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("How much have you trained?", style = MaterialTheme.typography.titleMedium)
                Row(
                    Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Level.entries.forEachIndexed { index, option ->
                        FilterChip(
                            selected = option == level,
                            onClick = { level = option },
                            label = { Text(experienceLabels[index]) },
                        )
                    }
                }
                Text("${level.label}: ${level.description}", style = MaterialTheme.typography.bodyMedium)
                Text(
                    "You move up automatically as you complete workouts: " +
                        "${LevelProgress.INTERMEDIATE_AT} to reach Intermediate, ${LevelProgress.ADVANCED_AT} for Advanced.",
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("How many days a week can you train?", style = MaterialTheme.typography.titleMedium)
                Row(
                    Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Split.programs.forEachIndexed { index, option ->
                        FilterChip(
                            selected = option == program,
                            onClick = { program = option },
                            label = { Text(daysLabels[index]) },
                        )
                    }
                }
                Text("${program.label}: ${program.tagline}", style = MaterialTheme.typography.bodyMedium)
                Text("Best for: ${program.bestFor}", style = MaterialTheme.typography.bodySmall)
                Text("You can change your plan any time on the Routines tab.", style = MaterialTheme.typography.bodySmall)
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Your goal", style = MaterialTheme.typography.titleMedium)
                Row(
                    Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Goal.entries.forEach { option ->
                        FilterChip(
                            selected = option == goal,
                            onClick = { goal = option },
                            label = { Text(option.label) },
                        )
                    }
                }
                Text(goal.description, style = MaterialTheme.typography.bodyMedium)
                Text("You can change this later in Settings.", style = MaterialTheme.typography.bodySmall)
            }

            Button(onClick = { onDone(true, goal, program, level) }, modifier = Modifier.fillMaxWidth()) {
                Text("Check my plates")
            }
            OutlinedButton(onClick = { onDone(false, goal, program, level) }, modifier = Modifier.fillMaxWidth()) {
                Text("Start with the defaults")
            }
        }
    }
}

@Composable
private fun Section(title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
