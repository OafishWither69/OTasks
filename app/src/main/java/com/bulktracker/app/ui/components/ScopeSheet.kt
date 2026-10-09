package com.bulktracker.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.bulktracker.app.data.repo.ScopeOptionType
import com.bulktracker.app.data.repo.ScopeSelection
import com.bulktracker.app.ui.theme.BulkTrackerTheme
import com.bulktracker.app.ui.theme.Tokens
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScopeSheet(
    targetName: String,
    isRemoveMode: Boolean,
    currentDate: LocalDate,
    onContinue: (ScopeSelection, confirmationSentence: String) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = BulkTrackerTheme.colors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var selectedType by remember { mutableStateOf(ScopeOptionType.JUST_THIS_DAY) }
    var startDate by remember { mutableStateOf(currentDate) }
    var endDate by remember { mutableStateOf(currentDate.plusDays(7)) }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    val dayFormatShort = DateTimeFormatter.ofPattern("EEE, d MMM", Locale.US)
    val dayFormatFull = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.US)
    val weekdayFull = currentDate.dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, Locale.US)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.card
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = Tokens.SpaceXl)
                .padding(bottom = Tokens.SpaceLg)
        ) {
            val title = if (isRemoveMode) "Remove $targetName" else "Add $targetName"
            Text(
                text = title,
                fontSize = Tokens.TitleMdSize,
                lineHeight = Tokens.TitleMdLineHeight,
                color = colors.textPrimary,
                modifier = Modifier.semantics { heading() }
            )

            Spacer(modifier = Modifier.height(Tokens.SpaceMd))

            // 4 RadioButton rows
            val option1Label = "Just this day (${currentDate.format(dayFormatShort)})"
            val option2Label = "Every $weekdayFull from now"
            val option3Label = "Forever from today"
            val option4Label = "For a date range"

            ScopeOptionRow(
                text = option1Label,
                selected = selectedType == ScopeOptionType.JUST_THIS_DAY,
                onClick = { selectedType = ScopeOptionType.JUST_THIS_DAY }
            )

            ScopeOptionRow(
                text = option2Label,
                selected = selectedType == ScopeOptionType.EVERY_WEEKDAY_FROM_NOW,
                onClick = { selectedType = ScopeOptionType.EVERY_WEEKDAY_FROM_NOW }
            )

            ScopeOptionRow(
                text = option3Label,
                selected = selectedType == ScopeOptionType.FOREVER_FROM_TODAY,
                onClick = { selectedType = ScopeOptionType.FOREVER_FROM_TODAY }
            )

            ScopeOptionRow(
                text = option4Label,
                selected = selectedType == ScopeOptionType.DATE_RANGE,
                onClick = { selectedType = ScopeOptionType.DATE_RANGE }
            )

            if (selectedType == ScopeOptionType.DATE_RANGE) {
                Spacer(modifier = Modifier.height(Tokens.SpaceSm))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { showStartDatePicker = true },
                        modifier = Modifier.weight(1f).height(Tokens.TouchMin)
                    ) {
                        Text(
                            text = "From: ${startDate.format(dayFormatShort)}",
                            fontSize = Tokens.BodySmSize,
                            color = colors.textPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(Tokens.SpaceSm))
                    OutlinedButton(
                        onClick = { showEndDatePicker = true },
                        modifier = Modifier.weight(1f).height(Tokens.TouchMin)
                    ) {
                        Text(
                            text = "To: ${endDate.format(dayFormatShort)}",
                            fontSize = Tokens.BodySmSize,
                            color = colors.textPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Tokens.SpaceXl))

            Button(
                onClick = {
                    val selection = ScopeSelection(
                        type = selectedType,
                        rangeStart = if (selectedType == ScopeOptionType.DATE_RANGE) startDate else null,
                        rangeEnd = if (selectedType == ScopeOptionType.DATE_RANGE) endDate else null
                    )

                    val sentence = if (isRemoveMode) {
                        when (selectedType) {
                            ScopeOptionType.JUST_THIS_DAY ->
                                "Remove $targetName for ${currentDate.format(dayFormatShort)}. Other days stay as they are."
                            ScopeOptionType.EVERY_WEEKDAY_FROM_NOW ->
                                "Remove $targetName from every $weekdayFull from ${currentDate.format(dayFormatFull)}. Past days stay as they are."
                            ScopeOptionType.FOREVER_FROM_TODAY ->
                                "Remove $targetName forever from ${currentDate.format(dayFormatFull)}. Past days stay as they are."
                            ScopeOptionType.DATE_RANGE ->
                                "Remove $targetName from ${startDate.format(dayFormatFull)} to ${endDate.format(dayFormatFull)}. Other days stay as they are."
                        }
                    } else {
                        when (selectedType) {
                            ScopeOptionType.JUST_THIS_DAY ->
                                "Add $targetName for ${currentDate.format(dayFormatShort)}."
                            ScopeOptionType.EVERY_WEEKDAY_FROM_NOW ->
                                "Add $targetName to every $weekdayFull from ${currentDate.format(dayFormatFull)}."
                            ScopeOptionType.FOREVER_FROM_TODAY ->
                                "Add $targetName forever from ${currentDate.format(dayFormatFull)}."
                            ScopeOptionType.DATE_RANGE ->
                                "Add $targetName from ${startDate.format(dayFormatFull)} to ${endDate.format(dayFormatFull)}."
                        }
                    }

                    onContinue(selection, sentence)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.ink,
                    contentColor = colors.onInk
                ),
                shape = RoundedCornerShape(Tokens.RadiusMd),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Tokens.TouchMin)
            ) {
                Text(
                    text = "Continue",
                    fontSize = Tokens.BodySmSize
                )
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
                        val millis = datePickerState.selectedDateMillis
                        if (millis != null) {
                            startDate = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                            if (endDate.isBefore(startDate)) {
                                endDate = startDate
                            }
                        }
                        showStartDatePicker = false
                    },
                    modifier = Modifier.height(Tokens.TouchMin)
                ) {
                    Text("OK", color = colors.textPrimary)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showStartDatePicker = false },
                    modifier = Modifier.height(Tokens.TouchMin)
                ) {
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
                        val millis = datePickerState.selectedDateMillis
                        if (millis != null) {
                            val picked = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                            endDate = if (picked.isBefore(startDate)) startDate else picked
                        }
                        showEndDatePicker = false
                    },
                    modifier = Modifier.height(Tokens.TouchMin)
                ) {
                    Text("OK", color = colors.textPrimary)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showEndDatePicker = false },
                    modifier = Modifier.height(Tokens.TouchMin)
                ) {
                    Text("Cancel", color = colors.textPrimary)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun ScopeOptionRow(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colors = BulkTrackerTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(Tokens.TouchMin)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
            colors = RadioButtonDefaults.colors(
                selectedColor = colors.ink,
                unselectedColor = colors.textTertiary
            )
        )
        Spacer(modifier = Modifier.width(Tokens.SpaceSm))
        Text(
            text = text,
            fontSize = Tokens.BodySize,
            color = colors.textPrimary
        )
    }
}
