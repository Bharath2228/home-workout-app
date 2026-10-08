package com.bharath.homeforge.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.bharath.homeforge.domain.Exercise
import com.bharath.homeforge.domain.ExerciseGuides
import com.bharath.homeforge.domain.ExerciseLibrary
import com.bharath.homeforge.domain.ExerciseMediaLibrary
import com.bharath.homeforge.domain.Rig
import com.bharath.homeforge.ui.theme.BackHeader
import com.bharath.homeforge.ui.theme.BulletDot
import com.bharath.homeforge.ui.theme.StepBadge

@Composable
fun ExerciseDetailScreen(name: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val exercise = remember(name) { ExerciseLibrary.all.find { it.name == name } }
    val guide = remember(name) { ExerciseGuides.forExercise(name) }
    val media = remember(name) { ExerciseMediaLibrary.forExercise(name) }

    Column(Modifier.fillMaxSize().padding(top = 8.dp)) {
        BackHeader(name, onBack)

        Column(
            Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            if (exercise != null) {
                Text(
                    "${exercise.movement.label}. You need: ${equipmentText(exercise)}.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (media != null) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        color = Color.White,
                        shape = MaterialTheme.shapes.medium,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(Modifier.padding(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            media.drawableNames.forEach { resourceName ->
                                val id = remember(resourceName) {
                                    context.resources.getIdentifier(resourceName, "drawable", context.packageName)
                                }
                                if (id != 0) {
                                    Image(
                                        painter = painterResource(id),
                                        contentDescription = "$name demonstration",
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier.weight(1f).height(220.dp),
                                    )
                                }
                            }
                        }
                    }
                    Text(
                        "Picture by ${media.credit.author}, ${media.credit.license}, from wger.de.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                Text(
                    "No picture for this one yet. Use the video demo below.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (guide != null) {
                Section("How to do it") {
                    guide.steps.forEachIndexed { index, step ->
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
                            StepBadge(index + 1)
                            Text(step, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Section("Form cues") { guide.cues.forEach { Bulleted(it) } }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Section("Common mistakes") { guide.mistakes.forEach { Bulleted(it) } }
            }

            if (exercise?.rig != null) {
                Text(
                    "Safety: lock your plates on both ends before every set, and stop if you feel sharp pain.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Button(onClick = { openDemoSearch(context, name) }, modifier = Modifier.fillMaxWidth()) {
                Text("Watch a demo video")
            }
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        content()
    }
}

@Composable
private fun Bulleted(text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        BulletDot()
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}

private fun equipmentText(exercise: Exercise): String {
    val main = when (exercise.rig) {
        Rig.SINGLE_DUMBBELL -> "one dumbbell"
        Rig.DUMBBELL_PAIR -> "a pair of dumbbells"
        Rig.BARBELL -> "the joined bar"
        null -> if (exercise.equipmentFree) "just your body" else "the bar from your set"
    }
    return if (exercise.prop != null) "$main and ${exercise.prop}" else main
}

private fun openDemoSearch(context: Context, exerciseName: String) {
    val query = Uri.encode("$exerciseName exercise form")
    runCatching {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=$query")))
    }
}
