// SPDX-License-Identifier: GPL-3.0-or-later
package com.otasks.app
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LaunchTest {
    @get:Rule val activity = ActivityScenarioRule(MainActivity::class.java)

    @Test fun navigateAndRecreateEachTab() {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        listOf(R.string.schedule, R.string.history, R.string.settings, R.string.today).forEach { id ->
            val label = context.getString(id)
            val tab =
                device.wait(Until.findObject(By.text(label).clickable(true)), 5000)
                    ?: device.wait(Until.findObject(By.text(label)), 5000)
            assertNotNull(tab)
            tab.click()
            device.waitForIdle()
            assertTrue(device.wait(Until.hasObject(By.text(label).selected(true)), 5000))
            activity.scenario.recreate()
            assertNotNull(device.wait(Until.findObject(By.text(context.getString(R.string.later_phase))), 5000))
            assertTrue(device.wait(Until.hasObject(By.text(label).selected(true)), 5000))
        }
    }
}
