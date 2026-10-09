package com.bulktracker.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.bulktracker.app.ui.theme.BulkTrackerTheme
import com.bulktracker.app.ui.theme.Tokens

@Composable
fun StatusMarker(
    completionPercent: Int,
    isFutureDay: Boolean,
    modifier: Modifier = Modifier
) {
    if (isFutureDay || completionPercent <= 0) return

    val colors = BulkTrackerTheme.colors

    Canvas(modifier = modifier.size(Tokens.Marker)) {
        val radius = size.minDimension / 2f
        when {
            completionPercent >= 90 -> {
                // Filled circle in success
                drawCircle(
                    color = colors.success,
                    radius = radius,
                    style = Fill
                )
            }
            completionPercent in 60..89 -> {
                // 1.5dp ring in warning with a 2dp centre dot
                drawCircle(
                    color = colors.warning,
                    radius = radius - 0.75.dp.toPx(),
                    style = Stroke(width = 1.5.dp.toPx())
                )
                drawCircle(
                    color = colors.warning,
                    radius = 1.dp.toPx(),
                    style = Fill
                )
            }
            else -> {
                // 1-59%: empty 1.5dp ring in danger
                drawCircle(
                    color = colors.danger,
                    radius = radius - 0.75.dp.toPx(),
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }
        }
    }
}
