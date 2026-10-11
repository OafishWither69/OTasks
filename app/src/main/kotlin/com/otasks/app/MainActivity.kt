// SPDX-License-Identifier: GPL-3.0-or-later

package com.otasks.app
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.display.DisplayManager
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.SideEffect
import androidx.core.content.ContextCompat
import com.otasks.app.ui.navigation.AppShell
import com.otasks.app.ui.theme.OTasksTheme

class MainActivity :
    ComponentActivity(),
    DisplayManager.DisplayListener {
    private val powerReceiver =
        object : BroadcastReceiver() {
            override fun onReceive(
                context: Context,
                intent: Intent,
            ) {
                applyBestRefreshRate()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.isNavigationBarContrastEnforced = false
        applyBestRefreshRate()
        setContent {
            val dark = isSystemInDarkTheme()
            SideEffect {
                val style = SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT) { dark }
                enableEdgeToEdge(style, style)
                window.isNavigationBarContrastEnforced =
                    false
            }
            OTasksTheme(dark) { AppShell() }
        }
    }

    override fun onStart() {
        super.onStart()
        getSystemService(DisplayManager::class.java).registerDisplayListener(this, null)
        ContextCompat.registerReceiver(
            this,
            powerReceiver,
            IntentFilter(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED),
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )
    }

    override fun onStop() {
        (application as OTasksApp).container.stopTicker()
        unregisterReceiver(powerReceiver)
        getSystemService(DisplayManager::class.java).unregisterDisplayListener(this)
        super.onStop()
    }

    override fun onResume() {
        super.onResume()
        applyBestRefreshRate()
        (application as OTasksApp).container.resume()
    }

    override fun onDisplayChanged(displayId: Int) {
        if (display?.displayId == displayId) applyBestRefreshRate()
    }

    override fun onDisplayAdded(displayId: Int) = Unit

    override fun onDisplayRemoved(displayId: Int) = Unit

    private fun applyBestRefreshRate() {
        try {
            val d = display ?: return
            val auto = getSystemService(PowerManager::class.java).isPowerSaveMode || (Build.VERSION.SDK_INT >= 36 && d.hasArrSupport())
            val current = d.mode
            val mode =
                if (auto) {
                    0
                } else {
                    d.supportedModes
                        .filter {
                            it.physicalWidth == current.physicalWidth &&
                                it.physicalHeight == current.physicalHeight
                        }.maxByOrNull { it.refreshRate }
                        ?.modeId
                        ?: 0
                }
            window.attributes = window.attributes.apply { preferredDisplayModeId = mode }
        } catch (_: Exception) {
            // PL13: display preferences must never prevent launch.
        }
    }
}
