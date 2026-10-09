package com.bulktracker.app.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "sections")
data class SectionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val role: String, // "MORNING_SHAKE" | "SNACK_1" | "LUNCH" | "SNACK_2" | "DINNER" | "NIGHT_SHAKE" | "CUSTOM"
    val size: String, // "BIG" | "SNACK"
    val timing: String, // "WAKE_RELATIVE" | "FIXED" | "FLEXIBLE" | "AFTER_GYM"
    val idealMold: Int,
    val durationMin: Int,
    val colorHex: String,
    val sortOrder: Int,
    val isMealSection: Boolean,
    val alarmEnabled: Boolean = true,
    val activeFromDay: Long,
    val activeUntilDay: Long? = null,
    val weekdaysMask: Int = 127
)

@Entity(
    tableName = "food_items",
    indices = [Index(value = ["sectionId"])]
)
data class FoodItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sectionId: Long,
    val name: String,
    val portionLabel: String,
    val kcal: Int? = null,
    val proteinG: Double? = null,
    val isPriority: Boolean = false,
    val isOptional: Boolean = false,
    val sortOrder: Int,
    val activeFromDay: Long,
    val activeUntilDay: Long? = null,
    val weekdaysMask: Int = 127
)

@Entity(
    tableName = "day_overrides",
    indices = [Index(value = ["epochDay", "targetType", "targetId"], unique = true)]
)
data class DayOverrideEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val epochDay: Long,
    val targetType: String, // "ITEM" | "SECTION"
    val targetId: Long,
    val action: String, // "ADD" | "REMOVE" | "SKIP" | "MERGE_INTO" | "PIN_TIME"
    val mergeIntoSectionId: Long? = null,
    val pinnedMold: Int? = null
)

@Entity(
    tableName = "log_entries",
    indices = [Index(value = ["epochDay"])]
)
data class LogEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val epochDay: Long,
    val sectionId: Long,
    val foodItemId: Long? = null,
    val nameSnapshot: String,
    val portionSnapshot: String,
    val kcalSnapshot: Int? = null,
    val proteinSnapshot: Double? = null,
    val multiplier: Double = 1.0,
    val isCustom: Boolean,
    val createdAtMillis: Long
)

@Entity(tableName = "daily_habits")
data class DailyHabitEntity(
    @PrimaryKey val epochDay: Long,
    val waterGlasses: Int = 0,
    val sunlight: Boolean = false,
    val gym: Boolean = false,
    val note: String = "",
    val gymPlan: String = "DEFAULT", // "DEFAULT" | "REST" | "TRAIN"
    val bareMinimum: Boolean = false
)

@Entity(tableName = "weights")
data class WeightEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val epochDay: Long,
    val kg: Double,
    val note: String = ""
)

@Entity(tableName = "semester")
data class SemesterEntity(
    @PrimaryKey val id: Int = 1,
    val startDay: Long,
    val endDay: Long
)

@Entity(tableName = "holidays")
data class HolidayEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val startDay: Long,
    val endDay: Long
)

@Entity(tableName = "classes")
data class ClassEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val weekday: Int, // 1 = Monday ... 7 = Sunday
    val startMold: Int,
    val endMold: Int,
    val location: String = ""
)

@Entity(
    tableName = "class_cancellations",
    indices = [Index(value = ["classId", "epochDay"], unique = true)]
)
data class ClassCancellationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val classId: Long,
    val epochDay: Long
)

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val kind: String, // "PERSONAL" | "EXTRA_CLASS"
    val title: String,
    val startDay: Long,
    val startMold: Int,
    val endMold: Int,
    val repeatType: String = "NONE", // "NONE" | "DAILY" | "WEEKLY" | "MONTHLY"
    val repeatInterval: Int = 1,
    val weekdaysMask: Int = 0,
    val endType: String = "NEVER", // "NEVER" | "UNTIL" | "COUNT"
    val untilDay: Long? = null,
    val count: Int? = null,
    val note: String = ""
)

@Entity(
    tableName = "event_exceptions",
    indices = [Index(value = ["eventId", "epochDay"], unique = true)]
)
data class EventExceptionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val eventId: Long,
    val epochDay: Long,
    val type: String, // "CANCELLED" | "MOVED"
    val newStartMold: Int? = null,
    val newEndMold: Int? = null
)

@Entity(tableName = "task_lists")
data class TaskListEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val colorHex: String,
    val sortOrder: Int
)

@Entity(
    tableName = "tasks",
    indices = [
        Index(value = ["listId"]),
        Index(value = ["dueDay"]),
        Index(value = ["completedAtMillis"])
    ]
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val listId: Long? = null,
    val title: String,
    val notes: String = "",
    val priority: Int = 0,
    val dueDay: Long? = null,
    val dueMold: Int? = null,
    val tags: String = "",
    val repeatType: String = "NONE",
    val repeatInterval: Int = 1,
    val repeatWeekdaysMask: Int = 0,
    val repeatStartDay: Long? = null,
    val completedAtMillis: Long? = null,
    val createdAtMillis: Long
)

@Entity(
    tableName = "subtasks",
    indices = [Index(value = ["taskId"])]
)
data class SubtaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskId: Long,
    val title: String,
    val done: Boolean = false,
    val sortOrder: Int
)

@Entity(
    tableName = "task_reminders",
    indices = [Index(value = ["taskId"])]
)
data class TaskReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskId: Long,
    val offsetMin: Int,
    val style: String // "NOTIFICATION" | "ALARM"
)

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey val id: Int = 1,
    val kcalTarget: Int = 3200,
    val proteinTarget: Double = 115.0,
    val waterTargetGlasses: Int = 12,
    val goalWeightKg: Double = 54.0,
    val weeklyGainTargetKg: Double = 0.4,
    val dayStartHour: Int = 4,
    val themeMode: String = "system",
    val defaultTimeline: Boolean = false,
    val wakeBeforeFirstClassMin: Int = 90,
    val noClassWakeMold: Int = 540,
    val morningShakeAfterWakeMin: Int = 30,
    val minGapBigMin: Int = 150,
    val minGapSnackMin: Int = 90,
    val gymAfterDinnerMin: Int = 60,
    val gymDurationMin: Int = 60,
    val nightShakeAfterGymMin: Int = 45,
    val latestNightShakeMold: Int = 1470,
    val gymRestDaysMask: Int = 0,
    val restDayNightShakeAfterDinnerMin: Int = 150,
    val alarmsEnabled: Boolean = false,
    val alarmVolumePercent: Int = 50,
    val alarmRingSeconds: Int = 60,
    val snoozeMin: Int = 10,
    val maxSnoozes: Int = 3,
    val followUpMin: Int = 45,
    val weighInAlarmEnabled: Boolean = true,
    val lastExportMillis: Long? = null,
    val autoBackupEnabled: Boolean = false,
    val autoBackupTreeUri: String? = null,
    val lastAutoBackupMillis: Long? = null,
    val maxFixedShiftMin: Int = 120,
    val savedAlarmVolume: Int? = null,
    val onboardingDone: Boolean = false
)
