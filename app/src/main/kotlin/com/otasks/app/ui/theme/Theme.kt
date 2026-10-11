// SPDX-License-Identifier: GPL-3.0-or-later

package com.otasks.app.ui.theme
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

fun <T> appAnimation(
    mode: String,
    systemScale: Float,
): FiniteAnimationSpec<T> =
    if (mode == "OFF" ||
        systemScale == 0f
    ) {
        snap()
    } else {
        tween(Tokens.FAST_MS, easing = LinearOutSlowInEasing)
    }

fun <T> appSizeAnimation(
    mode: String,
    systemScale: Float,
): FiniteAnimationSpec<T> =
    if (mode != "NORMAL" ||
        systemScale == 0f
    ) {
        snap()
    } else {
        tween(Tokens.FAST_MS, easing = LinearOutSlowInEasing)
    }

@Composable
fun OTasksTheme(
    dark: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (dark) DarkColors else LightColors
    CompositionLocalProvider(LocalAppColors provides colors) {
        MaterialTheme(colorScheme = colors.materialScheme(), typography = AppTypography, content = content)
    }
}
