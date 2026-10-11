// SPDX-License-Identifier: GPL-3.0-or-later
package com.otasks.app.data.db
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface PreferenceDao {
    @Query("SELECT * FROM preferences")
    fun observe(): Flow<List<PreferenceEntity>>

    @Query("SELECT * FROM preferences")
    suspend fun all(): List<PreferenceEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(row: PreferenceEntity): Long

    @Upsert suspend fun save(row: PreferenceEntity)

    @Delete suspend fun delete(row: PreferenceEntity)
}

@Dao
interface AlarmProfileDao {
    @Query("SELECT * FROM alarm_profiles")
    fun observe(): Flow<List<AlarmProfileEntity>>

    @Query("SELECT * FROM alarm_profiles")
    suspend fun all(): List<AlarmProfileEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(row: AlarmProfileEntity): Long

    @Upsert suspend fun save(row: AlarmProfileEntity)

    @Delete suspend fun delete(row: AlarmProfileEntity)
}

@Dao
interface WeekdayGoalDao {
    @Query("SELECT * FROM weekday_goals")
    fun observe(): Flow<List<WeekdayGoalEntity>>

    @Query("SELECT * FROM weekday_goals")
    suspend fun all(): List<WeekdayGoalEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(row: WeekdayGoalEntity): Long

    @Upsert suspend fun save(row: WeekdayGoalEntity)

    @Delete suspend fun delete(row: WeekdayGoalEntity)
}

@Dao
interface FoodDao {
    @Query("SELECT * FROM foods")
    fun observe(): Flow<List<FoodEntity>>

    @Query("SELECT * FROM foods")
    suspend fun all(): List<FoodEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(row: FoodEntity): Long

    @Upsert suspend fun save(row: FoodEntity)

    @Delete suspend fun delete(row: FoodEntity)
}

@Dao
interface RecipeIngredientDao {
    @Query("SELECT * FROM recipe_ingredients")
    fun observe(): Flow<List<RecipeIngredientEntity>>

    @Query("SELECT * FROM recipe_ingredients")
    suspend fun all(): List<RecipeIngredientEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(row: RecipeIngredientEntity): Long

    @Upsert suspend fun save(row: RecipeIngredientEntity)

    @Delete suspend fun delete(row: RecipeIngredientEntity)
}

@Dao
interface SectionDao {
    @Query("SELECT * FROM sections")
    fun observe(): Flow<List<SectionEntity>>

    @Query("SELECT * FROM sections")
    suspend fun all(): List<SectionEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(row: SectionEntity): Long

    @Upsert suspend fun save(row: SectionEntity)

    @Delete suspend fun delete(row: SectionEntity)
}

@Dao
interface MealItemDao {
    @Query("SELECT * FROM meal_items")
    fun observe(): Flow<List<MealItemEntity>>

    @Query("SELECT * FROM meal_items")
    suspend fun all(): List<MealItemEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(row: MealItemEntity): Long

    @Upsert suspend fun save(row: MealItemEntity)

    @Delete suspend fun delete(row: MealItemEntity)
}

@Dao
interface DayOverrideDao {
    @Query("SELECT * FROM day_overrides")
    fun observe(): Flow<List<DayOverrideEntity>>

    @Query("SELECT * FROM day_overrides")
    suspend fun all(): List<DayOverrideEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(row: DayOverrideEntity): Long

    @Upsert suspend fun save(row: DayOverrideEntity)

    @Delete suspend fun delete(row: DayOverrideEntity)
}

@Dao
interface LogEntryDao {
    @Query("SELECT * FROM log_entries")
    fun observe(): Flow<List<LogEntryEntity>>

    @Query("SELECT * FROM log_entries")
    suspend fun all(): List<LogEntryEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(row: LogEntryEntity): Long

    @Upsert suspend fun save(row: LogEntryEntity)

    @Delete suspend fun delete(row: LogEntryEntity)
}

@Dao
interface DayStateDao {
    @Query("SELECT * FROM day_states")
    fun observe(): Flow<List<DayStateEntity>>

    @Query("SELECT * FROM day_states")
    suspend fun all(): List<DayStateEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(row: DayStateEntity): Long

    @Upsert suspend fun save(row: DayStateEntity)

    @Delete suspend fun delete(row: DayStateEntity)
}

@Dao
interface HistoryDayDao {
    @Query("SELECT * FROM history_days")
    fun observe(): Flow<List<HistoryDayEntity>>

    @Query("SELECT * FROM history_days")
    suspend fun all(): List<HistoryDayEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(row: HistoryDayEntity): Long
}

@Dao
interface HistoryItemDao {
    @Query("SELECT * FROM history_items")
    fun observe(): Flow<List<HistoryItemEntity>>

    @Query("SELECT * FROM history_items")
    suspend fun all(): List<HistoryItemEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(row: HistoryItemEntity): Long
}

@Dao
interface WeightDao {
    @Query("SELECT * FROM weights")
    fun observe(): Flow<List<WeightEntity>>

    @Query("SELECT * FROM weights")
    suspend fun all(): List<WeightEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(row: WeightEntity): Long

    @Upsert suspend fun save(row: WeightEntity)

    @Delete suspend fun delete(row: WeightEntity)
}

@Dao
interface SemesterDao {
    @Query("SELECT * FROM semesters")
    fun observe(): Flow<List<SemesterEntity>>

    @Query("SELECT * FROM semesters")
    suspend fun all(): List<SemesterEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(row: SemesterEntity): Long

    @Upsert suspend fun save(row: SemesterEntity)

