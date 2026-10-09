package com.bulktracker.app.ui.components

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.bulktracker.app.data.db.FoodItemEntity
import com.bulktracker.app.ui.theme.BulkTrackerTheme
import com.bulktracker.app.ui.theme.Tokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditItemSheet(
    item: FoodItemEntity,
    onSave: (name: String, portion: String, kcal: Int?, protein: Double?, isPriority: Boolean, isOptional: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = BulkTrackerTheme.colors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var name by remember { mutableStateOf(item.name) }
    var portion by remember { mutableStateOf(item.portionLabel) }

    var kcalText by remember { mutableStateOf(item.kcal?.toString() ?: "") }
    var kcalUnknown by remember { mutableStateOf(item.kcal == null) }

    var proteinText by remember {
        mutableStateOf(
            item.proteinG?.let {
                if (it % 1.0 == 0.0) it.toInt().toString() else it.toString()
            } ?: ""
        )
    }
    var proteinUnknown by remember { mutableStateOf(item.proteinG == null) }

    var isPriority by remember { mutableStateOf(item.isPriority) }
    var isOptional by remember { mutableStateOf(item.isOptional) }

    // Validation
    val isNameValid = name.trim().isNotEmpty() && name.length <= 40
    val isPortionValid = portion.length <= 30

    val kcalVal = kcalText.toIntOrNull()
    val isKcalValid = kcalUnknown || (kcalVal != null && kcalVal in 0..5000)
    val isKcalError = !kcalUnknown && (kcalVal == null || kcalVal !in 0..5000)

    val normalizedProtein = proteinText.replace(',', '.')
    val proVal = normalizedProtein.toDoubleOrNull()
    val isProteinValid = proteinUnknown || (proVal != null && proVal in 0.0..300.0)
    val isProteinError = !proteinUnknown && (proVal == null || proVal !in 0.0..300.0)

    val isValid = isNameValid && isPortionValid && isKcalValid && isProteinValid

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
                text = "Edit item",
                fontSize = Tokens.TitleMdSize,
                lineHeight = Tokens.TitleMdLineHeight,
                color = colors.textPrimary,
                modifier = Modifier.semantics { heading() }
            )

            // Name (1-40 chars, error "Enter a name")
            OutlinedTextField(
                value = name,
                onValueChange = { if (it.length <= 40) name = it },
                label = { Text("Name") },
                isError = name.trim().isEmpty(),
                supportingText = {
                    if (name.trim().isEmpty()) {
                        Text("Enter a name", color = colors.danger)
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Portion (max 30 chars)
            OutlinedTextField(
                value = portion,
                onValueChange = { if (it.length <= 30) portion = it },
                label = { Text("Portion") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Calories field plus a "I don't know" checkbox (kcal 0-5000, error "Enter 0 to 5,000")
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = if (kcalUnknown) "" else kcalText,
                    onValueChange = { input ->
                        val digits = input.filter { it.isDigit() }
                        if (digits.length <= 5) {
                            kcalText = digits
                        }
                    },
                    enabled = !kcalUnknown,
                    label = { Text("Calories") },
                    isError = isKcalError,
                    supportingText = {
                        if (isKcalError) {
                            Text("Enter 0 to 5,000", color = colors.danger)
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(Tokens.SpaceSm))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = kcalUnknown,
                        onCheckedChange = { kcalUnknown = it },
                        colors = CheckboxDefaults.colors(checkedColor = colors.ink)
                    )
                    Text(
                        text = "I don't know",
                        fontSize = Tokens.CaptionSize,
                        color = colors.textSecondary
                    )
                }
            }

            // Protein (g) field plus its own "I don't know" checkbox (0-300)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = if (proteinUnknown) "" else proteinText,
                    onValueChange = { input ->
                        val norm = input.replace(',', '.')
                        if (norm.length <= 6) {
                            proteinText = input
                        }
                    },
                    enabled = !proteinUnknown,
                    label = { Text("Protein (g)") },
                    isError = isProteinError,
                    supportingText = {
                        if (isProteinError) {
                            Text("Enter 0 to 300", color = colors.danger)
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(Tokens.SpaceSm))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = proteinUnknown,
                        onCheckedChange = { proteinUnknown = it },
                        colors = CheckboxDefaults.colors(checkedColor = colors.ink)
                    )
                    Text(
                        text = "I don't know",
                        fontSize = Tokens.CaptionSize,
                        color = colors.textSecondary
                    )
                }
            }

            // "★ Priority" Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "★ Priority",
                    fontSize = Tokens.BodySize,
                    color = colors.textPrimary
                )
                Switch(
                    checked = isPriority,
                    onCheckedChange = { isPriority = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = colors.card,
                        checkedTrackColor = colors.ink,
                        uncheckedThumbColor = colors.textSecondary,
                        uncheckedTrackColor = colors.surface
                    )
                )
            }

            // "Optional" Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Optional",
                    fontSize = Tokens.BodySize,
                    color = colors.textPrimary
                )
                Switch(
                    checked = isOptional,
                    onCheckedChange = { isOptional = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = colors.card,
                        checkedTrackColor = colors.ink,
                        uncheckedThumbColor = colors.textSecondary,
                        uncheckedTrackColor = colors.surface
                    )
                )
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
                            val finalKcal = if (kcalUnknown) null else kcalText.toIntOrNull()
                            val finalProtein = if (proteinUnknown) null else proteinText.replace(',', '.').toDoubleOrNull()
                            onSave(name.trim(), portion.trim(), finalKcal, finalProtein, isPriority, isOptional)
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
                    Text(text = "Save", fontSize = Tokens.BodySmSize)
                }
            }
        }
    }
}
