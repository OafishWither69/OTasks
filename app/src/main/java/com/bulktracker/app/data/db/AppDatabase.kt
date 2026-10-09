package com.bulktracker.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        SectionEntity::class,
        FoodItemEntity::class,
        DayOverrideEntity::class,
        LogEntryEntity::class,
        DailyHabitEntity::class,
        WeightEntity::class,
        SemesterEntity::class,
        HolidayEntity::class,
        ClassEntity::class,
        ClassCancellationEntity::class,
        EventEntity::class,
        EventExceptionEntity::class,
        TaskListEntity::class,
        TaskEntity::class,
        SubtaskEntity::class,
        TaskReminderEntity::class,
        SettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun trackerDao(): TrackerDao
    abstract fun scheduleDao(): ScheduleDao
    abstract fun taskDao(): TaskDao
}
