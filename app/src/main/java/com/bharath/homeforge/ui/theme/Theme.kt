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
 * HomeForge's identity is literal: a smithy. Dark mode is the hero ("the forge glows"), built
 * around a near-black charcoal surface, a hot-metal ember accent, and a second amber "spark" tone
 * reserved only for warm-up content. Light mode is a cooler "daylight forge" sibling, not an
 * inverted afterthought. Surfaces are flat and square-edged rather than soft and rounded.
 */
private object Forge {
    // Dark (hero)
    val charcoal = Color(0xFF17140F)
    val plate = Color(0xFF241F18)
    val plateHigh = Color(0xFF2E2820)
    val ember = Color(0xFFFF6A39)
    val onEmber = Color(0xFF330E00)
    val emberContainer = Color(0xFF55220B)
    val onEmberContainer = Color(0xFFFFD9C4)
    val spark = Color(0xFFE8B23D)
    val onSpark = Color(0xFF2B1C00)
    val sparkContainer = Color(0xFF4A3700)
    val onSparkContainer = Color(0xFFFFE6AC)
    val cinder = Color(0xFFEDE6DA)
    val ash = Color(0xFFC9C0B2)
    val hairline = Color(0xFF52493C)
    val rustError = Color(0xFFFF6B5B)
    // Quenched steel: the one cool tone, reserved for "pay attention" (warnings, today on the calendar).
    val quench = Color(0xFF8FC2D6)
    val onQuench = Color(0xFF003542)
    val quenchContainer = Color(0xFF1E4A58)
    val onQuenchContainer = Color(0xFFC4E7F2)

    // Light ("daylight forge"): a cool, neutral workshop grey, not a bakery cream.
    // White cards sit on it with real contrast, instead of two close shades of beige.
    val chalk = Color(0xFFF1F0ED)
    val chalkPlate = Color(0xFFFFFFFF)
    val chalkPlateHigh = Color(0xFFF6F5F1)
    val emberDay = Color(0xFFBA3B13)
    val onEmberDay = Color(0xFFFFFFFF)
    val emberContainerDay = Color(0xFFFFDBC9)
    val onEmberContainerDay = Color(0xFF3B0E00)
    val sparkDay = Color(0xFF8A5A00)
    val onSparkDay = Color(0xFFFFFFFF)
    val sparkContainerDay = Color(0xFFF2DFB3)
    val onSparkContainerDay = Color(0xFF2B1D00)
    val iron = Color(0xFF221E18)
    val stone = Color(0xFF615A4C)
    val hairlineDay = Color(0xFFE1DDD3)
    val rustErrorDay = Color(0xFFB3261E)
    val quenchDay = Color(0xFF2A5A68)
    val onQuenchDay = Color(0xFFFFFFFF)
    val quenchContainerDay = Color(0xFFC4E7F2)
    val onQuenchContainerDay = Color(0xFF071F26)
}

private val DarkColors = darkColorScheme(
    primary = Forge.ember,
    onPrimary = Forge.onEmber,
    primaryContainer = Forge.emberContainer,
    onPrimaryContainer = Forge.onEmberContainer,
    secondary = Forge.spark,
    onSecondary = Forge.onSpark,
    secondaryContainer = Forge.sparkContainer,
    onSecondaryContainer = Forge.onSparkContainer,
    tertiary = Forge.quench,
    onTertiary = Forge.onQuench,
    tertiaryContainer = Forge.quenchContainer,
    onTertiaryContainer = Forge.onQuenchContainer,
    background = Forge.charcoal,
    onBackground = Forge.cinder,
    surface = Forge.charcoal,
    onSurface = Forge.cinder,
    surfaceVariant = Forge.plate,
    onSurfaceVariant = Forge.ash,
    surfaceContainerHighest = Forge.plateHigh,
    outline = Forge.hairline,
    outlineVariant = Forge.hairline,
    error = Forge.rustError,
)

private val LightColors = lightColorScheme(
    primary = Forge.emberDay,
    onPrimary = Forge.onEmberDay,
    primaryContainer = Forge.emberContainerDay,
    onPrimaryContainer = Forge.onEmberContainerDay,
    secondary = Forge.sparkDay,
    onSecondary = Forge.onSparkDay,
    secondaryContainer = Forge.sparkContainerDay,
    onSecondaryContainer = Forge.onSparkContainerDay,
    tertiary = Forge.quenchDay,
    onTertiary = Forge.onQuenchDay,
    tertiaryContainer = Forge.quenchContainerDay,
    onTertiaryContainer = Forge.onQuenchContainerDay,
    background = Forge.chalk,
    onBackground = Forge.iron,
    surface = Forge.chalk,
    onSurface = Forge.iron,
    surfaceVariant = Forge.chalkPlate,
    onSurfaceVariant = Forge.stone,
    surfaceContainerHighest = Forge.chalkPlateHigh,
    outline = Forge.hairlineDay,
    outlineVariant = Forge.hairlineDay,
    error = Forge.rustErrorDay,
)

/**
 * Softened from the original hairline-square "stamped metal" corners, which read as dated rather
 * than deliberate once paired with flat, borderless surfaces. Still crisp, not pill-shaped.
 */
private val ForgeShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

private val baseline = Typography()

/**
 * Headlines run heavy and a little tighter than the Material default, like they were stamped
 * rather than typed. Body copy is left at the system default for legibility at a glance mid-set.
 */
private val ForgeTypography = Typography(
    displaySmall = baseline.displaySmall.copy(fontWeight = FontWeight.Black, letterSpacing = (-0.5).sp),
    headlineLarge = baseline.headlineLarge.copy(fontWeight = FontWeight.Black, letterSpacing = (-0.5).sp),
    headlineMedium = baseline.headlineMedium.copy(fontWeight = FontWeight.Black, letterSpacing = (-0.5).sp),
    headlineSmall = baseline.headlineSmall.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.25).sp),
    titleLarge = baseline.titleLarge.copy(fontWeight = FontWeight.Bold),
    titleMedium = baseline.titleMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.sp),
    titleSmall = baseline.titleSmall.copy(fontWeight = FontWeight.Bold),
    labelLarge = baseline.labelLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.2.sp),
)

/** Every number that matters (the live timer, rest countdown, a PR, a streak) is set as a gauge reading. */
val ReadoutTextStyle = TextStyle(
    fontFamily = FontFamily.Monospace,
    fontWeight = FontWeight.Bold,
    fontSize = 28.sp,
    letterSpacing = 0.5.sp,
)

val ReadoutTextStyleSmall = TextStyle(
    fontFamily = FontFamily.Monospace,
    fontWeight = FontWeight.Bold,
    fontSize = 18.sp,
    letterSpacing = 0.3.sp,
)

@Composable
fun HomeForgeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        typography = ForgeTypography,
        shapes = ForgeShapes,
        content = content,
    )
}
