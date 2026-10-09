package com.bulktracker.app.ui.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.bulktracker.app.ui.theme.BulkTrackerTheme
import com.bulktracker.app.ui.theme.SansFamily
import com.bulktracker.app.ui.theme.TitleMd
import com.bulktracker.app.ui.theme.Tokens
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHolidayDialog(
    initialStartDate: LocalDate = LocalDate.now(),
    initialEndDate: LocalDate = LocalDate.now(),
    onDismiss: () -> Unit,
    onConfirm: (name: String, start: LocalDate, end: LocalDate) -> Unit
) {
    val colors = BulkTrackerTheme.colors

    var name by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf(initialStartDate) }
    var endDate by remember { mutableStateOf(initialEndDate) }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    val nameTrimmed = name.trim()
    val isNameValid = nameTrimmed.isNotEmpty() && nameTrimmed.length <= 40
    val isDateValid = !endDate.isBefore(startDate)
    val isValid = isNameValid && isDateValid

    val dateFormatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.US)

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
                text = "Add holiday or break",
                style = TitleMd,
                color = colors.textPrimary,
                modifier = Modifier.semantics { heading() }
            )

            Spacer(modifier = Modifier.height(Tokens.SpaceMd))

            OutlinedTextField(
                value = name,
                onValueChange = { if (it.length <= 40) name = it },
                label = { Text("Name", fontFamily = SansFamily) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.ink,
                    unfocusedBorderColor = colors.border,
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary
                )
            )

            Spacer(modifier = Modifier.height(Tokens.SpaceMd))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Tokens.SpaceMd)
            ) {
                // Start date
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Start date",
                        fontSize = Tokens.CaptionSize,
                        color = colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(Tokens.SpaceXs))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(Tokens.RadiusMd))
                            .border(Tokens.BorderThin, colors.border, RoundedCornerShape(Tokens.RadiusMd))
                            .clickable { showStartDatePicker = true }
                            .padding(Tokens.SpaceMd),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = startDate.format(dateFormatter),
                            fontSize = Tokens.BodySmSize,
                            color = colors.textPrimary
                        )
                    }
                }

                // End date
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "End date",
                        fontSize = Tokens.CaptionSize,
                        color = colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(Tokens.SpaceXs))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(Tokens.RadiusMd))
                            .border(
                                Tokens.BorderThin,
                                if (!isDateValid) colors.danger else colors.border,
                                RoundedCornerShape(Tokens.RadiusMd)
                            )
                            .clickable { showEndDatePicker = true }
                            .padding(Tokens.SpaceMd),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = endDate.format(dateFormatter),
                            fontSize = Tokens.BodySmSize,
                            color = colors.textPrimary
                        )
                    }
                }
            }

            if (!isDateValid) {
                Spacer(modifier = Modifier.height(Tokens.SpaceXs))
                Text(
                    text = "End date must be on or after start date",
                    color = colors.danger,
                    fontSize = Tokens.CaptionSize
                )
            }

            Spacer(modifier = Modifier.height(Tokens.SpaceLg))

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
                        fontSize = Tokens.BodySize,
                        color = colors.textPrimary,
                        fontFamily = SansFamily
                    )
                }

                Spacer(modifier = Modifier.padding(horizontal = Tokens.SpaceXs))

                Button(
                    onClick = {
                        if (isValid) {
                            onConfirm(nameTrimmed, startDate, endDate)
                            onDismiss()
                        }
                    },
                    enabled = isValid,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.ink,
                        contentColor = colors.onInk,
                        disabledContainerColor = colors.card,
                        disabledContentColor = colors.textTertiary
                    ),
                    shape = RoundedCornerShape(Tokens.RadiusMd),
                    modifier = Modifier.height(Tokens.TouchMin)
                ) {
                    Text(
                        text = "Save",
                        fontSize = Tokens.BodySize,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = SansFamily
                    )
                }
            }
        }
    }

    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = startDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            startDate = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                            if (endDate.isBefore(startDate)) {
                                endDate = startDate
                            }
                        }
                        showStartDatePicker = false
                    }
                ) {
                    Text("OK", color = colors.textPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) {
                    Text("Cancel", color = colors.textPrimary)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = endDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            endDate = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                        }
                        showEndDatePicker = false
                    }
                ) {
                    Text("OK", color = colors.textPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) {
                    Text("Cancel", color = colors.textPrimary)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
