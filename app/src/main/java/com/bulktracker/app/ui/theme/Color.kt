package com.bulktracker.app.ui.theme

import androidx.compose.ui.graphics.Color

// Light Theme Base
val LightBackground = Color(0xFFFAF9F5)
val LightSurface = Color(0xFFF0EEE6)
val LightCard = Color(0xFFFFFFFF)
val LightBorder = Color(0xFFE8E6DC)
val LightTextPrimary = Color(0xFF141413)
val LightTextSecondary = Color(0xFF4D4C48)
val LightTextTertiary = Color(0xFF696862)
val LightTrack = Color(0xFFE8E6DC)
val LightInk = Color(0xFF141413)
val LightOnInk = Color(0xFFFAF9F5)
val LightAccent = Color(0xFFC96442)

// Dark Theme Base
val DarkBackground = Color(0xFF262624)
val DarkSurface = Color(0xFF1F1E1D)
val DarkCard = Color(0xFF30302E)
val DarkBorder = Color(0xFF3D3D3A)
val DarkTextPrimary = Color(0xFFFAF9F5)
val DarkTextSecondary = Color(0xFFC2C0B6)
val DarkTextTertiary = Color(0xFF9C9A92)
val DarkTrack = Color(0xFF3D3D3A)
val DarkInk = Color(0xFFFAF9F5)
val DarkOnInk = Color(0xFF141413)
val DarkAccent = Color(0xFFD97757)

// Common Accent Text / Icon
val OnAccentText = Color(0xFF141413)
val OnAccentIcon = Color(0xFFFFFFFF)

// Light Theme Accents
val LightSuccess = Color(0xFF16A34A)
val LightWarning = Color(0xFFD97706)
val LightWarningText = Color(0xFFB45309)
val LightDanger = Color(0xFFDC2626)
val LightPriorityStar = Color(0xFFD97706)
val LightNowLine = Color(0xFFDC2626)

// Dark Theme Accents
val DarkSuccess = Color(0xFF4ADE80)
val DarkWarning = Color(0xFFFBBF24)
val DarkWarningText = Color(0xFFFBBF24)
val DarkDanger = Color(0xFFF87171)
val DarkPriorityStar = Color(0xFFFBBF24)
val DarkNowLine = Color(0xFFF87171)

data class AppColors(
    val background: Color,
    val surface: Color,
    val card: Color,
    val border: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val track: Color,
    val ink: Color,
    val onInk: Color,
    val accent: Color,
    val onAccentText: Color,
    val onAccentIcon: Color,
    val success: Color,
    val warning: Color,
    val warningText: Color,
    val danger: Color,
    val priorityStar: Color,
    val nowLine: Color
)

val LocalAppLightColors = AppColors(
    background = LightBackground,
    surface = LightSurface,
    card = LightCard,
    border = LightBorder,
    textPrimary = LightTextPrimary,
    textSecondary = LightTextSecondary,
    textTertiary = LightTextTertiary,
    track = LightTrack,
    ink = LightInk,
    onInk = LightOnInk,
    accent = LightAccent,
    onAccentText = OnAccentText,
    onAccentIcon = OnAccentIcon,
    success = LightSuccess,
    warning = LightWarning,
    warningText = LightWarningText,
    danger = LightDanger,
    priorityStar = LightPriorityStar,
    nowLine = LightNowLine
)

val LocalAppDarkColors = AppColors(
    background = DarkBackground,
    surface = DarkSurface,
    card = DarkCard,
    border = DarkBorder,
    textPrimary = DarkTextPrimary,
    textSecondary = DarkTextSecondary,
    textTertiary = DarkTextTertiary,
    track = DarkTrack,
    ink = DarkInk,
    onInk = DarkOnInk,
    accent = DarkAccent,
    onAccentText = OnAccentText,
    onAccentIcon = OnAccentIcon,
    success = DarkSuccess,
    warning = DarkWarning,
    warningText = DarkWarningText,
    danger = DarkDanger,
    priorityStar = DarkPriorityStar,
    nowLine = DarkNowLine
)
