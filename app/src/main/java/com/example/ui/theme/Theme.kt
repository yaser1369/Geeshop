package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val GhoghnousColorScheme = darkColorScheme(
    primary = PhoenixOrange,
    onPrimary = TextPrimary,
    primaryContainer = Color(0xFF4A1F00),
    onPrimaryContainer = PhoenixGoldLight,
    secondary = GhoghnousCyan,
    onSecondary = Color(0xFF07151D),
    secondaryContainer = Color(0xFF094352),
    onSecondaryContainer = Color(0xFFB2F5EA),
    tertiary = PhoenixRed,
    onTertiary = TextPrimary,
    background = GhoghnousBg,
    onBackground = TextPrimary,
    surface = GhoghnousSurface,
    onSurface = TextPrimary,
    surfaceVariant = GhoghnousSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = GhoghnousBorder,
    error = GhoghnousError
)

@Composable
fun GhoghnousShopTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = GhoghnousColorScheme,
        typography = Typography,
        content = content
    )
}
