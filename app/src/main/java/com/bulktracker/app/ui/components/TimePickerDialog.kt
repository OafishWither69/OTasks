package com.bulktracker.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.bulktracker.app.ui.theme.BulkTrackerTheme
import com.bulktracker.app.ui.theme.Tokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    initialHour: Int = 12,
    initialMinute: Int = 0,
    dayStartHour: Int = 4,
    onConfirm: (mold: Int) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = BulkTrackerTheme.colors
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = false
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val h = if (timePickerState.hour < dayStartHour) timePickerState.hour + 24 else timePickerState.hour
                    val mold = h * 60 + timePickerState.minute
                    onConfirm(mold)
                },
                modifier = Modifier.height(Tokens.TouchMin)
            ) {
                Text(
                    text = "OK",
                    fontSize = Tokens.BodySmSize,
                    color = colors.textPrimary
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.height(Tokens.TouchMin)
            ) {
                Text(
                    text = "Cancel",
                    fontSize = Tokens.BodySmSize,
                    color = colors.textPrimary
                )
            }
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                TimePicker(state = timePickerState)
                Spacer(modifier = Modifier.height(Tokens.SpaceSm))
            }
        },
        containerColor = colors.card
    )
}
