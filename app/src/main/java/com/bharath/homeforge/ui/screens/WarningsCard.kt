package com.bharath.homeforge.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bharath.homeforge.ui.theme.Plate
import com.bharath.homeforge.ui.theme.PlateAccent

@Composable
fun WarningsCard(warnings: List<String>, modifier: Modifier = Modifier) {
    if (warnings.isEmpty()) return
    Plate(modifier.fillMaxWidth(), accent = PlateAccent.NONE) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Heads up", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.tertiary)
            warnings.forEach { Text(it, style = MaterialTheme.typography.bodyMedium) }
        }
    }
}
