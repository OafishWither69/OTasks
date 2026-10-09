package com.bulktracker.app.alarm

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.bulktracker.app.BulkTrackerApp
import com.bulktracker.app.MainActivity
import com.bulktracker.app.domain.VisibilityResolver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "AlarmReceiver"
        var wakeLock: PowerManager.WakeLock? = null

        const val ACTION_ALARM_TRIGGER = "com.bulktracker.app.ALARM_TRIGGER"
        const val ACTION_MARK_ALL = "com.bulktracker.app.ACTION_MARK_ALL"
        const val ACTION_SNOOZE = "com.bulktracker.app.ACTION_SNOOZE"
        const val ACTION_SKIP = "com.bulktracker.app.ACTION_SKIP"
        const val ACTION_OPEN = "com.bulktracker.app.ACTION_OPEN"
        const val ACTION_DISMISS = "com.bulktracker.app.ACTION_DISMISS"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()

        // FIRST, before any other work: acquire a PARTIAL_WAKE_LOCK tagged "bulktracker:alarm"
        // with a 60-second timeout and store it in a companion object
        try {
            val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            val wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "bulktracker:alarm")
            wl.acquire(60_000L)
            wakeLock = wl
        } catch (e: Exception) {
            Log.w(TAG, "Failed to acquire wake lock", e)
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                handleIntent(context, intent)
            } catch (e: Exception) {
                Log.e(TAG, "Error handling alarm intent", e)
                val type = intent.getStringExtra("type") ?: intent.action ?: "ALARM"
                AlarmDiagnostics.recordReceiverRun(context, type, "Unknown", "failed with exception: ${e.message}")
            } finally {
                // LAST, after handling any type: call AlarmScheduler.rescheduleAll(),
                // then release the wake lock if no service took it, then pendingResult.finish().
                try {
                    AlarmScheduler.rescheduleAll(context)
                } catch (e: Exception) {
                    Log.e(TAG, "Error in rescheduleAll", e)
                }
                if (wakeLock?.isHeld == true) {
                    try {
                        wakeLock?.release()
                    } catch (_: Exception) {}
                    wakeLock = null
                }
                pendingResult.finish()
            }
        }
    }

    private suspend fun handleIntent(context: Context, intent: Intent) {
        val app = context.applicationContext as? BulkTrackerApp ?: return
        val trackerRepo = app.container.trackerRepository
        val trackerDao = app.container.database.trackerDao()
        val settings = trackerDao.getSettings() ?: com.bulktracker.app.data.db.SettingsEntity()
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val action = intent.action
        val epochDay = intent.getLongExtra("epochDay", 0L)
        val sectionId = intent.getLongExtra("sectionId", 0L)
        val notificationId = intent.getIntExtra("notificationId", (20000 + sectionId).toInt())

        val k = ((epochDay % 4 + 4) % 4).toInt()
        val s = (sectionId % 1000).toInt()
        val followUpCode = 200000 + k * 1000 + s
        val snoozeCode = 300000 + k * 1000 + s

        when (action) {
            ACTION_MARK_ALL -> {
                // MARK_ALL: in one transaction, insert log rows (multiplier 1.0) for every visible
                // unchecked non-optional item of that section and day; cancel its FOLLOW_UP and SNOOZE alarms.
                trackerRepo.markAllEatenForAlarm(sectionId, epochDay)

                // Cancel FOLLOW_UP and SNOOZE alarms
                cancelAlarm(context, alarmManager, followUpCode)
                cancelAlarm(context, alarmManager, snoozeCode)

                // Stop AlarmRingService
                AlarmRingService.stopService(context)

                // Cancel notifications
                notificationManager.cancel(AlarmRingService.NOTIFICATION_ID)
                notificationManager.cancel(notificationId)
            }

            ACTION_SNOOZE -> {
                val currentSnoozeCount = intent.getIntExtra("snoozeCount", 0)
                if (currentSnoozeCount >= settings.maxSnoozes) {
                    AlarmRingService.stopService(context)
                    notificationManager.cancel(AlarmRingService.NOTIFICATION_ID)
                    notificationManager.cancel(notificationId)
                    return
                }
                val snoozeCount = currentSnoozeCount + 1
                val triggerMillis = System.currentTimeMillis() + settings.snoozeMin * 60 * 1000L

                val snoozeIntent = Intent(context, AlarmReceiver::class.java).apply {
                    this.action = ACTION_ALARM_TRIGGER
                    putExtra("type", "MAIN")
                    putExtra("epochDay", epochDay)
                    putExtra("sectionId", sectionId)
                    putExtra("snoozeCount", snoozeCount)
                }
                val pi = PendingIntent.getBroadcast(
                    context,
                    snoozeCode,
                    snoozeIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        if (alarmManager.canScheduleExactAlarms()) {
                            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pi)
                        } else {
                            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pi)
                        }
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pi)
                    } else {
                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerMillis, pi)
                    }
                } catch (e: SecurityException) {
                    Log.w(TAG, "Cannot schedule snooze exact alarm", e)
                }

                AlarmRingService.stopService(context)
                notificationManager.cancel(AlarmRingService.NOTIFICATION_ID)
                notificationManager.cancel(notificationId)
            }

            ACTION_SKIP -> {
                // SKIP: upsert DayOverride (epochDay, SECTION, sectionId, SKIP); cancel its FOLLOW_UP and SNOOZE alarms.
                trackerRepo.skipSectionForDay(sectionId, epochDay)
                cancelAlarm(context, alarmManager, followUpCode)
                cancelAlarm(context, alarmManager, snoozeCode)

                AlarmRingService.stopService(context)
                notificationManager.cancel(AlarmRingService.NOTIFICATION_ID)
                notificationManager.cancel(notificationId)
            }

            ACTION_OPEN -> {
                AlarmRingService.stopService(context)
                notificationManager.cancel(AlarmRingService.NOTIFICATION_ID)
                notificationManager.cancel(notificationId)

                val mainIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    putExtra("epochDay", epochDay)
                }
                context.startActivity(mainIntent)
            }

            ACTION_DISMISS -> {
                AlarmRingService.stopService(context)
                notificationManager.cancel(AlarmRingService.NOTIFICATION_ID)
                notificationManager.cancel(notificationId)
            }

            ACTION_ALARM_TRIGGER -> {
                val type = intent.getStringExtra("type") ?: "MAIN"
                when (type) {
                    "MAIN" -> {
                        val sectionObj = trackerDao.getSectionById(sectionId)
                        val sectionName = sectionObj?.name ?: "Section #$sectionId"

                        // type MAIN: load the section's visible non-optional items for epochDay.
                        // If all are checked, or the section is SKIPPED or MERGED that day: do nothing.
                        val overrides = trackerDao.getOverridesForDay(epochDay)
                        val secOverride = overrides.firstOrNull { it.targetType == "SECTION" && it.targetId == sectionId }
                        if (secOverride?.action == "SKIP" || secOverride?.action == "MERGE_INTO") {
                            AlarmDiagnostics.recordReceiverRun(context, "MAIN", sectionName, "skipped because skipped-or-merged")
                            return
                        }

                        val allItems = trackerDao.getItemsForSection(sectionId)
                        val itemOverrides = overrides.filter { it.targetType == "ITEM" }.associateBy { it.targetId }
                        val visibleNonOptional = allItems.filter { item ->
                            !item.isOptional &&
                            VisibilityResolver.isItemVisible(item, epochDay) &&
                            itemOverrides[item.id]?.action != "REMOVE"
                        }

                        val logEntries = trackerDao.getLogEntriesForDay(epochDay)
                        val checkedItemIds = logEntries.mapNotNull { it.foodItemId }.toSet()

                        if (visibleNonOptional.isNotEmpty() && visibleNonOptional.all { checkedItemIds.contains(it.id) }) {
                            AlarmDiagnostics.recordReceiverRun(context, "MAIN", sectionName, "skipped because all checked")
                            return
                        }

                        // Otherwise ContextCompat.startForegroundService(AlarmRingService with the same extras)
                        val serviceIntent = Intent(context, AlarmRingService::class.java).apply {
                            putExtras(intent)
                        }
                        try {
                            ContextCompat.startForegroundService(context, serviceIntent)
                            AlarmDiagnostics.recordReceiverRun(context, "MAIN", sectionName, "rang")
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to start AlarmRingService", e)
                            AlarmDiagnostics.recordReceiverRun(context, "MAIN", sectionName, "failed with exception: ${e.message}")
                        }

                        // and schedule FOLLOW_UP with setExactAndAllowWhileIdle at now + settings.followUpMin (skip if followUpMin == 0).
                        if (settings.followUpMin > 0) {
                            val followUpMillis = System.currentTimeMillis() + settings.followUpMin * 60 * 1000L
                            val followUpIntent = Intent(context, AlarmReceiver::class.java).apply {
                                this.action = ACTION_ALARM_TRIGGER
                                putExtra("type", "FOLLOW_UP")
                                putExtra("epochDay", epochDay)
                                putExtra("sectionId", sectionId)
                            }
                            val pi = PendingIntent.getBroadcast(
                                context,
                                followUpCode,
                                followUpIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                            )

                            try {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                    if (alarmManager.canScheduleExactAlarms()) {
                                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, followUpMillis, pi)
                                    } else {
                                        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, followUpMillis, pi)
                                    }
                                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, followUpMillis, pi)
                                } else {
                                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, followUpMillis, pi)
                                }
                            } catch (e: SecurityException) {
                                Log.w(TAG, "Cannot schedule follow-up alarm", e)
                            }
                        }
                    }

                    "FOLLOW_UP" -> {
                        // type FOLLOW_UP: if unchecked non-optional items remain and the section is not skipped:
                        // post a "meal_reminders" notification "<Name> — <n> items left (<kcal> kcal)" with the 4 action buttons.
                        val overrides = trackerDao.getOverridesForDay(epochDay)
                        val secOverride = overrides.firstOrNull { it.targetType == "SECTION" && it.targetId == sectionId }
                        if (secOverride?.action == "SKIP" || secOverride?.action == "MERGE_INTO") {
                            return
                        }

                        val section = trackerDao.getSectionById(sectionId) ?: return
                        val allItems = trackerDao.getItemsForSection(sectionId)
                        val itemOverrides = overrides.filter { it.targetType == "ITEM" }.associateBy { it.targetId }
                        val visibleNonOptional = allItems.filter { item ->
                            !item.isOptional &&
                            VisibilityResolver.isItemVisible(item, epochDay) &&
                            itemOverrides[item.id]?.action != "REMOVE"
                        }
                        val logEntries = trackerDao.getLogEntriesForDay(epochDay)
                        val checkedItemIds = logEntries.mapNotNull { it.foodItemId }.toSet()
                        val unchecked = visibleNonOptional.filter { !checkedItemIds.contains(it.id) }

                        if (unchecked.isNotEmpty()) {
                            val totalKcal = unchecked.sumOf { it.kcal ?: 0 }
                            NotificationHelper.showMealReminderNotification(
                                context = context,
                                notificationId = (20000 + sectionId).toInt(),
                                section = section,
                                epochDay = epochDay,
                                itemsLeft = unchecked.size,
                                kcalLeft = totalKcal,
                                settings = settings
                            )
                        }
                    }

                    "MAINTENANCE" -> {
                        // Handled in finally block via rescheduleAll
                    }
                }
            }
        }
    }

    private fun cancelAlarm(context: Context, alarmManager: AlarmManager, requestCode: Int) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_ALARM_TRIGGER
        }
        val pi = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pi != null) {
            alarmManager.cancel(pi)
            pi.cancel()
        }
    }
}
