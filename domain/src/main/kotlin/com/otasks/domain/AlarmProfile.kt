// SPDX-License-Identifier: GPL-3.0-or-later
package com.otasks.domain

data class AlarmProfile(
    val kind: String,
    val useDefault: Boolean = true,
    val soundUri: String? = null,
    val volumeMode: String = "SYSTEM",
    val volumePercent: Int = 70,
    val rampSeconds: Int = 0,
    val vibrationPattern: String = "STANDARD",
    val ringSeconds: Int = 60,
    val snoozeMin: Int = 10,
    val maxSnoozes: Int = 3,
    val flashScreen: Boolean = false,
    val whenNormal: String = "SOUND_VIBRATE",
    val whenVibrate: String = "VIBRATE",
    val whenSilent: String = "VIBRATE",
    val whenDnd: String = "VIBRATE",
    val whenInCall: String = "VIBRATE",
)

object AlarmProfiles {
    fun effective(
        kind: String,
        profiles: List<AlarmProfile>,
    ): AlarmProfile {
        val selected = profiles.single { it.kind == kind }
        return if (kind != "DEFAULT" && selected.useDefault) profiles.single { it.kind == "DEFAULT" } else selected
    }
}
