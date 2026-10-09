package com.bulktracker.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.bulktracker.app.ui.theme.BulkTrackerTheme
import com.bulktracker.app.ui.theme.Tokens

@Composable
fun ProgressRow(
    label: String,
    currentValueText: String,
    targetValueText: String,
    currentValue: Double,
    targetValue: Double,
    unitText: String = "",
    extraText: String = "",
    modifier: Modifier = Modifier
) {
    val colors = BulkTrackerTheme.colors
    val progress = if (targetValue <= 0.0) 0f else (currentValue / targetValue).toFloat().coerceIn(0f, 1f)
    val isTargetMet = currentValue >= targetValue && targetValue > 0.0
    val fillColor = if (isTargetMet) colors.success else colors.accent

    val displayValue = if (unitText.isNotEmpty()) {
        "$currentValueText / $targetValueText $unitText$extraText"
    } else {
        "$currentValueText / $targetValueText$extraText"
    }

    val accessibilityDesc = "$label $currentValueText of $targetValueText $unitText".trim()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = accessibilityDesc
                progressBarRangeInfo = ProgressBarRangeInfo(
                    current = currentValue.toFloat(),
                    range = 0f..targetValue.toFloat().coerceAtLeast(1f)
                )
            }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = Tokens.CaptionSize,
                lineHeight = Tokens.CaptionLineHeight,
                color = colors.textSecondary
            )
            Text(
                text = displayValue,
                fontSize = Tokens.BodySmSize,
                lineHeight = Tokens.BodySmLineHeight,
                color = colors.textPrimary
            )
        }

        Spacer(modifier = Modifier.height(Tokens.SpaceXs))

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = fillColor,
            trackColor = colors.track
        )
    }
}
