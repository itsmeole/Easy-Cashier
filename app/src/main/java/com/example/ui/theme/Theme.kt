package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = SleekDarkPrimary,
    secondary = SleekDarkSecondary,
    tertiary = SleekDarkTertiary,
    onTertiary = SleekOnSecondary, // Use dark color on SleekDarkTertiary light background
    background = SleekDarkBackground,
    surface = SleekDarkSurface,
    surfaceVariant = SleekDarkSurfaceVariant,
    onPrimary = SleekDarkOnPrimary,
    onSecondary = SleekDarkOnSecondary,
    onBackground = SleekDarkOnBackground,
    onSurface = SleekDarkOnSurface,
    outline = SleekDarkOutline
)

private val LightColorScheme = lightColorScheme(
    primary = SleekPrimary,
    secondary = SleekSecondary,
    tertiary = SleekTertiary,
    onTertiary = SleekOnPrimary, // Use light color on SleekTertiary dark background
    background = SleekBackground,
    surface = SleekSurface,
    surfaceVariant = SleekSurfaceVariant,
    onPrimary = SleekOnPrimary,
    onSecondary = SleekOnSecondary,
    onBackground = SleekOnBackground,
    onSurface = SleekOnSurface,
    outline = SleekOutline
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Keep consistent premium brand by default
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
