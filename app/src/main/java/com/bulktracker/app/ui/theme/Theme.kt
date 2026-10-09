package com.bulktracker.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val LocalAppColorPalette = staticCompositionLocalOf { LocalAppLightColors }

object BulkTrackerTheme {
    val colors: AppColors
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColorPalette.current
}

private val LightColorScheme = lightColorScheme(
    primary = LightInk,
    onPrimary = LightOnInk,
    primaryContainer = LightSurface,
    onPrimaryContainer = LightTextPrimary,
    inversePrimary = LightOnInk,
    secondary = LightInk,
    onSecondary = LightOnInk,
    secondaryContainer = LightBorder,
    onSecondaryContainer = LightTextPrimary,
    tertiary = LightInk,
    onTertiary = LightOnInk,
    tertiaryContainer = LightSurface,
    onTertiaryContainer = LightTextPrimary,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightCard,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurface,
    onSurfaceVariant = LightTextSecondary,
    surfaceTint = Color.Transparent,
    inverseSurface = LightTextPrimary,
    inverseOnSurface = LightBackground,
    error = LightDanger,
    onError = Color(0xFFFFFFFF),
    errorContainer = LightDanger.copy(alpha = 0.15f),
    onErrorContainer = LightDanger,
    outline = LightBorder,
    outlineVariant = LightBorder,
    scrim = Color(0xFF000000),
    surfaceBright = LightCard,
    surfaceDim = LightSurface,
    surfaceContainerLowest = LightCard,
    surfaceContainerLow = LightCard,
    surfaceContainer = LightSurface,
    surfaceContainerHigh = LightSurface,
    surfaceContainerHighest = LightBorder
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkInk,
    onPrimary = DarkOnInk,
    primaryContainer = DarkSurface,
    onPrimaryContainer = DarkTextPrimary,
    inversePrimary = DarkOnInk,
    secondary = DarkInk,
    onSecondary = DarkOnInk,
    secondaryContainer = DarkBorder,
    onSecondaryContainer = DarkTextPrimary,
    tertiary = DarkInk,
    onTertiary = DarkOnInk,
    tertiaryContainer = DarkSurface,
    onTertiaryContainer = DarkTextPrimary,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkCard,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurface,
    onSurfaceVariant = DarkTextSecondary,
    surfaceTint = Color.Transparent,
    inverseSurface = DarkTextPrimary,
    inverseOnSurface = DarkBackground,
    error = DarkDanger,
    onError = Color(0xFFFFFFFF),
    errorContainer = DarkDanger.copy(alpha = 0.15f),
    onErrorContainer = DarkDanger,
    outline = DarkBorder,
    outlineVariant = DarkBorder,
    scrim = Color(0xFF000000),
    surfaceBright = DarkCard,
    surfaceDim = DarkSurface,
    surfaceContainerLowest = DarkCard,
    surfaceContainerLow = DarkCard,
    surfaceContainer = DarkSurface,
    surfaceContainerHigh = DarkSurface,
    surfaceContainerHighest = DarkBorder
)

@Composable
fun BulkTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val appColors = if (darkTheme) LocalAppDarkColors else LocalAppLightColors

    CompositionLocalProvider(
        LocalAppColorPalette provides appColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            content = content
        )
    }
}
