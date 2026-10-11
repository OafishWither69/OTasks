// SPDX-License-Identifier: GPL-3.0-or-later

package com.otasks.app
import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.StrictMode
import android.os.UserManager
import androidx.core.content.ContextCompat

class OTasksApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy
                    .Builder()
                    .detectAll()
                    .penaltyLog()
                    .build(),
            )
            StrictMode.setVmPolicy(
                StrictMode.VmPolicy
                    .Builder()
                    .detectAll()
                    .penaltyLog()
                    .build(),
            )
        }
        container = AppContainer(this)
        if (getSystemService(UserManager::class.java).isUserUnlocked) {
            container.startUnlocked()
        } else {
            val unlocked =
                object : BroadcastReceiver() {
                    override fun onReceive(
                        context: Context,
                        intent: Intent,
                    ) {
                        if (intent.action == Intent.ACTION_USER_UNLOCKED) {
                            container.startUnlocked()
                            unregisterReceiver(this)
                        }
                    }
                }
            ContextCompat.registerReceiver(this, unlocked, IntentFilter(Intent.ACTION_USER_UNLOCKED), ContextCompat.RECEIVER_NOT_EXPORTED)
        }
    }
}
