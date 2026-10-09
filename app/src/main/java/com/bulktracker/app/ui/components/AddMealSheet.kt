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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import com.bulktracker.app.ui.theme.BulkTrackerTheme
import com.bulktracker.app.ui.theme.Tokens
import com.bulktracker.app.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMealSheet(
    onContinueToScope: (name: String, idealMold: Int, size: String, colorHex: String) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = BulkTrackerTheme.colors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var name by remember { mutableStateOf("") }
    var selectedMold by remember { mutableIntStateOf(660) } // Default 11:00 AM
    var selectedSize by remember { mutableStateOf("SNACK") } // "SNACK" or "BIG"
    var selectedColorHex by remember { mutableStateOf("#3B82F6") }

    var showTimePicker by remember { mutableStateOf(false) }

    val availableColors = listOf(
        "#3B82F6",
        "#0D9488",
        "#EA580C",
        "#8B5CF6",
        "#E5484D",
        "#818CF8"
    )

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
            Text(
                text = "Add meal",
                fontSize = Tokens.TitleMdSize,
                lineHeight = Tokens.TitleMdLineHeight,
                color = colors.textPrimary,
                modifier = Modifier.semantics { heading() }
            )

            Spacer(modifier = Modifier.height(Tokens.SpaceMd))

            // Meal Name
            OutlinedTextField(
                value = name,
                onValueChange = { if (it.length <= 40) name = it },
                label = { Text("Meal name") },
                placeholder = { Text("e.g. Sunday brunch") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(Tokens.SpaceMd))

            // Time Selection
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Time: ${DateUtils.formatMold(selectedMold)}",
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

            Spacer(modifier = Modifier.height(Tokens.SpaceMd))

            // Size Selection
            Text(
                text = "Size",
                fontSize = Tokens.CaptionSize,
                color = colors.textSecondary
            )
            Spacer(modifier = Modifier.height(Tokens.SpaceXs))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Tokens.SpaceSm)
            ) {
                FilterChip(
                    selected = selectedSize == "SNACK",
                    onClick = { selectedSize = "SNACK" },
                    label = { Text("Snack (20 min)") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = colors.border,
                        selectedLabelColor = colors.textPrimary,
                        containerColor = colors.card,
                        labelColor = colors.textSecondary
                    )
                )
                FilterChip(
                    selected = selectedSize == "BIG",
                    onClick = { selectedSize = "BIG" },
                    label = { Text("Big meal (30 min)") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = colors.border,
                        selectedLabelColor = colors.textPrimary,
                        containerColor = colors.card,
                        labelColor = colors.textSecondary
                    )
                )
            }

            Spacer(modifier = Modifier.height(Tokens.SpaceMd))

            // Color circles selection
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
                    val color = Color(android.graphics.Color.parseColor(hex))
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

            Spacer(modifier = Modifier.height(Tokens.SpaceXl))

            // Buttons
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
                        onContinueToScope(name.trim(), selectedMold, selectedSize, selectedColorHex)
                    },
                    enabled = name.trim().isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.ink,
                        contentColor = colors.onInk
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
            onConfirm = { mold ->
                selectedMold = mold
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false }
        )
    }
}
