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
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bharath.homeforge.domain.Goal

@Composable
fun OnboardingScreen(onDone: (openSettings: Boolean, goal: Goal) -> Unit) {
    var goal by remember { mutableStateOf(Goal.MUSCLE_GAIN) }

    Surface(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Welcome to HomeForge", style = MaterialTheme.typography.headlineLarge)
            Text(
                "A workout planner and tracker built around the plates and rods you actually own.",
                style = MaterialTheme.typography.bodyLarge,
            )

            Section(
                "Weights you can really load",
                "Suggested weights are always ones you can build with your plates. Two rods can be loaded as a " +
                    "pair of dumbbells, or joined into one barbell. The barbell takes plates on both sides, " +
                    "so its weight moves in bigger steps than a dumbbell does.",
            )
            Section(
                "Plan, log, progress",
                "Pick a routine, log your sets, and the app suggests a heavier weight once you hit the top of " +
                    "the rep range on every set. Swap, reorder or add exercises whenever you like.",
            )
            Section(
                "Health Connect (optional)",
                "Send finished workouts to Health Connect from Settings. Calories are a rough estimate " +
                    "based on your body weight.",
            )

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

            Button(onClick = { onDone(true, goal) }, modifier = Modifier.fillMaxWidth()) {
                Text("Check my plates and rod weight")
            }
            OutlinedButton(onClick = { onDone(false, goal) }, modifier = Modifier.fillMaxWidth()) {
                Text("Start with the defaults")
            }
        }
    }
}

@Composable
private fun Section(title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Text(body, style = MaterialTheme.typography.bodyMedium)
    }
}
