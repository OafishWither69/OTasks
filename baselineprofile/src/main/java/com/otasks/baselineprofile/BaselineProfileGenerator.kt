// SPDX-License-Identifier: GPL-3.0-or-later
package com.otasks.baselineprofile
import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {
    @get:Rule val baseline = BaselineProfileRule()

    @Test fun shell() =
        baseline.collect(packageName = "com.otasks.app") {
            pressHome()
            startActivityAndWait()
            listOf("Schedule", "History", "Settings", "Today").forEach { label ->
                device.wait(Until.findObject(By.text(label)), 5000).click()
                device.waitForIdle()
            }
        }
}
