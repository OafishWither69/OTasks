package com.bulktracker.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.window.Dialog
import com.bulktracker.app.ui.theme.BulkTrackerTheme
import com.bulktracker.app.ui.theme.Tokens

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmVerb: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = BulkTrackerTheme.colors

    Dialog(onDismissRequest = onDismiss) {
        val shape = RoundedCornerShape(Tokens.RadiusLg)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.card, shape)
                .border(Tokens.BorderThin, colors.border, shape)
                .padding(Tokens.SpaceLg)
        ) {
            Text(
                text = title,
                fontSize = Tokens.TitleMdSize,
                lineHeight = Tokens.TitleMdLineHeight,
                color = colors.textPrimary,
                modifier = Modifier.semantics { heading() }
            )

            Spacer(modifier = Modifier.height(Tokens.SpaceSm))

            Text(
                text = message,
                fontSize = Tokens.BodySize,
                lineHeight = Tokens.BodyLineHeight,
                color = colors.textSecondary
            )

            Spacer(modifier = Modifier.height(Tokens.SpaceXl))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
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

                Spacer(modifier = Modifier.width(Tokens.SpaceSm))

                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.ink,
                        contentColor = colors.onInk
                    ),
                    shape = RoundedCornerShape(Tokens.RadiusMd),
                    modifier = Modifier.height(Tokens.TouchMin)
                ) {
                    Text(
                        text = confirmVerb,
                        fontSize = Tokens.BodySmSize
                    )
                }
            }
        }
    }
}