    @Delete suspend fun delete(row: SemesterEntity)
}

@Dao
interface HolidayDao {
    @Query("SELECT * FROM holidays")
    fun observe(): Flow<List<HolidayEntity>>

    @Query("SELECT * FROM holidays")
    suspend fun all(): List<HolidayEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(row: HolidayEntity): Long

    @Upsert suspend fun save(row: HolidayEntity)

    @Delete suspend fun delete(row: HolidayEntity)
}

@Dao
interface CourseDao {
    @Query("SELECT * FROM courses")
    fun observe(): Flow<List<CourseEntity>>

    @Query("SELECT * FROM courses")
    suspend fun all(): List<CourseEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(row: CourseEntity): Long

    @Upsert suspend fun save(row: CourseEntity)

    @Delete suspend fun delete(row: CourseEntity)
}

@Dao
interface ClassSessionDao {
    @Query("SELECT * FROM class_sessions")
    fun observe(): Flow<List<ClassSessionEntity>>

    @Query("SELECT * FROM class_sessions")
    suspend fun all(): List<ClassSessionEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(row: ClassSessionEntity): Long

    @Upsert suspend fun save(row: ClassSessionEntity)

    @Delete suspend fun delete(row: ClassSessionEntity)
}

@Dao
interface ClassExceptionDao {
    @Query("SELECT * FROM class_exceptions")
    fun observe(): Flow<List<ClassExceptionEntity>>

    @Query("SELECT * FROM class_exceptions")
    suspend fun all(): List<ClassExceptionEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(row: ClassExceptionEntity): Long

    @Upsert suspend fun save(row: ClassExceptionEntity)

    @Delete suspend fun delete(row: ClassExceptionEntity)
}

@Dao
interface EventDao {
    @Query("SELECT * FROM events")
    fun observe(): Flow<List<EventEntity>>

    @Query("SELECT * FROM events")
    suspend fun all(): List<EventEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(row: EventEntity): Long

    @Upsert suspend fun save(row: EventEntity)

    @Delete suspend fun delete(row: EventEntity)
}

@Dao
interface EventExceptionDao {
    @Query("SELECT * FROM event_exceptions")
    fun observe(): Flow<List<EventExceptionEntity>>

    @Query("SELECT * FROM event_exceptions")
    suspend fun all(): List<EventExceptionEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(row: EventExceptionEntity): Long

    @Upsert suspend fun save(row: EventExceptionEntity)

    @Delete suspend fun delete(row: EventExceptionEntity)
}

@Dao
interface TaskListDao {
    @Query("SELECT * FROM task_lists")
    fun observe(): Flow<List<TaskListEntity>>

    @Query("SELECT * FROM task_lists")
    suspend fun all(): List<TaskListEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(row: TaskListEntity): Long

    @Upsert suspend fun save(row: TaskListEntity)

    @Delete suspend fun delete(row: TaskListEntity)
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks")
    fun observe(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks")
    suspend fun all(): List<TaskEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(row: TaskEntity): Long

    @Upsert suspend fun save(row: TaskEntity)

    @Delete suspend fun delete(row: TaskEntity)
}

@Dao
interface SubtaskDao {
    @Query("SELECT * FROM subtasks")
    fun observe(): Flow<List<SubtaskEntity>>

    @Query("SELECT * FROM subtasks")
    suspend fun all(): List<SubtaskEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(row: SubtaskEntity): Long

    @Upsert suspend fun save(row: SubtaskEntity)

    @Delete suspend fun delete(row: SubtaskEntity)
}

@Dao
interface TagDao {
    @Query("SELECT * FROM tags")
    fun observe(): Flow<List<TagEntity>>

    @Query("SELECT * FROM tags")
    suspend fun all(): List<TagEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(row: TagEntity): Long

    @Upsert suspend fun save(row: TagEntity)

    @Delete suspend fun delete(row: TagEntity)
}

@Dao
interface TaskTagDao {
    @Query("SELECT * FROM task_tags")
    fun observe(): Flow<List<TaskTagEntity>>

    @Query("SELECT * FROM task_tags")
    suspend fun all(): List<TaskTagEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(row: TaskTagEntity): Long

    @Upsert suspend fun save(row: TaskTagEntity)

    @Delete suspend fun delete(row: TaskTagEntity)
}

@Dao
interface TaskReminderDao {
    @Query("SELECT * FROM task_reminders")
    fun observe(): Flow<List<TaskReminderEntity>>

    @Query("SELECT * FROM task_reminders")
    suspend fun all(): List<TaskReminderEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(row: TaskReminderEntity): Long

    @Upsert suspend fun save(row: TaskReminderEntity)

    @Delete suspend fun delete(row: TaskReminderEntity)
}

@Dao
interface AlarmLogDao {
    @Query("SELECT * FROM alarm_log")
    fun observe(): Flow<List<AlarmLogEntity>>

    @Query("SELECT * FROM alarm_log")
    suspend fun all(): List<AlarmLogEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(row: AlarmLogEntity): Long

    @Upsert suspend fun save(row: AlarmLogEntity)

    @Delete suspend fun delete(row: AlarmLogEntity)
}

@Dao
interface AlarmStateDao {
    @Query("SELECT * FROM alarm_state")
    fun observe(): Flow<List<AlarmStateEntity>>

    @Query("SELECT * FROM alarm_state")
    suspend fun all(): List<AlarmStateEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(row: AlarmStateEntity): Long

    @Upsert suspend fun save(row: AlarmStateEntity)

    @Delete suspend fun delete(row: AlarmStateEntity)
}
