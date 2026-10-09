package com.bulktracker.app.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.bulktracker.app.MainActivity
import com.bulktracker.app.R
import com.bulktracker.app.data.db.SectionEntity
import com.bulktracker.app.data.db.SettingsEntity

object NotificationHelper {
    const val CHANNEL_MEAL_ALARMS = "meal_alarms"
    const val CHANNEL_MEAL_REMINDERS = "meal_reminders"

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Meal alarms channel: sound & vibration handled by AlarmRingService
            val alarmsChannel = NotificationChannel(
                CHANNEL_MEAL_ALARMS,
                "Meal alarms",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Full-screen alarms and ringing for meals"
                setSound(null, null)
                enableVibration(false)
            }

            // Meal reminders channel: default sound off, vibration on
            val remindersChannel = NotificationChannel(
                CHANNEL_MEAL_REMINDERS,
                "Meal reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Follow-ups and reminders for meals"
                setSound(null, null)
                enableVibration(true)
            }

            notificationManager.createNotificationChannel(alarmsChannel)
            notificationManager.createNotificationChannel(remindersChannel)
        }
    }

    fun showMealReminderNotification(
        context: Context,
        notificationId: Int,
        section: SectionEntity,
        epochDay: Long,
        itemsLeft: Int,
        kcalLeft: Int,
        settings: SettingsEntity,
        isMissed: Boolean = false
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val title = if (isMissed) {
            "Missed: ${section.name} — $itemsLeft items left"
        } else {
            "${section.name} — $itemsLeft items left ($kcalLeft kcal)"
        }

        val k = ((epochDay % 4 + 4) % 4).toInt()
        val s = (section.id % 1000).toInt()

        // 1. Mark all eaten
        val markAllIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_MARK_ALL
            putExtra("epochDay", epochDay)
            putExtra("sectionId", section.id)
            putExtra("notificationId", notificationId)
        }
        val markAllPendingIntent = PendingIntent.getBroadcast(
            context,
            10000 + k * 1000 + s,
            markAllIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 2. Snooze
        val snoozeIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_SNOOZE
            putExtra("epochDay", epochDay)
            putExtra("sectionId", section.id)
            putExtra("notificationId", notificationId)
            putExtra("snoozeCount", 0)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            20000 + k * 1000 + s,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 3. Skip today
        val skipIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_SKIP
            putExtra("epochDay", epochDay)
            putExtra("sectionId", section.id)
            putExtra("notificationId", notificationId)
        }
        val skipPendingIntent = PendingIntent.getBroadcast(
            context,
            30000 + k * 1000 + s,
            skipIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 4. Open app
        val openIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_OPEN
            putExtra("epochDay", epochDay)
            putExtra("notificationId", notificationId)
        }
        val openPendingIntent = PendingIntent.getBroadcast(
            context,
            40000 + k * 1000 + s,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_MEAL_REMINDERS)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText("$itemsLeft items · $kcalLeft kcal")
            .setAutoCancel(true)
            .setContentIntent(openPendingIntent)
            .addAction(0, "Mark all eaten", markAllPendingIntent)
            .addAction(0, "Snooze ${settings.snoozeMin} min", snoozePendingIntent)
            .addAction(0, "Skip today", skipPendingIntent)
            .addAction(0, "Open app", openPendingIntent)

        notificationManager.notify(notificationId, builder.build())
    }
}
