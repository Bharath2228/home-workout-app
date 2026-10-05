package com.bharath.homeforge.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Forge = Color(0xFFE4572E)

private val LightColors = lightColorScheme(
    primary = Color(0xFFB93A15),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDBD0),
    onPrimaryContainer = Color(0xFF3B0A00),
    secondary = Color(0xFF77574E),
    secondaryContainer = Color(0xFFFFDBD0),
    background = Color(0xFFFFFBFF),
    surface = Color(0xFFFFFBFF),
    surfaceVariant = Color(0xFFF5DED8),
)

private val DarkColors = darkColorScheme(
    primary = Forge,
    onPrimary = Color(0xFF3B0A00),
    primaryContainer = Color(0xFF8A2400),
    onPrimaryContainer = Color(0xFFFFDBD0),
    secondary = Color(0xFFE7BDB2),
    secondaryContainer = Color(0xFF5D4038),
    background = Color(0xFF1A1110),
    surface = Color(0xFF1A1110),
    surfaceVariant = Color(0xFF53433F),
)

@Composable
fun HomeForgeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content,
    )
}
