package com.bulktracker.app.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.bulktracker.app.BulkTrackerApp
import com.bulktracker.app.MainActivity
import com.bulktracker.app.domain.VisibilityResolver
import com.bulktracker.app.util.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.ZoneId

object AlarmScheduler {
    private const val TAG = "AlarmScheduler"

    suspend fun rescheduleAll(context: Context) = withContext(Dispatchers.IO) {
        val app = context.applicationContext as? BulkTrackerApp ?: return@withContext
        val container = app.container
        val trackerDao = container.database.trackerDao()
        val settings = trackerDao.getSettings() ?: com.bulktracker.app.data.db.SettingsEntity()
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return@withContext

        val now = LocalDateTime.now()
        val today = DateUtils.logicalToday(now, settings.dayStartHour)
        val tomorrow = today.plusDays(1)
        val targetDays = listOf(today, tomorrow)

        val allSections = trackerDao.getAllSections()

        // 4. Before scheduling, rescheduleAll() cancels ONLY the MAIN codes of every section for today and tomorrow, and code 9000.
        // It NEVER cancels FOLLOW_UP, SNOOZE or 9001 alarms; those check the current state themselves when they fire.
        for (day in targetDays) {
            val epochDay = day.toEpochDay()
            val k = ((epochDay % 4 + 4) % 4).toInt()
            for (section in allSections) {
                val s = (section.id % 1000).toInt()
                val mainCode = 100000 + k * 1000 + s
                val cancelIntent = Intent(context, AlarmReceiver::class.java).apply {
                    action = AlarmReceiver.ACTION_ALARM_TRIGGER
                }
                val pi = PendingIntent.getBroadcast(
                    context,
                    mainCode,
                    cancelIntent,
                    PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
                )
                if (pi != null) {
                    alarmManager.cancel(pi)
                    pi.cancel()
                }
            }
        }

        // Cancel code 9000 (weigh-in)
        val cancel9000 = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_ALARM_TRIGGER
        }
        val pi9000 = PendingIntent.getBroadcast(
            context,
            9000,
            cancel9000,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pi9000 != null) {
            alarmManager.cancel(pi9000)
            pi9000.cancel()
        }

        // Step 1: If settings.alarmsEnabled is false: skip steps 2–6 (meal and weigh-in alarms).
        val canScheduleMeals = settings.alarmsEnabled && (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms())
        val scheduledDiagnosticsList = mutableListOf<ScheduledAlarmInfo>()

        if (canScheduleMeals) {
            // Step 2: For logical today and logical tomorrow, build the DayPlan.
            for (day in targetDays) {
                val epochDay = day.toEpochDay()
                val overrides = trackerDao.getOverridesForDay(epochDay)
                val habit = trackerDao.getHabitForDay(epochDay)

                // Visible sections on this day
                val visibleSections = allSections.filter { sec ->
                    VisibilityResolver.isSectionVisible(sec, epochDay) &&
                    overrides.firstOrNull { it.targetType == "SECTION" && it.targetId == sec.id }?.action != "REMOVE"
                }

                val busyBlocks = container.scheduleRepository.getBusyBlocks(day)

                val dayPlan = container.dayPlanner.plan(
                    day = day,
                    sections = visibleSections,
                    overrides = overrides,
                    busy = busyBlocks,
                    habit = habit,
                    settings = settings
                )

                val sectionMap = allSections.associateBy { it.id }

                for (slot in dayPlan.slots) {
                    if (slot.status in listOf("NORMAL", "MOVED", "PINNED")) {
                        val section = sectionMap[slot.sectionId] ?: continue
                        if (section.alarmEnabled) {
                            val triggerDateTime = DateUtils.moldToLocalDateTime(day, slot.mold)
                            if (triggerDateTime.isAfter(now)) {
                                val triggerMillis = triggerDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                                val k = ((epochDay % 4 + 4) % 4).toInt()
                                val s = (slot.sectionId % 1000).toInt()
                                val requestCode = 100000 + k * 1000 + s

                                val showIntent = Intent(context, MainActivity::class.java).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                                    putExtra("epochDay", epochDay)
                                }
                                val showPendingIntent = PendingIntent.getActivity(
                                    context,
                                    requestCode,
                                    showIntent,
                                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                                )

                                val alarmIntent = Intent(context, AlarmReceiver::class.java).apply {
                                    action = AlarmReceiver.ACTION_ALARM_TRIGGER
                                    putExtra("type", "MAIN")
                                    putExtra("epochDay", epochDay)
                                    putExtra("sectionId", slot.sectionId)
                                }
                                val alarmPendingIntent = PendingIntent.getBroadcast(
                                    context,
                                    requestCode,
                                    alarmIntent,
                                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                                )

                                try {
                                    alarmManager.setAlarmClock(
                                        AlarmManager.AlarmClockInfo(triggerMillis, showPendingIntent),
                                        alarmPendingIntent
                                    )
                                    val dayName = day.format(java.time.format.DateTimeFormatter.ofPattern("EEE"))
                                    scheduledDiagnosticsList.add(
                                        ScheduledAlarmInfo(
                                            sectionName = section.name,
                                            triggerTimeFormatted = "$dayName ${DateUtils.formatMold(slot.mold)}"
                                        )
                                    )
                                } catch (e: SecurityException) {
                                    Log.w(TAG, "Cannot schedule exact alarm for slot ${slot.sectionId}", e)
                                }
                            }
                        }
                    }
                }
            }
        }
        AlarmDiagnostics.recordScheduledAlarms(scheduledDiagnosticsList)

        // Step 7: Maintenance alarm (always scheduled, whatever the switches say):
        // setExactAndAllowWhileIdle at the next occurrence of dayStartHour:05 (4:05 AM by default), requestCode 9100, extras type "MAINTENANCE".
        try {
            var maintenanceTime = now.toLocalDate().atTime(settings.dayStartHour, 5)
            if (!maintenanceTime.isAfter(now)) {
                maintenanceTime = maintenanceTime.plusDays(1)
            }
            val maintenanceMillis = maintenanceTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val maintenanceIntent = Intent(context, AlarmReceiver::class.java).apply {
                action = AlarmReceiver.ACTION_ALARM_TRIGGER
                putExtra("type", "MAINTENANCE")
            }
            val maintenancePendingIntent = PendingIntent.getBroadcast(
                context,
                9100,
                maintenanceIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        maintenanceMillis,
                        maintenancePendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        maintenanceMillis,
                        maintenancePendingIntent
                    )
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    maintenanceMillis,
                    maintenancePendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    maintenanceMillis,
                    maintenancePendingIntent
                )
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to schedule maintenance alarm", e)
        }
    }
}
