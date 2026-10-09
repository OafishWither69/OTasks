package com.bulktracker.app.ui.components

import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.bulktracker.app.ui.theme.BulkTrackerTheme
import com.bulktracker.app.ui.theme.Tokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemSheet(
    title: String,
    initialMultiplier: Double,
    initialKcal: Int?,
    initialProtein: Double?,
    isCustomEntry: Boolean,
    onSaveNutrition: (multiplier: Double, customKcal: Int?, customProtein: Double?) -> Unit,
    onEditItem: (() -> Unit)? = null,
    onRemoveFromChecklist: () -> Unit,
    onDeleteCustomEntry: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = BulkTrackerTheme.colors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var currentMultiplier by remember { mutableStateOf(initialMultiplier) }

    var kcalText by remember { mutableStateOf(initialKcal?.toString() ?: "") }
    var kcalUnknown by remember { mutableStateOf(initialKcal == null) }

    var proteinText by remember { mutableStateOf(initialProtein?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() } ?: "") }
    var proteinUnknown by remember { mutableStateOf(initialProtein == null) }

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
                text = title,
                fontSize = Tokens.TitleMdSize,
                lineHeight = Tokens.TitleMdLineHeight,
                color = colors.textPrimary,
                modifier = Modifier.semantics { heading() }
            )

            Spacer(modifier = Modifier.height(Tokens.SpaceMd))

            // Multiplier Chips
            Text(
                text = "Portion multiplier",
                fontSize = Tokens.CaptionSize,
                color = colors.textSecondary
            )

            Spacer(modifier = Modifier.height(Tokens.SpaceXs))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(Tokens.SpaceSm)
            ) {
                val multipliers = listOf(0.5 to "×0.5", 1.0 to "×1", 1.5 to "×1.5", 2.0 to "×2")
                multipliers.forEach { (factor, label) ->
                    val isSelected = currentMultiplier == factor
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            currentMultiplier = factor
                            val parsedKcal = if (kcalUnknown) null else kcalText.toIntOrNull()
                            val parsedProtein = if (proteinUnknown) null else proteinText.toDoubleOrNull()
                            onSaveNutrition(factor, parsedKcal, parsedProtein)
                        },
                        label = { Text(text = label, fontSize = Tokens.BodySmSize) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = colors.border,
                            selectedLabelColor = colors.textPrimary,
                            containerColor = colors.card,
                            labelColor = colors.textSecondary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(Tokens.SpaceMd))

            // Calories this day + I don't know
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = if (kcalUnknown) "" else kcalText,
                    onValueChange = { input ->
                        val filtered = input.filter { it.isDigit() }
                        if (filtered.length <= 4) {
                            kcalText = filtered
                            val parsed = filtered.toIntOrNull()
                            val parsedProtein = if (proteinUnknown) null else proteinText.toDoubleOrNull()
                            onSaveNutrition(currentMultiplier, parsed, parsedProtein)
                        }
                    },
                    enabled = !kcalUnknown,
                    label = { Text("Calories this day") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(Tokens.SpaceSm))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = kcalUnknown,
                        onCheckedChange = {
                            kcalUnknown = it
                            val parsedKcal = if (it) null else kcalText.toIntOrNull()
                            val parsedProtein = if (proteinUnknown) null else proteinText.toDoubleOrNull()
                            onSaveNutrition(currentMultiplier, parsedKcal, parsedProtein)
                        },
                        colors = CheckboxDefaults.colors(checkedColor = colors.ink)
                    )
                    Text(
                        text = "I don't know",
                        fontSize = Tokens.CaptionSize,
                        color = colors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(Tokens.SpaceSm))

            // Protein this day + I don't know
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = if (proteinUnknown) "" else proteinText,
                    onValueChange = { input ->
                        val normalized = input.replace(',', '.')
                        if (normalized.length <= 5) {
                            proteinText = normalized
                            val parsedProtein = normalized.toDoubleOrNull()
                            val parsedKcal = if (kcalUnknown) null else kcalText.toIntOrNull()
                            onSaveNutrition(currentMultiplier, parsedKcal, parsedProtein)
                        }
                    },
                    enabled = !proteinUnknown,
                    label = { Text("Protein this day") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(Tokens.SpaceSm))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = proteinUnknown,
                        onCheckedChange = {
                            proteinUnknown = it
                            val parsedProtein = if (it) null else proteinText.toDoubleOrNull()
                            val parsedKcal = if (kcalUnknown) null else kcalText.toIntOrNull()
                            onSaveNutrition(currentMultiplier, parsedKcal, parsedProtein)
                        },
                        colors = CheckboxDefaults.colors(checkedColor = colors.ink)
                    )
                    Text(
                        text = "I don't know",
                        fontSize = Tokens.CaptionSize,
                        color = colors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(Tokens.SpaceLg))

            // Action Buttons
            if (isCustomEntry) {
                Button(
                    onClick = onDeleteCustomEntry,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.danger,
                        contentColor = colors.onInk
                    ),
                    shape = RoundedCornerShape(Tokens.RadiusMd),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Tokens.TouchMin)
                ) {
                    Text(text = "Delete entry", fontSize = Tokens.BodySmSize)
                }
            } else {
                if (onEditItem != null) {
                    OutlinedButton(
                        onClick = onEditItem,
                        shape = RoundedCornerShape(Tokens.RadiusMd),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(Tokens.TouchMin)
                    ) {
                        Text(
                            text = "Edit item…",
                            fontSize = Tokens.BodySmSize,
                            color = colors.textPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(Tokens.SpaceSm))
                }

                OutlinedButton(
                    onClick = onRemoveFromChecklist,
                    shape = RoundedCornerShape(Tokens.RadiusMd),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Tokens.TouchMin)
                ) {
                    Text(
                        text = "Remove from checklist…",
                        fontSize = Tokens.BodySmSize,
                        color = colors.danger
                    )
                }
            }

            Spacer(modifier = Modifier.height(Tokens.SpaceSm))

            TextButton(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Tokens.TouchMin)
            ) {
                Text(
                    text = "Done",
                    fontSize = Tokens.BodySmSize,
                    color = colors.textPrimary
                )
            }
        }
    }
}
