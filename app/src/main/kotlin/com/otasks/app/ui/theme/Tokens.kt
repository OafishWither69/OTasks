// SPDX-License-Identifier: GPL-3.0-or-later

package com.otasks.app.ui.theme
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object Tokens {
    object Space {
        val xxs = 2.dp
        val xs = 4.dp
        val sm = 8.dp
        val md = 12.dp
        val lg = 16.dp
        val xl = 24.dp
        val xxl = 32.dp
        val xxxl = 48.dp
    }

    object Radius {
        val sm = 5.dp
        val md = 8.dp
        val lg = 12.dp
        val squircle = 16.dp
        val indicator = 12.dp
    }

    object Border {
        val thin = 1.dp
        val strong = 1.5.dp
        val focus = 2.dp
    }

    object Size {
        val touchMin = 48.dp
        val rowMin = 56.dp
        val checkbox = 22.dp
        val checkboxSm = 20.dp
        val dot = 10.dp
        val dotSm = 6.dp
        val marker = 6.dp
        val iconSm = 16.dp
        val iconMd = 24.dp
        val fab = 56.dp
        val bottomBar = 64.dp
        val rail = 80.dp
        val indicatorW = 48.dp
        val indicatorH = 32.dp
    }

    object Layout {
        val gutter = 16.dp
        val gutterWide = 24.dp
        val maxContent = 640.dp
        val dayPane = 360.dp
        val dayPaneLarge = 400.dp
        val listsPane = 280.dp
    }

    object Type {
        val titleLg = 26.sp
        val titleLgLine = 32.sp
        val titleMd = 18.sp
        val titleMdLine = 24.sp
        val body = 16.sp
        val bodyLine = 24.sp
        val bodySm = 15.sp
        val bodySmLine = 22.sp
        val caption = 13.sp
        val captionLine = 18.sp
        val label = 12.sp
        val labelLine = 16.sp
        val numberLg = 28.sp
        val numberLgLine = 34.sp
    }

    val zero = 0.dp
    const val FAST_MS = 100
}
