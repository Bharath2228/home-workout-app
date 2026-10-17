package com.bharath.homeforge.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Modern Athletic Forge Design System:
 * Dark Mode: Premium deep obsidian slate with energetic athletic flame/ember accents,
 * vibrant solar sparks, and cool ice-quench highlights. High contrast, clean, and punchy.
 * Light Mode: Crisp titanium studio with pure white cards, subtle borders, and vivid accents.
 */
object ForgePalette {
    // Brand Accents
    val Flame = Color(0xFFFF5222)          // Energetic athletic ember / hyper-flame
    val FlameHover = Color(0xFFFF6A39)
    val FlameContainerDark = Color(0xFF33140C)
    val OnFlameContainerDark = Color(0xFFFFDACF)
    val FlameContainerLight = Color(0xFFFFEDE6)
    val OnFlameContainerLight = Color(0xFF410D02)

    val Solar = Color(0xFFFFB300)          // Bright amber spark for warm-up and streaks
    val SolarContainerDark = Color(0xFF332400)
    val OnSolarContainerDark = Color(0xFFFFE082)
    val SolarContainerLight = Color(0xFFFFF8E1)
    val OnSolarContainerLight = Color(0xFF5D4000)

    val Quench = Color(0xFF00C9FF)         // Crisp ice quench for schedule, recovery, cool highlights
    val QuenchContainerDark = Color(0xFF002B38)
    val OnQuenchContainerDark = Color(0xFFB3F0FF)
    val QuenchContainerLight = Color(0xFFE0F7FA)
    val OnQuenchContainerLight = Color(0xFF004D61)

    val Success = Color(0xFF10B981)        // Vibrant emerald for completed sets & PRs
    val SuccessContainerDark = Color(0xFF063321)
    val OnSuccessContainerDark = Color(0xFFA7F3D0)

    val RustError = Color(0xFFFF5252)
    val RustErrorLight = Color(0xFFD32F2F)

    // Dark Mode Neutrals (Obsidian Athletic)
    val Obsidian = Color(0xFF0E1117)       // Deep slate-black canvas
    val DarkSurface = Color(0xFF161B22)     // Elevated card background
    val DarkSurfaceHigh = Color(0xFF212833) // Higher lifted card background
    val DarkSurfaceHighest = Color(0xFF2C3442) // Inputs, toggles, badges
    val DarkTextPrimary = Color(0xFFF1F5F9)    // Crisp near-white
    val DarkTextSecondary = Color(0xFF94A3B8)  // Clean cool slate
    val DarkBorder = Color(0xFF27313F)         // Subtle structural boundary

    // Light Mode Neutrals (Titanium Studio)
    val PureWhite = Color(0xFFFFFFFF)
    val LightCanvas = Color(0xFFF8FAFC)        // Crisp soft slate-white canvas
    val LightSurfaceHigh = Color(0xFFF1F5F9)   // Soft pill & input container
    val LightSurfaceHighest = Color(0xFFE2E8F0)
    val LightTextPrimary = Color(0xFF0F172A)   // Deep charcoal slate
    val LightTextSecondary = Color(0xFF64748B) // Balanced slate secondary
    val LightBorder = Color(0xFFE2E8F0)        // Clean subtle card border
}

private val DarkColors = darkColorScheme(
    primary = ForgePalette.Flame,
    onPrimary = Color.White,
    primaryContainer = ForgePalette.FlameContainerDark,
    onPrimaryContainer = ForgePalette.OnFlameContainerDark,
    secondary = ForgePalette.Solar,
    onSecondary = Color(0xFF201600),
    secondaryContainer = ForgePalette.SolarContainerDark,
    onSecondaryContainer = ForgePalette.OnSolarContainerDark,
    tertiary = ForgePalette.Quench,
    onTertiary = Color(0xFF001F29),
    tertiaryContainer = ForgePalette.QuenchContainerDark,
    onTertiaryContainer = ForgePalette.OnQuenchContainerDark,
    background = ForgePalette.Obsidian,
    onBackground = ForgePalette.DarkTextPrimary,
    surface = ForgePalette.Obsidian,
    onSurface = ForgePalette.DarkTextPrimary,
    surfaceVariant = ForgePalette.DarkSurface,
    onSurfaceVariant = ForgePalette.DarkTextSecondary,
    surfaceContainerHighest = ForgePalette.DarkSurfaceHighest,
    outline = ForgePalette.DarkBorder,
    outlineVariant = ForgePalette.DarkBorder.copy(alpha = 0.6f),
    error = ForgePalette.RustError,
)

