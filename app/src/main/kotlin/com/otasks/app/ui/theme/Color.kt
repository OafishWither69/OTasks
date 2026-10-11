// SPDX-License-Identifier: GPL-3.0-or-later

package com.otasks.app.ui.theme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import kotlin.math.max
import kotlin.math.min

@Immutable
data class AppColors(
    val background: Color,
    val surface: Color,
    val card: Color,
    val border: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val ink: Color,
    val onInk: Color,
    val accent: Color,
    val success: Color,
    val successText: Color,
    val warning: Color,
    val warningText: Color,
    val danger: Color,
    val dangerText: Color,
) {
    val onAccent: Color get() = onColor(accent)
}

val LightColors =
    AppColors(
        Color(0xFFFAF9F5),
        Color(0xFFF0EEE6),
        Color.White,
        Color(0xFFE8E6DC),
        Color(0xFF141413),
        Color(0xFF4D4C48),
        Color(0xFF696862),
        Color(0xFF141413),
        Color(0xFFFAF9F5),
        Color(0xFFC96442),
        Color(0xFF15803D),
        Color(0xFF166534),
        Color(0xFFB85C08),
        Color(0xFF92400E),
        Color(0xFFDC2626),
        Color(0xFFB91C1C),
    )
val DarkColors =
    AppColors(
        Color(0xFF262624),
        Color(0xFF1F1E1D),
        Color(0xFF30302E),
        Color(0xFF3D3D3A),
        Color(0xFFFAF9F5),
        Color(0xFFC2C0B6),
        Color(0xFF9C9A92),
        Color(0xFFFAF9F5),
        Color(0xFF141413),
        Color(0xFFD97757),
        Color(0xFF4ADE80),
        Color(0xFF4ADE80),
        Color(0xFFFBBF24),
        Color(0xFFFBBF24),
        Color(0xFFF87171),
        Color(0xFFF87171),
    )
val AccentLight = listOf(0xFFC96442, 0xFF2F6FB2, 0xFF3F7D4E, 0xFF8A4FA8, 0xFFB5487A, 0xFFB7791F, 0xFF57534E).map(::Color)
val AccentDark = listOf(0xFFD97757, 0xFF6AA6E8, 0xFF6FBF7F, 0xFFB98BD6, 0xFFE88AB4, 0xFFE6B04F, 0xFFA8A29E).map(::Color)
val ItemLight = listOf(0xFF1D4ED8, 0xFF0F766E, 0xFFA84A0C, 0xFF6D28D9, 0xFF881337, 0xFFBE185D, 0xFF166534, 0xFF806000).map(::Color)
val ItemDark = listOf(0xFF60A5FA, 0xFF2DD4BF, 0xFFFB923C, 0xFFA78BFA, 0xFFFDA4AF, 0xFFF472B6, 0xFF4ADE80, 0xFFFACC15).map(::Color)

fun contrast(
    a: Color,
    b: Color,
): Double = (max(a.luminance(), b.luminance()) + 0.05) / (min(a.luminance(), b.luminance()) + 0.05)

fun onColor(c: Color): Color = if (contrast(c, Color(0xFF141413)) >= contrast(c, Color.White)) Color(0xFF141413) else Color.White

fun readableText(
    text: Color,
    background: Color,
): Color {
    if (contrast(text, background) >= 4.5) return text
    val end = onColor(background)
    for (i in 1..100) {
        val t = i / 100f
        val adjusted =
            Color(text.red + (end.red - text.red) * t, text.green + (end.green - text.green) * t, text.blue + (end.blue - text.blue) * t)
        if (contrast(adjusted, background) >= 4.5) return adjusted
    }
    return end
}

fun AppColors.materialScheme(): ColorScheme {
    val errorContainer = danger.copy(alpha = 0.15f).compositeOver(card)
    return lightColorScheme(
        primary = ink,
        onPrimary = onInk,
        primaryContainer = surface,
        onPrimaryContainer = textPrimary,
        inversePrimary = onInk,
        secondary = ink,
        onSecondary = onInk,
        secondaryContainer = border,
        onSecondaryContainer = textPrimary,
        tertiary = accent,
        onTertiary = onAccent,
        tertiaryContainer = surface,
        onTertiaryContainer = textPrimary,
        background = background,
        onBackground = textPrimary,
        surface = card,
        onSurface = textPrimary,
        surfaceVariant = surface,
        onSurfaceVariant = textSecondary,
        surfaceTint = Color.Transparent,
        inverseSurface = textPrimary,
        inverseOnSurface = background,
        error = dangerText,
        onError = onColor(dangerText),
        errorContainer = errorContainer,
        onErrorContainer = readableText(dangerText, errorContainer),
        outline = border,
        outlineVariant = border,
        scrim = Color.Black,
        surfaceBright = card,
        surfaceDim = surface,
        surfaceContainerLowest = card,
        surfaceContainerLow = card,
        surfaceContainer = surface,
        surfaceContainerHigh = surface,
        surfaceContainerHighest = surface,
    )
}

val LocalAppColors = staticCompositionLocalOf { LightColors }
