package com.bulktracker.app.ui.theme

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween

object Motion {
    const val FAST_MS = 100
    fun <T> fastTween() = tween<T>(durationMillis = FAST_MS, easing = LinearOutSlowInEasing)
}