private val LightColors = lightColorScheme(
    primary = ForgePalette.Flame,
    onPrimary = Color.White,
    primaryContainer = ForgePalette.FlameContainerLight,
    onPrimaryContainer = ForgePalette.OnFlameContainerLight,
    secondary = Color(0xFFD97706),
    onSecondary = Color.White,
    secondaryContainer = ForgePalette.SolarContainerLight,
    onSecondaryContainer = ForgePalette.OnSolarContainerLight,
    tertiary = Color(0xFF0284C7),
    onTertiary = Color.White,
    tertiaryContainer = ForgePalette.QuenchContainerLight,
    onTertiaryContainer = ForgePalette.OnQuenchContainerLight,
    background = ForgePalette.LightCanvas,
    onBackground = ForgePalette.LightTextPrimary,
    surface = ForgePalette.LightCanvas,
    onSurface = ForgePalette.LightTextPrimary,
    surfaceVariant = ForgePalette.PureWhite,
    onSurfaceVariant = ForgePalette.LightTextSecondary,
    surfaceContainerHighest = ForgePalette.LightSurfaceHigh,
    outline = ForgePalette.LightBorder,
    outlineVariant = ForgePalette.LightBorder.copy(alpha = 0.8f),
    error = ForgePalette.RustErrorLight,
)

/**
 * Modern rounded shapes giving a smooth, elevated athletic look.
 */
private val ModernShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

private val baseline = Typography()

private val ForgeTypography = Typography(
    displaySmall = baseline.displaySmall.copy(fontWeight = FontWeight.Black, letterSpacing = (-0.5).sp),
    headlineLarge = baseline.headlineLarge.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.5).sp),
    headlineMedium = baseline.headlineMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = (-0.25).sp),
    headlineSmall = baseline.headlineSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = (-0.2).sp),
    titleLarge = baseline.titleLarge.copy(fontWeight = FontWeight.Bold),
    titleMedium = baseline.titleMedium.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 0.1.sp),
    titleSmall = baseline.titleSmall.copy(fontWeight = FontWeight.SemiBold),
    labelLarge = baseline.labelLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.3.sp),
    labelMedium = baseline.labelMedium.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 0.2.sp),
    bodyLarge = baseline.bodyLarge.copy(lineHeight = 22.sp),
    bodyMedium = baseline.bodyMedium.copy(lineHeight = 20.sp),
)

/** Modern Monospace digital gauge readout for timers, weights, and metrics */
val ReadoutTextStyle = TextStyle(
    fontFamily = FontFamily.Monospace,
    fontWeight = FontWeight.ExtraBold,
    fontSize = 28.sp,
    letterSpacing = 0.5.sp,
)

val ReadoutTextStyleSmall = TextStyle(
    fontFamily = FontFamily.Monospace,
    fontWeight = FontWeight.Bold,
    fontSize = 18.sp,
    letterSpacing = 0.3.sp,
)

val ReadoutTextStyleMedium = TextStyle(
    fontFamily = FontFamily.Monospace,
    fontWeight = FontWeight.Bold,
    fontSize = 22.sp,
    letterSpacing = 0.4.sp,
)

@Composable
fun HomeForgeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        typography = ForgeTypography,
        shapes = ModernShapes,
        content = content,
    )
}
