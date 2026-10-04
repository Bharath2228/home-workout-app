package com.bharath.homeforge.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bharath.homeforge.data.SessionPoint
import com.bharath.homeforge.data.buildHistory
import com.bharath.homeforge.ui.ProgressViewModel
import com.bharath.homeforge.ui.formatKg
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProgressScreen(vm: ProgressViewModel = viewModel()) {
    val sets by vm.sets.collectAsStateWithLifecycle()
    val sessionCount by vm.sessionCount.collectAsStateWithLifecycle()
    val history = remember(sets) { buildHistory(sets) }
    val names = remember(history) { history.keys.sorted() }
    var selected by rememberSaveable { mutableStateOf<String?>(null) }
    val current = selected?.takeIf { it in history } ?: names.firstOrNull()

    Column(Modifier.fillMaxSize().padding(top = 16.dp)) {
        Text(
            "Progress",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        Text(
            "$sessionCount workouts logged",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        )

        if (current == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Log a workout to see your progress here.")
            }
            return@Column
        }

        LazyRow(
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(names) { name ->
                FilterChip(selected = name == current, onClick = { selected = name }, label = { Text(name) })
            }
        }

        val points = history.getValue(current)
        val weighted = points.any { it.topWeightKg != null }
        val values = points.map { (if (weighted) it.topWeightKg ?: 0.0 else it.totalReps.toDouble()).toFloat() }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            if (weighted) "Top weight per workout (kg)" else "Total reps per workout",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        LineChart(values)
                    }
                }
            }
            items(points.reversed()) { point -> SessionRow(point, weighted) }
        }
    }
}

@Composable
private fun LineChart(values: List<Float>) {
    val color = MaterialTheme.colorScheme.primary
    Canvas(Modifier.fillMaxWidth().height(160.dp)) {
        if (values.isEmpty()) return@Canvas
        val min = values.min()
        val max = values.max()
        val range = if (max - min < 1e-6f) 1f else max - min
        val pad = 12.dp.toPx()
        val w = size.width - 2 * pad
        val h = size.height - 2 * pad

        fun point(i: Int): Offset {
            val x = if (values.size == 1) size.width / 2 else pad + w * i / (values.size - 1)
            val y = if (max - min < 1e-6f) size.height / 2 else pad + h * (1 - (values[i] - min) / range)
            return Offset(x, y)
        }

        if (values.size > 1) {
            val path = Path()
            values.indices.forEach { i ->
                val p = point(i)
                if (i == 0) path.moveTo(p.x, p.y) else path.lineTo(p.x, p.y)
            }
            drawPath(path, color, style = Stroke(width = 3.dp.toPx()))
        }
        values.indices.forEach { drawCircle(color, radius = 5.dp.toPx(), center = point(it)) }
    }
}

@Composable
private fun SessionRow(point: SessionPoint, weighted: Boolean) {
    val date = remember(point.time) {
        SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(point.time))
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(date)
        Text(
            if (weighted) {
                "top ${formatKg(point.topWeightKg ?: 0.0)} kg, volume ${formatKg(point.volumeKg)} kg"
            } else {
                "${point.totalReps} reps"
            },
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
