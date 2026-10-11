// SPDX-License-Identifier: GPL-3.0-or-later
package com.otasks.app
import androidx.compose.ui.graphics.Color
import com.otasks.app.ui.theme.AccentDark
import com.otasks.app.ui.theme.AccentLight
import com.otasks.app.ui.theme.DarkColors
import com.otasks.app.ui.theme.ItemDark
import com.otasks.app.ui.theme.ItemLight
import com.otasks.app.ui.theme.LightColors
import com.otasks.app.ui.theme.contrast
import com.otasks.app.ui.theme.materialScheme
import com.otasks.app.ui.theme.onColor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ThemeTest {
    @Test fun contrastAndMaterialRoles() {
        listOf(LightColors, DarkColors).forEachIndexed { index, c ->
            val backgrounds = listOf(c.background, c.surface, c.card)
            listOf(c.successText, c.warningText, c.dangerText, c.textPrimary, c.textSecondary, c.textTertiary).forEach { text ->
                backgrounds.forEach {
                    assertTrue(
                        "text contrast $text on $it",
                        contrast(text, it) >= 4.5,
                    )
                }
            }
            listOf(c.success, c.warning, c.danger).forEach { icon ->
                backgrounds.forEach {
                    assertTrue(
                        "icon contrast $icon on $it",
                        contrast(icon, it) >= 3,
                    )
                }
            }
            (
                if (index ==
                    0
                ) {
                    AccentLight
                } else {
                    AccentDark
                }
            ).forEach { color ->
                backgrounds.forEach {
                    assertTrue(
                        "accent contrast $color on $it",
                        contrast(color, it) >= 3,
                    )
                }
                ;assertTrue(contrast(color, onColor(color)) >= 4.5)
            }
            (
                if (index ==
                    0
                ) {
                    ItemLight
                } else {
                    ItemDark
                }
            ).forEach { color ->
                backgrounds.forEach {
                    assertTrue(
                        "palette contrast $color on $it",
                        contrast(color, it) >= 4.5,
                    )
                }
                ;assertTrue(contrast(color, onColor(color)) >= 3)
            }
            val m = c.materialScheme()
            assertEquals(Color.Transparent, m.surfaceTint)
            assertEquals(c.ink, m.primary)
            assertEquals(c.dangerText, m.error)
            assertTrue(contrast(m.error, m.onError) >= 4.5)
            assertTrue(contrast(m.errorContainer, m.onErrorContainer) >= 4.5)
        }
    }

    @Test fun uiUsesTokensAndEveryKotlinFileHasLicence() {
        val ui = File("src/main/kotlin/com/otasks/app/ui")
        ui
            .walkTopDown()
            .filter {
                it.extension == "kt" && "/theme/" !in it.invariantSeparatorsPath
            }.forEach { assertFalse(it.path, Regex("\\.(dp|sp)\\b").containsMatchIn(it.readText())) }
        File("src")
            .walkTopDown()
            .filter {
                it.extension == "kt"
            }.forEach { assertTrue(it.path, it.readText().startsWith("// SPDX-License-Identifier: GPL-3.0-or-later")) }
    }
}
