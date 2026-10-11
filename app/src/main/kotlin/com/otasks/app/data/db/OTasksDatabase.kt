// SPDX-License-Identifier: GPL-3.0-or-later
package com.otasks.app.data.db
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        PreferenceEntity::class,
        AlarmProfileEntity::class,
        WeekdayGoalEntity::class,
        FoodEntity::class,
        RecipeIngredientEntity::class,
        SectionEntity::class,
        MealItemEntity::class,
        DayOverrideEntity::class,
        LogEntryEntity::class,
        DayStateEntity::class,
        HistoryDayEntity::class,
        HistoryItemEntity::class,
        WeightEntity::class,
        SemesterEntity::class,
        HolidayEntity::class,
        CourseEntity::class,
        ClassSessionEntity::class,
        ClassExceptionEntity::class,
        EventEntity::class,
        EventExceptionEntity::class,
        TaskListEntity::class,
        TaskEntity::class,
        SubtaskEntity::class,
        TagEntity::class,
        TaskTagEntity::class,
        TaskReminderEntity::class,
        AlarmLogEntity::class,
        AlarmStateEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class OTasksDatabase : RoomDatabase() {
    abstract fun preferenceDao(): PreferenceDao

    abstract fun alarmProfileDao(): AlarmProfileDao

    abstract fun weekdayGoalDao(): WeekdayGoalDao

    abstract fun foodDao(): FoodDao

    abstract fun recipeIngredientDao(): RecipeIngredientDao

    abstract fun sectionDao(): SectionDao

    abstract fun mealItemDao(): MealItemDao

    abstract fun dayOverrideDao(): DayOverrideDao

    abstract fun logEntryDao(): LogEntryDao

    abstract fun dayStateDao(): DayStateDao

    abstract fun historyDayDao(): HistoryDayDao

    abstract fun historyItemDao(): HistoryItemDao

    abstract fun weightDao(): WeightDao

    abstract fun semesterDao(): SemesterDao

    abstract fun holidayDao(): HolidayDao

    abstract fun courseDao(): CourseDao

    abstract fun classSessionDao(): ClassSessionDao

    abstract fun classExceptionDao(): ClassExceptionDao

    abstract fun eventDao(): EventDao

    abstract fun eventExceptionDao(): EventExceptionDao

    abstract fun taskListDao(): TaskListDao

    abstract fun taskDao(): TaskDao

    abstract fun subtaskDao(): SubtaskDao

    abstract fun tagDao(): TagDao

    abstract fun taskTagDao(): TaskTagDao

    abstract fun taskReminderDao(): TaskReminderDao

    abstract fun alarmLogDao(): AlarmLogDao

    abstract fun alarmStateDao(): AlarmStateDao

    companion object {
        fun open(context: Context): OTasksDatabase =
            Room.databaseBuilder(context, OTasksDatabase::class.java, "otasks.db").addCallback(Defaults).build()

        val Defaults =
            object : Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    listOf("DEFAULT", "MEAL", "TASK", "WEIGH_IN").forEach { kind ->
                        db.execSQL(
                            """
                            INSERT INTO alarm_profiles(kind,useDefault,soundUri,volumeMode,volumePercent,rampSeconds,vibrationPattern,
ringSeconds,snoozeMin,maxSnoozes,flashScreen,whenNormal,whenVibrate,whenSilent,whenDnd,whenInCall)
VALUES (?,1,NULL,'SYSTEM',70,0,'STANDARD',60,10,3,0,'SOUND_VIBRATE','VIBRATE','VIBRATE','VIBRATE','VIBRATE')
                            """.trimIndent(),
                            arrayOf(kind),
                        )
                    }
                }
            }
    }
}
