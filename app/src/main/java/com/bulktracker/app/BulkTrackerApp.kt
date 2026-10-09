package com.bulktracker.app

import android.app.Application
import android.content.Context
import android.media.AudioManager
import com.bulktracker.app.alarm.AlarmDiagnostics
import com.bulktracker.app.alarm.AlarmRingService
import com.bulktracker.app.alarm.AlarmScheduler
import com.bulktracker.app.alarm.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BulkTrackerApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        AlarmDiagnostics.init(this)
        container = AppContainer(this)

        // Create notification channels
        NotificationHelper.createChannels(this)

        container.applicationScope.launch(Dispatchers.IO) {
            container.trackerRepository.seedIfEmpty()

            // In BulkTrackerApp.onCreate, if savedAlarmVolume != null and no alarm is ringing,
            // restore STREAM_ALARM to that value and set it to null (D3 line 718)
            val settings = container.trackerRepository.getSettingsDirect()
            if (settings.savedAlarmVolume != null && !AlarmRingService.isRinging) {
                try {
                    val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
                    audioManager.setStreamVolume(AudioManager.STREAM_ALARM, settings.savedAlarmVolume, 0)
                    container.trackerRepository.setSavedAlarmVolume(null)
                } catch (_: Exception) {}
            }

            // Reschedule alarms on app start
            AlarmScheduler.rescheduleAll(this@BulkTrackerApp)
        }
    }
}
