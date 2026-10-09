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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.ModalBottomSheet
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
import com.bulktracker.app.domain.ResolvedSection
import com.bulktracker.app.ui.theme.BulkTrackerTheme
import com.bulktracker.app.ui.theme.Tokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSheet(
    sections: List<ResolvedSection>,
    preselectedSectionId: Long?,
    onSaveCustomLog: (sectionId: Long, name: String, portion: String, kcal: Int?, protein: Double?) -> Unit,
    onSaveWithScope: (sectionId: Long, name: String, portion: String, kcal: Int?, protein: Double?) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = BulkTrackerTheme.colors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var selectedSectionId by remember {
        mutableStateOf(preselectedSectionId ?: sections.firstOrNull()?.section?.id ?: 1L)
    }

    val selectedSection = sections.firstOrNull { it.section.id == selectedSectionId }
    val isMealSection = selectedSection?.section?.isMealSection == true

    var name by remember { mutableStateOf("") }
    var portion by remember { mutableStateOf("") }
    var kcalText by remember { mutableStateOf("") }
    var kcalUnknown by remember { mutableStateOf(false) }
    var proteinText by remember { mutableStateOf("") }
    var proteinUnknown by remember { mutableStateOf(false) }
    var alsoAddToChecklist by remember { mutableStateOf(false) }

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
                .padding(bottom = Tokens.SpaceLg)
        ) {
            Text(
                text = "Add food",
                fontSize = Tokens.TitleMdSize,
                lineHeight = Tokens.TitleMdLineHeight,
                color = colors.textPrimary,
                modifier = Modifier.semantics { heading() }
            )

            Spacer(modifier = Modifier.height(Tokens.SpaceSm))

            // Section chips row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(Tokens.SpaceSm)
            ) {
                sections.forEach { sec ->
                    FilterChip(
                        selected = sec.section.id == selectedSectionId,
                        onClick = { selectedSectionId = sec.section.id },
                        label = {
                            Text(
                                text = sec.section.name,
                                fontSize = Tokens.CaptionSize
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = colors.border,
                            selectedLabelColor = colors.textPrimary,
                            containerColor = colors.card,
                            labelColor = colors.textSecondary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(Tokens.SpaceSm))

            // Meal section presets
            if (isMealSection) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(Tokens.SpaceSm)
                ) {
                    AssistChip(
                        onClick = {
                            kcalText = "350"
                            kcalUnknown = false
                            proteinText = "10"
                            proteinUnknown = false
                        },
                        label = { Text("Light plate · 350", fontSize = Tokens.LabelSize) }
                    )
                    AssistChip(
                        onClick = {
                            kcalText = "450"
                            kcalUnknown = false
                            proteinText = "12"
                            proteinUnknown = false
                        },
                        label = { Text("Normal plate · 450", fontSize = Tokens.LabelSize) }
                    )
                    AssistChip(
                        onClick = {
                            kcalText = "600"
                            kcalUnknown = false
                            proteinText = "16"
                            proteinUnknown = false
                        },
                        label = { Text("Big plate · 600", fontSize = Tokens.LabelSize) }
                    )
                    AssistChip(
                        onClick = {
                            kcalText = ""
                            kcalUnknown = true
                            proteinText = ""
                            proteinUnknown = true
                        },
                        label = { Text("I don't know", fontSize = Tokens.LabelSize) }
                    )
                }
                Spacer(modifier = Modifier.height(Tokens.SpaceSm))
            }

            // Name Field
            OutlinedTextField(
                value = name,
                onValueChange = { if (it.length <= 40) name = it },
                label = { Text("Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(Tokens.SpaceSm))

            // Portion Field
            OutlinedTextField(
                value = portion,
                onValueChange = { if (it.length <= 30) portion = it },
                label = { Text("Portion (optional)") },
                placeholder = { Text("e.g. 1 bowl") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(Tokens.SpaceSm))

            // Calories Field + I don't know
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = if (kcalUnknown) "" else kcalText,
                    onValueChange = { input ->
                        val filtered = input.filter { it.isDigit() }
                        if (filtered.length <= 4) kcalText = filtered
                    },
                    enabled = !kcalUnknown,
                    label = { Text("Calories") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(Tokens.SpaceSm))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
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

            Spacer(modifier = Modifier.height(Tokens.SpaceSm))

            // Protein Field + I don't know
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = if (proteinUnknown) "" else proteinText,
                    onValueChange = { input ->
                        val normalized = input.replace(',', '.')
                        if (normalized.length <= 5) proteinText = normalized
                    },
                    enabled = !proteinUnknown,
                    label = { Text("Protein (g)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(Tokens.SpaceSm))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
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

            Spacer(modifier = Modifier.height(Tokens.SpaceSm))

            // Checkbox "Also add to this meal's checklist"
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = alsoAddToChecklist,
                    onCheckedChange = { alsoAddToChecklist = it },
                    colors = CheckboxDefaults.colors(checkedColor = colors.ink)
                )
                Spacer(modifier = Modifier.width(Tokens.SpaceXs))
                Text(
                    text = "Also add to this meal's checklist",
                    fontSize = Tokens.BodySmSize,
                    color = colors.textPrimary
                )
            }

            Spacer(modifier = Modifier.height(Tokens.SpaceLg))

            // Action Buttons
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

                val isSaveValid = name.trim().isNotEmpty()

                Button(
                    onClick = {
                        val parsedKcal = if (kcalUnknown) null else kcalText.toIntOrNull()
                        val parsedProtein = if (proteinUnknown) null else proteinText.toDoubleOrNull()
                        val trimmedName = name.trim()
                        val trimmedPortion = portion.trim()

                        if (alsoAddToChecklist) {
                            onSaveWithScope(selectedSectionId, trimmedName, trimmedPortion, parsedKcal, parsedProtein)
                        } else {
                            onSaveCustomLog(selectedSectionId, trimmedName, trimmedPortion, parsedKcal, parsedProtein)
                        }
                    },
                    enabled = isSaveValid,
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
}
