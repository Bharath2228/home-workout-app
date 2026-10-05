package com.bharath.homeforge.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.bharath.homeforge.domain.ExerciseMediaLibrary

@Composable
fun CreditsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val credits = remember { ExerciseMediaLibrary.all.entries.sortedBy { it.key } }

    Column(Modifier.fillMaxSize().padding(top = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text("Picture credits", style = MaterialTheme.typography.headlineMedium)
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    "The exercise pictures come from the wger.de exercise database and are used under " +
                        "Creative Commons Attribution-ShareAlike licenses (CC BY-SA 3.0 and 4.0, " +
                        "creativecommons.org/licenses/by-sa/4.0/). They were cropped and resized for this app. " +
                        "Tap an entry to open its source page.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            items(credits, key = { it.key }) { (exercise, media) ->
                Column(
                    Modifier
                        .fillMaxWidth()
                        .clickable {
                            runCatching {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(media.credit.sourceUrl)))
                            }
                        },
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(exercise, style = MaterialTheme.typography.titleSmall)
                    Text(
                        "${media.credit.author}, ${media.credit.license}",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}
