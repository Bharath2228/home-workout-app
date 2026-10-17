package com.bharath.homeforge.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** What a [Plate]'s top edge is naming: a working exercise, warm-up content, or plain information. */
enum class PlateAccent { EMBER, SPARK, QUENCH, SUCCESS, NONE }

/**
 * Modern Plate card component:
 * Elevates content with crisp modern rounded corners (16.dp), subtle high-precision borders,
 * and elegant accent indicators (smooth gradient accent line or soft ambient tint).
 */
@Composable
fun Plate(
    modifier: Modifier = Modifier,
    accent: PlateAccent = PlateAccent.NONE,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    cornerRadius: Dp = 16.dp,
    elevation: Dp = 2.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(cornerRadius)
    val border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

    val accentColor = when (accent) {
        PlateAccent.EMBER -> MaterialTheme.colorScheme.primary
        PlateAccent.SPARK -> MaterialTheme.colorScheme.secondary
        PlateAccent.QUENCH -> MaterialTheme.colorScheme.tertiary
        PlateAccent.SUCCESS -> ForgePalette.Success
        PlateAccent.NONE -> Color.Transparent
    }

    val body: @Composable () -> Unit = {
        Column {
            if (accent != PlateAccent.NONE) {
                // Sleek gradient top accent bar that tapers smoothly
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.5.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    accentColor,
                                    accentColor.copy(alpha = 0.85f),
                                    accentColor.copy(alpha = 0.4f),
                                )
                            )
                        )
                )
            }
            content()
        }
    }

    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = modifier.clip(shape),
            shape = shape,
            color = containerColor,
            contentColor = contentColor,
            border = border,
            shadowElevation = elevation,
            content = body,
        )
    } else {
        Surface(
            modifier = modifier.clip(shape),
            shape = shape,
            color = containerColor,
            contentColor = contentColor,
            border = border,
            shadowElevation = elevation,
            content = body,
        )
    }
}
