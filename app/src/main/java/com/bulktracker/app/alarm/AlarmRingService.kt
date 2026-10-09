package com.bulktracker.app.alarm

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.bulktracker.app.BulkTrackerApp
import com.bulktracker.app.data.db.SectionEntity
import com.bulktracker.app.data.db.SettingsEntity
import com.bulktracker.app.domain.VisibilityResolver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.roundToInt

class AlarmRingService : Service() {

    companion object {
        private const val TAG = "AlarmRingService"
        const val NOTIFICATION_ID = 1001

        @Volatile
        var isRinging: Boolean = false
            private set

        fun stopService(context: Context) {
            val intent = Intent(context, AlarmRingService::class.java).apply {
                action = "STOP"
            }
            context.startService(intent)
        }
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var audioFocusRequest: AudioFocusRequest? = null
    private var originalAlarmVolume: Int? = null
    private val handler = Handler(Looper.getMainLooper())
    private var timeoutRunnable: Runnable? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "STOP" || intent?.action == "STOP_SWIPE") {
            val fromSwipe = intent.action == "STOP_SWIPE"
            stopRinging(fromTimeout = false, fromSwipe = fromSwipe)
            return START_NOT_STICKY
        }

        val epochDay = intent?.getLongExtra("epochDay", 0L) ?: 0L
        val sectionId = intent?.getLongExtra("sectionId", 0L) ?: 0L
        val snoozeCount = intent?.getIntExtra("snoozeCount", 0) ?: 0

        val app = applicationContext as? BulkTrackerApp
        val container = app?.container

        if (container == null) {
            stopSelf()
            return START_NOT_STICKY
        }

        // Only one alarm rings at a time. If AlarmRingService is already ringing when another
        // alarm arrives, do not start a second ring: post the new one as a normal notification
        if (isRinging) {
            serviceScope.launch(Dispatchers.IO) {
                val trackerDao = container.database.trackerDao()
                val section = trackerDao.getSectionById(sectionId) ?: return@launch
                val settings = trackerDao.getSettings() ?: SettingsEntity()
                val allItems = trackerDao.getItemsForSection(sectionId)
                val overrides = trackerDao.getOverridesForDay(epochDay)
                val itemOverrides = overrides.filter { it.targetType == "ITEM" }.associateBy { it.targetId }
                val visibleNonOptional = allItems.filter { item ->
                    !item.isOptional &&
                    VisibilityResolver.isItemVisible(item, epochDay) &&
                    itemOverrides[item.id]?.action != "REMOVE"
                }
                val logEntries = trackerDao.getLogEntriesForDay(epochDay)
                val checkedIds = logEntries.mapNotNull { it.foodItemId }.toSet()
                val unchecked = visibleNonOptional.filter { !checkedIds.contains(it.id) }
                val totalKcal = unchecked.sumOf { it.kcal ?: 0 }

                NotificationHelper.showMealReminderNotification(
                    context = applicationContext,
                    notificationId = (20000 + sectionId).toInt(),
                    section = section,
                    epochDay = epochDay,
                    itemsLeft = unchecked.size,
                    kcalLeft = totalKcal,
                    settings = settings
                )
            }
            return START_NOT_STICKY
        }

        isRinging = true

