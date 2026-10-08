package com.bharath.homeforge.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** What a [Plate]'s top edge is naming: a working exercise, warm-up content, or plain information. */
enum class PlateAccent { EMBER, SPARK, NONE }

/**
 * HomeForge's one card motif, used everywhere a Material `Card` would otherwise appear: a lifted
 * surface with a soft shadow and a faint hairline edge, instead of a flat fill that blends into
 * the screen behind it. Content that's genuinely a working exercise or a warm-up gets a colored
 * top edge naming it as such; plain, same-as-its-neighbors content (a list row, a settings block)
 * stays quiet, so the accent reads as a signal instead of wallpaper.
 */
@Composable
fun Plate(
    modifier: Modifier = Modifier,
    accent: PlateAccent = PlateAccent.NONE,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
    val body: @Composable () -> Unit = {
        Column {
            if (accent != PlateAccent.NONE) {
                val accentColor = if (accent == PlateAccent.EMBER) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.secondary
                }
                Box(Modifier.fillMaxWidth().height(3.dp).background(accentColor))
            }
            content()
        }
    }
    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = modifier,
            shape = MaterialTheme.shapes.medium,
            color = containerColor,
            contentColor = contentColor,
            border = border,
            shadowElevation = 3.dp,
            content = body,
        )
    } else {
        Surface(
            modifier = modifier,
            shape = MaterialTheme.shapes.medium,
            color = containerColor,
            contentColor = contentColor,
            border = border,
            shadowElevation = 3.dp,
            content = body,
        )
    }
}
