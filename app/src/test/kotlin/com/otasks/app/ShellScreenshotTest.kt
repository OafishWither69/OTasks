// SPDX-License-Identifier: GPL-3.0-or-later
package com.otasks.app
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.Density
import com.github.takahirom.roborazzi.captureRoboImage
import com.otasks.app.ui.navigation.AppShell
import com.otasks.app.ui.theme.OTasksTheme
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
class ShellScreenshotTest(
    private val width: Int,
    private val height: Int,
    private val dark: Boolean,
    private val font: Float,
    private val rtl: Boolean,
) {
    @get:Rule val compose = createComposeRule()

    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun destinations() {
        RuntimeEnvironment.setQualifiers("${if (rtl) "ar-rXB-ldrtl-" else "en-rUS-ldltr-"}w${width}dp-h${height}dp-mdpi")
        compose.setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f, font)) {
                OTasksTheme(dark) {
                    CompositionLocalProvider(LocalRippleConfiguration provides null) { AppShell() }
                }
            }
        }
        listOf(R.string.today, R.string.schedule, R.string.history, R.string.settings).forEachIndexed { index, label ->
            compose.onNodeWithTag("nav-$label").performClick()
            compose.onNodeWithText(RuntimeEnvironment.getApplication().getString(R.string.later_phase)).assertIsDisplayed()
            compose.mainClock.advanceTimeBy(1000)
            compose.waitForIdle()
            compose.onRoot().captureRoboImage("src/test/screenshots/$width-$height-$dark-$font-$rtl-$index.png")
        }
    }

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}x{1} dark={2} font={3} rtl={4}")
        fun sizes(): List<Array<Any>> =
            listOf(411 to 891, 700 to 1000, 1280 to 800, 891 to 411).flatMap { (w, h) ->
                listOf(false, true).flatMap { dark ->
                    listOf(1f, 2f).flatMap { font -> listOf(false, true).map { rtl -> arrayOf(w, h, dark, font, rtl) } }
                }
            }
    }
}