        serviceScope.launch(Dispatchers.IO) {
            val trackerDao = container.database.trackerDao()
            val section = trackerDao.getSectionById(sectionId) ?: return@launch
            val settings = trackerDao.getSettings() ?: SettingsEntity()

            val allItems = trackerDao.getItemsForSection(sectionId)
            val overrides = trackerDao.getOverridesForDay(epochDay)
            val itemOverrides = overrides.filter { it.targetType == "ITEM" }.associateBy { it.targetId }
            val visibleNonOptional = allItems.filter { item ->
                !item.isOptional &&
                VisibilityResolver.isItemVisible(item, epochDay) &&
                itemOverrides[item.id]?.action != "REMOVE"
            }
            val logEntries = trackerDao.getLogEntriesForDay(epochDay)
            val checkedIds = logEntries.mapNotNull { it.foodItemId }.toSet()
            val unchecked = visibleNonOptional.filter { !checkedIds.contains(it.id) }
            val totalKcal = unchecked.sumOf { it.kcal ?: 0 }

            val notification = buildRingingNotification(
                section = section,
                epochDay = epochDay,
                snoozeCount = snoozeCount,
                itemsLeft = unchecked.size,
                totalKcal = totalKcal,
                settings = settings
            )

            serviceScope.launch(Dispatchers.Main) {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        ServiceCompat.startForeground(
                            this@AlarmRingService,
                            NOTIFICATION_ID,
                            notification,
                            ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                        )
                    } else {
                        startForeground(NOTIFICATION_ID, notification)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error starting foreground service", e)
                }

                // Release wake lock acquired by AlarmReceiver right after startForeground()
                if (AlarmReceiver.wakeLock?.isHeld == true) {
                    try {
                        AlarmReceiver.wakeLock?.release()
                    } catch (_: Exception) {}
                    AlarmReceiver.wakeLock = null
                }

                startSoundAndVibration(settings)

                // Schedule timeout after settings.alarmRingSeconds
                timeoutRunnable = Runnable {
                    serviceScope.launch(Dispatchers.IO) {
                        stopRinging(fromTimeout = true, section = section, epochDay = epochDay, itemsLeft = unchecked.size, totalKcal = totalKcal, settings = settings)
                    }
                }
                handler.postDelayed(timeoutRunnable!!, settings.alarmRingSeconds * 1000L)
            }
        }

        return START_NOT_STICKY
    }

    private fun buildRingingNotification(
        section: SectionEntity,
        epochDay: Long,
        snoozeCount: Int,
        itemsLeft: Int,
        totalKcal: Int,
        settings: SettingsEntity
    ): Notification {
        val k = ((epochDay % 4 + 4) % 4).toInt()
        val s = (section.id % 1000).toInt()

        // Full screen intent to AlarmActivity
        val fullScreenIntent = Intent(this, AlarmActivity::class.java).apply {
            putExtra("epochDay", epochDay)
            putExtra("sectionId", section.id)
            putExtra("snoozeCount", snoozeCount)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            50000 + k * 1000 + s,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 1. Mark all eaten
        val markAllIntent = Intent(this, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_MARK_ALL
            putExtra("epochDay", epochDay)
            putExtra("sectionId", section.id)
        }
        val markAllPendingIntent = PendingIntent.getBroadcast(
            this,
            11000 + k * 1000 + s,
            markAllIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 2. Snooze
        val snoozeIntent = Intent(this, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_SNOOZE
            putExtra("epochDay", epochDay)
            putExtra("sectionId", section.id)
            putExtra("snoozeCount", snoozeCount)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            this,
            21000 + k * 1000 + s,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 3. Skip today
        val skipIntent = Intent(this, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_SKIP
            putExtra("epochDay", epochDay)
            putExtra("sectionId", section.id)
        }
        val skipPendingIntent = PendingIntent.getBroadcast(
            this,
            31000 + k * 1000 + s,
            skipIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 4. Open app
        val openIntent = Intent(this, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_OPEN
            putExtra("epochDay", epochDay)
        }
        val openPendingIntent = PendingIntent.getBroadcast(
            this,
            41000 + k * 1000 + s,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Delete intent (swipe to dismiss)
        val deleteIntent = Intent(this, AlarmRingService::class.java).apply {
            action = "STOP_SWIPE"
        }
        val deletePendingIntent = PendingIntent.getService(
            this,
            61000 + k * 1000 + s,
            deleteIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, NotificationHelper.CHANNEL_MEAL_ALARMS)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(section.name)
            .setContentText("$itemsLeft items · $totalKcal kcal")
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setOngoing(true)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setDeleteIntent(deletePendingIntent)
            .addAction(0, "Mark all eaten", markAllPendingIntent)

        // When an alarm rings with snoozeCount == settings.maxSnoozes, the Snooze button is hidden
        if (snoozeCount < settings.maxSnoozes) {
            builder.addAction(0, "Snooze ${settings.snoozeMin} min", snoozePendingIntent)
        }

        builder.addAction(0, "Skip today", skipPendingIntent)
        builder.addAction(0, "Open app", openPendingIntent)

        return builder.build()
    }

    private fun startSoundAndVibration(settings: SettingsEntity) {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // quiet = audioManager.ringerMode != AudioManager.RINGER_MODE_NORMAL
        // OR notificationManager.currentInterruptionFilter != NotificationManager.INTERRUPTION_FILTER_ALL
        // OR audioManager.mode is MODE_IN_CALL or MODE_IN_COMMUNICATION
        val quiet = audioManager.ringerMode != AudioManager.RINGER_MODE_NORMAL ||
            notificationManager.currentInterruptionFilter != NotificationManager.INTERRUPTION_FILTER_ALL ||
            audioManager.mode == AudioManager.MODE_IN_CALL ||
            audioManager.mode == AudioManager.MODE_IN_COMMUNICATION

        // Request audio focus
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .build()
                audioManager.requestAudioFocus(audioFocusRequest!!)
            } else {
                @Suppress("DEPRECATION")
                audioManager.requestAudioFocus(null, AudioManager.STREAM_ALARM, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to request audio focus", e)
        }

        // Sound handling
        if (!quiet) {
            try {
                originalAlarmVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM)
                // Crash safety: write to DB
                serviceScope.launch(Dispatchers.IO) {
                    val app = applicationContext as? BulkTrackerApp
                    val repo = app?.container?.trackerRepository
                    val curr = repo?.getSettingsDirect()
                    if (curr?.savedAlarmVolume == null && originalAlarmVolume != null) {
                        repo?.setSavedAlarmVolume(originalAlarmVolume)
                    }
                }

                val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
                val targetVolume = max(1, (maxVolume * settings.alarmVolumePercent / 100.0).roundToInt())
                audioManager.setStreamVolume(AudioManager.STREAM_ALARM, targetVolume, 0)

                val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

                mediaPlayer = MediaPlayer().apply {
                    setDataSource(this@AlarmRingService, alarmUri)
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    isLooping = true
                    setVolume(1f, 1f)
                    prepare()
                    start()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start MediaPlayer", e)
            }
        }

        // Vibration handling (both cases)
        try {
            val pattern = longArrayOf(0, 600, 400)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibrator = vibratorManager.defaultVibrator
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
            } else {
                @Suppress("DEPRECATION")
                vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(pattern, 0)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to start vibrator", e)
        }
    }

    private fun stopRinging(
        fromTimeout: Boolean = false,
        fromSwipe: Boolean = false,
        section: SectionEntity? = null,
        epochDay: Long = 0L,
        itemsLeft: Int = 0,
        totalKcal: Int = 0,
        settings: SettingsEntity? = null
    ) {
        timeoutRunnable?.let { handler.removeCallbacks(it) }

        // Stop sound and vibration
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (_: Exception) {}
        mediaPlayer = null

        try {
            vibrator?.cancel()
        } catch (_: Exception) {}
        vibrator = null

        // Restore volume and abandon audio focus
        try {
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            originalAlarmVolume?.let { orig ->
                audioManager.setStreamVolume(AudioManager.STREAM_ALARM, orig, 0)
                serviceScope.launch(Dispatchers.IO) {
                    val app = applicationContext as? BulkTrackerApp
                    app?.container?.trackerRepository?.setSavedAlarmVolume(null)
                }
                originalAlarmVolume = null
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && audioFocusRequest != null) {
                audioManager.abandonAudioFocusRequest(audioFocusRequest!!)
            } else {
                @Suppress("DEPRECATION")
                audioManager.abandonAudioFocus(null)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error restoring volume / abandoning audio focus", e)
        }

        isRinging = false
        stopForeground(STOP_FOREGROUND_REMOVE)

        // After timeout: post "meal_reminders" notification "Missed: <Name> — <n> items left"
        if (fromTimeout && !fromSwipe && section != null && settings != null) {
            NotificationHelper.showMealReminderNotification(
                context = applicationContext,
                notificationId = (20000 + section.id).toInt(),
                section = section,
                epochDay = epochDay,
                itemsLeft = itemsLeft,
                kcalLeft = totalKcal,
                settings = settings,
                isMissed = true
            )
        }

        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRinging(fromTimeout = false)
        serviceScope.cancel()
    }
}
