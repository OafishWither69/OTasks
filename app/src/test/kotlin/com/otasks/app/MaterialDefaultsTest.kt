// SPDX-License-Identifier: GPL-3.0-or-later
package com.otasks.app

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import com.otasks.app.ui.theme.LocalAppColors
import com.otasks.app.ui.theme.OTasksTheme
import com.otasks.app.ui.theme.Tokens
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(ParameterizedRobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class MaterialDefaultsTest(
    private val dark: Boolean,
) {
    @get:Rule val compose = createComposeRule()

    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun explicitColours() {
        RuntimeEnvironment.setQualifiers("w411dp-h891dp-mdpi")
        compose.setContent {
            OTasksTheme(dark) {
                val c = LocalAppColors.current
                Column(Modifier.fillMaxSize().background(c.background).padding(Tokens.Space.lg)) {
                    Text("Material defaults")
                    Card(
                        colors = CardDefaults.cardColors(containerColor = c.card),
                        elevation = CardDefaults.cardElevation(Tokens.zero),
                        border = BorderStroke(Tokens.Border.thin, c.border),
                    ) {
                        Text("Card", Modifier.padding(Tokens.Space.lg))
                    }
                    OutlinedTextField(
                        "Text",
                        {
                        },
                        isError = true,
                        label = {
                            Text("Field")
                        },
                        supportingText = {
                            Text("Error", color = c.dangerText)
                        },
                        colors =
                            OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = c.card,
                                unfocusedContainerColor = c.card,
                                errorContainerColor = c.card,
                                errorBorderColor = c.dangerText,
                            ),
                    )
                    Switch(
                        true,
                        {
                        },
                        colors =
                            SwitchDefaults.colors(
                                checkedThumbColor = c.onInk,
                                checkedTrackColor = c.ink,
                                uncheckedThumbColor = c.textTertiary,
                                uncheckedTrackColor = c.border,
                                uncheckedBorderColor = c.border,
                            ),
                    )
                    SearchBar(
                        query = "",
                        onQueryChange = {
                        },
                        onSearch = {
                        },
                        active = false,
                        onActiveChange = {
                        },
                        placeholder = {
                            Text("Search")
                        },
                        colors =
                            SearchBarDefaults.colors(
                                containerColor = c.surface,
                                dividerColor = c.border,
                            ),
                        tonalElevation = Tokens.zero,
                    ) {}
                    Slider(
                        0.4f,
                        {},
                        colors = SliderDefaults.colors(thumbColor = c.ink, activeTrackColor = c.ink, inactiveTrackColor = c.border),
                    )
                    SingleChoiceSegmentedButtonRow {
                        listOf("First", "Second").forEachIndexed { index, label ->
                            SegmentedButton(
                                index == 0,
                                {
                                },
                                RoundedCornerShape(
                                    Tokens.Radius.md,
                                ),
                                colors =
                                    SegmentedButtonDefaults.colors(
                                        activeContainerColor = c.border,
                                        activeContentColor = c.textPrimary,
                                        inactiveContainerColor = c.card,
                                        inactiveContentColor = c.textSecondary,
                                    ),
                            ) {
                                Text(label)
                            }
                        }
                    }
                    NavigationBar(containerColor = c.surface, tonalElevation = Tokens.zero) {
                        NavigationBarItem(
                            true,
                            {
                            },
                            icon = {
                                Icon(Icons.Outlined.CheckCircle, null)
                            },
                            label = {
                                Text("Selected")
                            },
                            colors =
                                NavigationBarItemDefaults.colors(
                                    selectedIconColor = c.accent,
                                    selectedTextColor = c.textPrimary,
                                    indicatorColor = c.border,
                                    unselectedIconColor = c.textTertiary,
                                    unselectedTextColor = c.textTertiary,
                                ),
                        )
                    }
                    FilterChip(
                        true,
                        {
                        },
                        label = {
                            Text("Selected filter")
                        },
                        colors =
                            FilterChipDefaults.filterChipColors(
                                containerColor = c.card,
                                labelColor = c.textSecondary,
                                selectedContainerColor = c.border,
                                selectedLabelColor = c.textPrimary,
                            ),
                    )
                }
            }
        }
        compose.onRoot().captureRoboImage("src/test/screenshots/material-$dark.png")
    }

    companion object {
        @JvmStatic @ParameterizedRobolectricTestRunner.Parameters
        fun themes(): List<Array<Any>> = listOf(arrayOf(false), arrayOf(true))
    }
}
