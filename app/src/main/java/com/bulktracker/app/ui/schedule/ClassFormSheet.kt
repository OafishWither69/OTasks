package com.bulktracker.app.ui.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bulktracker.app.data.db.ClassEntity
import com.bulktracker.app.ui.components.ConfirmDialog
import com.bulktracker.app.ui.components.TimePickerDialog
import com.bulktracker.app.ui.theme.BulkTrackerTheme
import com.bulktracker.app.ui.theme.SansFamily
import com.bulktracker.app.ui.theme.TitleMd
import com.bulktracker.app.ui.theme.Tokens
import com.bulktracker.app.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ClassFormSheet(
    classEntity: ClassEntity? = null,
    onDismiss: () -> Unit,
    onSaveAdd: (name: String, weekdays: Set<Int>, startMold: Int, endMold: Int, location: String) -> Unit,
    onSaveEdit: (id: Long, name: String, weekday: Int, startMold: Int, endMold: Int, location: String) -> Unit,
    onDelete: (id: Long) -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    val colors = BulkTrackerTheme.colors
    val isEdit = classEntity != null

    var name by remember { mutableStateOf(classEntity?.name ?: "") }
    var nameTouched by remember { mutableStateOf(false) }
    var location by remember { mutableStateOf(classEntity?.location ?: "") }

    // In add mode: multiple days. In edit mode: single choice.
    var selectedDays by remember {
        mutableStateOf(
            if (isEdit) setOf(classEntity!!.weekday) else setOf(1)
        )
    }
    var selectedSingleDay by remember {
        mutableIntStateOf(classEntity?.weekday ?: 1)
    }

    var startMold by remember { mutableIntStateOf(classEntity?.startMold ?: 540) } // 9:00 AM default
    var endMold by remember { mutableIntStateOf(classEntity?.endMold ?: 600) } // 10:00 AM default

    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val nameTrimmed = name.trim()
    val isNameValid = nameTrimmed.isNotEmpty() && nameTrimmed.length <= 40
    val isLocationValid = location.trim().length <= 40
    val isDaysValid = if (isEdit) true else selectedDays.isNotEmpty()
    val isTimeValid = endMold > startMold

    val isValid = isNameValid && isLocationValid && isDaysValid && isTimeValid

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.card,
        modifier = Modifier
            .navigationBarsPadding()
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Tokens.SpaceLg)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = if (isEdit) "Edit class" else "Add class",
                style = TitleMd,
                color = colors.textPrimary
            )

            Spacer(modifier = Modifier.height(Tokens.SpaceMd))

            // Name
            OutlinedTextField(
                value = name,
                onValueChange = {
                    nameTouched = true
                    if (it.length <= 40) name = it
                },
                label = { Text("Name", fontFamily = SansFamily) },
                isError = nameTouched && name.trim().isEmpty(),
                supportingText = {
                    if (nameTouched && name.trim().isEmpty()) {
                        Text("Enter a name", color = colors.danger, fontSize = Tokens.CaptionSize)
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.ink,
                    unfocusedBorderColor = colors.border,
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary,
                    errorBorderColor = colors.danger
                )
            )

            Spacer(modifier = Modifier.height(Tokens.SpaceSm))

            // Location
            OutlinedTextField(
                value = location,
                onValueChange = { if (it.length <= 40) location = it },
                label = { Text("Location (optional)", fontFamily = SansFamily) },
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

            // Days chips
            Text(
                text = "Days",
                fontSize = Tokens.CaptionSize,
                color = colors.textSecondary
            )
            Spacer(modifier = Modifier.height(Tokens.SpaceXs))

            val dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Tokens.SpaceSm),
                verticalArrangement = Arrangement.spacedBy(Tokens.SpaceXs)
            ) {
                dayLabels.forEachIndexed { index, label ->
                    val dayNum = index + 1 // 1 = Mon ... 7 = Sun
                    val isSelected = if (isEdit) selectedSingleDay == dayNum else dayNum in selectedDays

                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            if (isEdit) {
                                selectedSingleDay = dayNum
                            } else {
                                selectedDays = if (dayNum in selectedDays) {
                                    selectedDays - dayNum
                                } else {
                                    selectedDays + dayNum
                                }
                            }
                        },
                        label = { Text(label, fontSize = Tokens.BodySmSize, fontFamily = SansFamily) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = colors.ink,
                            selectedLabelColor = colors.onInk,
                            containerColor = colors.card,
                            labelColor = colors.textPrimary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = colors.border,
                            selectedBorderColor = colors.border
                        )
                    )
                }
            }

            if (!isEdit && selectedDays.isEmpty()) {
                Text(
                    text = "Pick at least one day",
                    color = colors.danger,
                    fontSize = Tokens.CaptionSize,
                    modifier = Modifier.padding(top = Tokens.SpaceXs)
                )
            }

            Spacer(modifier = Modifier.height(Tokens.SpaceMd))

            // Start and End Times
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Tokens.SpaceMd)
            ) {
                // Start Time
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Start",
                        fontSize = Tokens.CaptionSize,
                        color = colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(Tokens.SpaceXs))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(Tokens.RadiusMd))
                            .border(Tokens.BorderThin, colors.border, RoundedCornerShape(Tokens.RadiusMd))
                            .clickable { showStartTimePicker = true }
                            .padding(Tokens.SpaceMd),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = DateUtils.formatMold(startMold),
                            fontSize = Tokens.BodySize,
                            color = colors.textPrimary
                        )
                    }
                }

                // End Time
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "End",
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
                                if (!isTimeValid) colors.danger else colors.border,
                                RoundedCornerShape(Tokens.RadiusMd)
                            )
                            .clickable { showEndTimePicker = true }
                            .padding(Tokens.SpaceMd),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = DateUtils.formatMold(endMold),
                            fontSize = Tokens.BodySize,
                            color = colors.textPrimary
                        )
                    }
                }
            }

            if (!isTimeValid) {
                Text(
                    text = "End must be after start",
                    color = colors.danger,
                    fontSize = Tokens.CaptionSize,
                    modifier = Modifier.padding(top = Tokens.SpaceXs)
                )
            }

            Spacer(modifier = Modifier.height(Tokens.SpaceLg))

            // Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = Tokens.SpaceXl),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isEdit) {
                    TextButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.height(Tokens.TouchMin)
                    ) {
                        Text(
                            text = "Delete",
                            fontSize = Tokens.BodySize,
                            color = colors.danger,
                            fontFamily = SansFamily
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Row(horizontalArrangement = Arrangement.spacedBy(Tokens.SpaceSm)) {
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

                    Button(
                        onClick = {
                            if (isValid) {
                                if (isEdit) {
                                    onSaveEdit(
                                        classEntity!!.id,
                                        nameTrimmed,
                                        selectedSingleDay,
                                        startMold,
                                        endMold,
                                        location.trim()
                                    )
                                } else {
                                    onSaveAdd(
                                        nameTrimmed,
                                        selectedDays,
                                        startMold,
                                        endMold,
                                        location.trim()
                                    )
                                }
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
    }

    if (showStartTimePicker) {
        val h = (startMold % 1440) / 60
        val m = startMold % 60
        TimePickerDialog(
            initialHour = h,
            initialMinute = m,
            onConfirm = { mold ->
                startMold = mold
                showStartTimePicker = false
            },
            onDismiss = { showStartTimePicker = false }
        )
    }

    if (showEndTimePicker) {
        val h = (endMold % 1440) / 60
        val m = endMold % 60
        TimePickerDialog(
            initialHour = h,
            initialMinute = m,
            onConfirm = { mold ->
                endMold = mold
                showEndTimePicker = false
            },
            onDismiss = { showEndTimePicker = false }
        )
    }

    if (showDeleteConfirm && classEntity != null) {
        ConfirmDialog(
            title = "Delete class",
            message = "Delete ${classEntity.name} from every week of the semester?",
            confirmVerb = "Delete",
            onConfirm = {
                onDelete(classEntity.id)
                showDeleteConfirm = false
                onDismiss()
            },
            onDismiss = { showDeleteConfirm = false }
        )
    }
}
