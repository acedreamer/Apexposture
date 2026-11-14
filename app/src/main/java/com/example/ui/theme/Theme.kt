package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val OledDarkColorScheme = darkColorScheme(
    primary = EmeraldPrimary,
    onPrimary = Color(0xFF022C22),
    primaryContainer = EmeraldContainer,
    onPrimaryContainer = OnEmeraldContainer,
    secondary = AmberAccent,
    onSecondary = Color(0xFF1F1200),
    secondaryContainer = AmberContainer,
    onSecondaryContainer = OnAmberContainer,
    tertiary = CyanAccent,
    onTertiary = Color(0xFF00212B),
    tertiaryContainer = CyanContainer,
    onTertiaryContainer = OnCyanContainer,
    background = OledBackground,
    onBackground = TextPrimary,
    surface = OledSurface,
    onSurface = TextPrimary,
    surfaceVariant = OledSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    surfaceContainer = OledSurface,
    surfaceContainerHigh = OledSurfaceVariant,
    surfaceContainerHighest = OledSurfaceElevated,
    outline = OledCardBorder,
    outlineVariant = Color(0xFF1B263B),
    error = CoralWarning,
    onError = Color.White,
    errorContainer = CoralContainer,
    onErrorContainer = Color(0xFFFFE4E6)
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    // Strictly enforce the sleek high-contrast OLED Dark Theme requested by the user
    MaterialTheme(
        colorScheme = OledDarkColorScheme,
        typography = Typography,
        content = content
    )
}
