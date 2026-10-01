package com.z1fire.alma.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily

// Collegiate palette: navy, oxblood, old gold on parchment.
private val Light = lightColorScheme(
    primary = Color(0xFF1F3A5F),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD5E2F5),
    onPrimaryContainer = Color(0xFF0A1D35),
    secondary = Color(0xFF7A2232),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF5D9DC),
    onSecondaryContainer = Color(0xFF3B0A13),
    tertiary = Color(0xFF7D6200),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF6E3A6),
    onTertiaryContainer = Color(0xFF2A2000),
    background = Color(0xFFFBF8F1),
    onBackground = Color(0xFF1C1B17),
    surface = Color(0xFFFBF8F1),
    onSurface = Color(0xFF1C1B17),
    surfaceVariant = Color(0xFFECE5D5),
    onSurfaceVariant = Color(0xFF4C4639),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF7F2E7),
    surfaceContainer = Color(0xFFF2ECDF),
    surfaceContainerHigh = Color(0xFFEDE6D7),
    surfaceContainerHighest = Color(0xFFE7E0D0),
    outline = Color(0xFF7D7666),
    outlineVariant = Color(0xFFCFC6B4),
    error = Color(0xFFB3261E),
)

private val Dark = darkColorScheme(
    primary = Color(0xFFA9C7F0),
    onPrimary = Color(0xFF0D2744),
    primaryContainer = Color(0xFF26446B),
    onPrimaryContainer = Color(0xFFD5E2F5),
    secondary = Color(0xFFF0B3BC),
    onSecondary = Color(0xFF4F1220),
    secondaryContainer = Color(0xFF6A2030),
    onSecondaryContainer = Color(0xFFF5D9DC),
    tertiary = Color(0xFFE3C565),
    onTertiary = Color(0xFF3B2F00),
    tertiaryContainer = Color(0xFF5A4700),
    onTertiaryContainer = Color(0xFFF6E3A6),
    background = Color(0xFF14171C),
    onBackground = Color(0xFFE6E2D9),
    surface = Color(0xFF14171C),
    onSurface = Color(0xFFE6E2D9),
    surfaceVariant = Color(0xFF3A3F47),
    onSurfaceVariant = Color(0xFFC4C6CC),
    surfaceContainerLowest = Color(0xFF0F1216),
    surfaceContainerLow = Color(0xFF1A1E24),
    surfaceContainer = Color(0xFF1E2229),
    surfaceContainerHigh = Color(0xFF252A31),
    surfaceContainerHighest = Color(0xFF2F343C),
    outline = Color(0xFF8E9199),
    outlineVariant = Color(0xFF41464E),
)

private val base = Typography()
private val Serif = FontFamily.Serif
private val AppTypography = Typography(
    displayLarge = base.displayLarge.copy(fontFamily = Serif),
    displayMedium = base.displayMedium.copy(fontFamily = Serif),
    displaySmall = base.displaySmall.copy(fontFamily = Serif),
    headlineLarge = base.headlineLarge.copy(fontFamily = Serif),
    headlineMedium = base.headlineMedium.copy(fontFamily = Serif),
    headlineSmall = base.headlineSmall.copy(fontFamily = Serif),
    titleLarge = base.titleLarge.copy(fontFamily = Serif),
)

val Gold = Color(0xFFC9A227)

/** Department accent colors, picked by index. */
val DeptColors = listOf(
    Color(0xFF2E5C8A), // blue
    Color(0xFF8C2F39), // oxblood
    Color(0xFF2F7A57), // green
    Color(0xFFB0782A), // amber
    Color(0xFF6B4C9A), // violet
    Color(0xFF1F8A8A), // teal
    Color(0xFFB04F7A), // rose
    Color(0xFF5B6B2E), // olive
    Color(0xFF8A5A3C), // brown
    Color(0xFF4A5568), // slate
)

fun deptColor(index: Int?): Color = DeptColors[((index ?: 9) % DeptColors.size + DeptColors.size) % DeptColors.size]

@Composable
fun AlmaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) Dark else Light,
        typography = AppTypography,
        content = content,
    )
}
