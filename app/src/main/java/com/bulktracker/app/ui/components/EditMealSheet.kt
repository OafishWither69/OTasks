package com.bulktracker.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.bulktracker.app.data.db.SectionEntity
import com.bulktracker.app.ui.theme.BulkTrackerTheme
import com.bulktracker.app.ui.theme.TitleMd
import com.bulktracker.app.ui.theme.Tokens
import com.bulktracker.app.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMealSheet(
    section: SectionEntity,
    onSave: (name: String, idealMold: Int, size: String, colorHex: String) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = BulkTrackerTheme.colors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var name by remember { mutableStateOf(section.name) }
    var selectedMold by remember { mutableIntStateOf(section.idealMold) }
    var selectedSize by remember { mutableStateOf(section.size) }
    var selectedColorHex by remember { mutableStateOf(section.colorHex) }

    var showTimePicker by remember { mutableStateOf(false) }

    val availableColors = listOf(
        "#3B82F6",
        "#0D9488",
        "#EA580C",
        "#8B5CF6",
        "#E5484D",
        "#818CF8"
    )

    val isNameValid = name.trim().isNotEmpty() && name.length <= 40
    val isValid = isNameValid

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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Tokens.SpaceXl)
                .padding(bottom = Tokens.SpaceLg),
            verticalArrangement = Arrangement.spacedBy(Tokens.SpaceMd)
        ) {
            Text(
                text = "Edit meal",
                style = TitleMd,
                color = colors.textPrimary,
                modifier = Modifier.semantics { heading() }
            )

            // Name (1-40 chars, error "Enter a name")
            OutlinedTextField(
                value = name,
                onValueChange = { if (it.length <= 40) name = it },
                label = { Text("Meal name") },
                placeholder = { Text("e.g. Lunch") },
                isError = name.trim().isEmpty(),
                supportingText = {
                    if (name.trim().isEmpty()) {
                        Text("Enter a name", color = colors.danger)
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Time (hidden for MORNING_SHAKE)
            if (section.role != "MORNING_SHAKE") {
                Column {
                    Text(
                        text = "Time",
                        fontSize = Tokens.CaptionSize,
                        color = colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(Tokens.SpaceXs))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(Tokens.RadiusMd))
                            .border(Tokens.BorderThin, colors.border, RoundedCornerShape(Tokens.RadiusMd))
                            .clickable { showTimePicker = true }
                            .padding(horizontal = Tokens.SpaceMd, vertical = Tokens.SpaceMd),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = DateUtils.formatMold(selectedMold),
                            fontSize = Tokens.BodySize,
                            color = colors.textPrimary
                        )
                        OutlinedButton(
                            onClick = { showTimePicker = true },
                            modifier = Modifier.height(Tokens.TouchMin)
                        ) {
                            Text("Change time", color = colors.textPrimary)
                        }
                    }
                }
            }

            // Size as SegmentedButton "Snack" / "Big meal" (SectionEntity.size "SNACK" / "BIG")
            Column {
                Text(
                    text = "Size",
                    fontSize = Tokens.CaptionSize,
                    color = colors.textSecondary
                )
                Spacer(modifier = Modifier.height(Tokens.SpaceXs))
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SegmentedButton(
                        selected = selectedSize == "SNACK",
                        onClick = { selectedSize = "SNACK" },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = colors.ink,
                            activeContentColor = colors.onInk,
                            inactiveContainerColor = colors.card,
                            inactiveContentColor = colors.textPrimary,
                            activeBorderColor = colors.border,
                            inactiveBorderColor = colors.border
                        )
                    ) {
                        Text("Snack", fontSize = Tokens.BodySmSize)
                    }
                    SegmentedButton(
                        selected = selectedSize == "BIG",
                        onClick = { selectedSize = "BIG" },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = colors.ink,
                            activeContentColor = colors.onInk,
                            inactiveContainerColor = colors.card,
                            inactiveContentColor = colors.textPrimary,
                            activeBorderColor = colors.border,
                            inactiveBorderColor = colors.border
                        )
                    ) {
                        Text("Big meal", fontSize = Tokens.BodySmSize)
                    }
                }
            }

            // Colour as 6 colour circles
            Column {
                Text(
                    text = "Color",
                    fontSize = Tokens.CaptionSize,
                    color = colors.textSecondary
                )
                Spacer(modifier = Modifier.height(Tokens.SpaceXs))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(Tokens.SpaceMd),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    availableColors.forEach { hex ->
                        val color = try {
                            Color(android.graphics.Color.parseColor(hex))
                        } catch (e: Exception) {
                            colors.ink
                        }
                        val isSelected = selectedColorHex.equals(hex, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(color)
                                .then(
                                    if (isSelected) {
                                        Modifier.border(Tokens.BorderFocus, colors.textPrimary, CircleShape)
                                    } else {
                                        Modifier
                                    }
                                )
                                .clickable { selectedColorHex = hex },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = "Selected",
                                    tint = Color.White,
                                    modifier = Modifier.size(Tokens.IconSm)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Tokens.SpaceSm))

            // Buttons: "Cancel" (text) and "Save" (ink, disabled until valid)
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
                    onClick = {
                        if (isValid) {
                            onSave(name.trim(), selectedMold, selectedSize, selectedColorHex)
                        }
                    },
                    enabled = isValid,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.ink,
                        contentColor = colors.onInk,
                        disabledContainerColor = colors.border,
                        disabledContentColor = colors.textTertiary
                    ),
                    shape = RoundedCornerShape(Tokens.RadiusMd),
                    modifier = Modifier.height(Tokens.TouchMin)
                ) {
                    Text(
                        text = "Save",
                        fontSize = Tokens.BodySmSize
                    )
                }
            }
        }
    }

    if (showTimePicker) {
        val initialHour = (selectedMold % 1440) / 60
        val initialMinute = (selectedMold % 1440) % 60
        TimePickerDialog(
            initialHour = initialHour,
            initialMinute = initialMinute,
            dayStartHour = 4,
            onConfirm = { mold ->
                selectedMold = mold
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false }
        )
    }
}
