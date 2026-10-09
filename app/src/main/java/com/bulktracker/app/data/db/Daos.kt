package com.bulktracker.app.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackerDao {
    // Sections
    @Query("SELECT * FROM sections WHERE id = :sectionId")
    suspend fun getSectionById(sectionId: Long): SectionEntity?

    @Query("SELECT * FROM food_items WHERE sectionId = :sectionId ORDER BY sortOrder ASC")
    suspend fun getItemsForSection(sectionId: Long): List<FoodItemEntity>

    @Query("DELETE FROM log_entries WHERE epochDay = :epochDay AND sectionId = :sectionId")
    suspend fun deleteLogsForSectionOnDay(epochDay: Long, sectionId: Long)

    @Query("DELETE FROM log_entries WHERE epochDay = :epochDay AND foodItemId = :itemId")
    suspend fun deleteLogsForItemOnDay(epochDay: Long, itemId: Long)

    @Query("UPDATE day_overrides SET targetId = :newSectionId WHERE targetType = 'SECTION' AND targetId = :oldSectionId AND epochDay >= :fromEpochDay")
    suspend fun updateOverridesSectionId(oldSectionId: Long, newSectionId: Long, fromEpochDay: Long)

    @Query("UPDATE day_overrides SET targetId = :newItemId WHERE targetType = 'ITEM' AND targetId = :oldItemId AND epochDay >= :fromEpochDay")
    suspend fun updateOverridesItemId(oldItemId: Long, newItemId: Long, fromEpochDay: Long)

    @Query("SELECT * FROM log_entries WHERE id = :id")
    suspend fun getLogEntryById(id: Long): LogEntryEntity?

    @Query("SELECT COUNT(*) FROM sections")
    suspend fun getSectionCount(): Int

    @Query("SELECT * FROM sections ORDER BY sortOrder ASC")
    fun observeAllSections(): Flow<List<SectionEntity>>

    @Query("SELECT * FROM sections ORDER BY sortOrder ASC")
    suspend fun getAllSections(): List<SectionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSections(sections: List<SectionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSection(section: SectionEntity): Long

    @Update
    suspend fun updateSection(section: SectionEntity)

    // Food Items
    @Query("SELECT * FROM food_items ORDER BY sortOrder ASC")
    fun observeAllFoodItems(): Flow<List<FoodItemEntity>>

    @Query("SELECT * FROM food_items ORDER BY sortOrder ASC")
    suspend fun getAllFoodItems(): List<FoodItemEntity>

    @Query("SELECT * FROM food_items WHERE id = :itemId")
    suspend fun getFoodItemById(itemId: Long): FoodItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoodItems(items: List<FoodItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoodItem(item: FoodItemEntity): Long

    @Update
    suspend fun updateFoodItem(item: FoodItemEntity)

    // Day Overrides
    @Query("SELECT * FROM day_overrides WHERE epochDay = :epochDay")
    fun observeOverridesForDay(epochDay: Long): Flow<List<DayOverrideEntity>>

    @Query("SELECT * FROM day_overrides WHERE epochDay = :epochDay")
    suspend fun getOverridesForDay(epochDay: Long): List<DayOverrideEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDayOverride(override: DayOverrideEntity): Long

    @Query("DELETE FROM day_overrides WHERE epochDay = :epochDay AND targetType = :targetType AND targetId = :targetId")
    suspend fun deleteDayOverride(epochDay: Long, targetType: String, targetId: Long)

    // Log Entries
    @Query("SELECT * FROM log_entries WHERE epochDay = :epochDay ORDER BY id ASC")
    fun observeLogEntriesForDay(epochDay: Long): Flow<List<LogEntryEntity>>

    @Query("SELECT * FROM log_entries WHERE epochDay = :epochDay ORDER BY id ASC")
    suspend fun getLogEntriesForDay(epochDay: Long): List<LogEntryEntity>

    @Query("SELECT * FROM log_entries WHERE epochDay = :epochDay AND foodItemId = :itemId LIMIT 1")
    suspend fun findLogEntry(epochDay: Long, itemId: Long): LogEntryEntity?

    @Query("DELETE FROM log_entries WHERE epochDay = :epochDay AND foodItemId = :itemId")
    suspend fun deleteLogEntryByItem(epochDay: Long, itemId: Long)

    @Query("DELETE FROM log_entries WHERE id = :id")
    suspend fun deleteLogEntryById(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogEntry(entry: LogEntryEntity): Long

    @Update
    suspend fun updateLogEntry(entry: LogEntryEntity)

    @Transaction
    suspend fun toggleItem(epochDay: Long, itemId: Long) {
        val existing = findLogEntry(epochDay, itemId)
        if (existing != null) {
            deleteLogEntryByItem(epochDay, itemId)
        } else {
            val item = getFoodItemById(itemId) ?: return
            val entry = LogEntryEntity(
                epochDay = epochDay,
                sectionId = item.sectionId,
                foodItemId = item.id,
                nameSnapshot = item.name,
                portionSnapshot = item.portionLabel,
                kcalSnapshot = item.kcal,
                proteinSnapshot = item.proteinG,
                multiplier = 1.0,
                isCustom = false,
                createdAtMillis = System.currentTimeMillis()
            )
            insertLogEntry(entry)
        }
    }

    // Daily Habits
    @Query("SELECT * FROM daily_habits WHERE epochDay = :epochDay")
    fun observeHabitForDay(epochDay: Long): Flow<DailyHabitEntity?>

    @Query("SELECT * FROM daily_habits WHERE epochDay = :epochDay")
    suspend fun getHabitForDay(epochDay: Long): DailyHabitEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertHabit(habit: DailyHabitEntity)

    // Weights
    @Query("SELECT * FROM weights ORDER BY epochDay ASC")
    fun observeAllWeights(): Flow<List<WeightEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeight(weight: WeightEntity): Long

    @Delete
    suspend fun deleteWeight(weight: WeightEntity)

    // Settings
    @Query("SELECT * FROM settings WHERE id = 1 LIMIT 1")
    fun observeSettings(): Flow<SettingsEntity?>

    @Query("SELECT * FROM settings WHERE id = 1 LIMIT 1")
    suspend fun getSettings(): SettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSettings(settings: SettingsEntity)
}

@Dao
interface ScheduleDao {
    @Query("SELECT * FROM semester WHERE id = 1 LIMIT 1")
    fun observeSemester(): Flow<SemesterEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSemester(semester: SemesterEntity)

    @Query("SELECT * FROM holidays ORDER BY startDay ASC")
    fun observeAllHolidays(): Flow<List<HolidayEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHoliday(holiday: HolidayEntity): Long

    @Delete
    suspend fun deleteHoliday(holiday: HolidayEntity)

    @Query("SELECT * FROM classes ORDER BY startMold ASC")
    fun observeAllClasses(): Flow<List<ClassEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClass(classEntity: ClassEntity): Long

    @Delete
    suspend fun deleteClass(classEntity: ClassEntity)

    @Query("SELECT * FROM class_cancellations WHERE epochDay = :epochDay")
    fun observeCancellationsForDay(epochDay: Long): Flow<List<ClassCancellationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCancellation(cancellation: ClassCancellationEntity): Long

    @Query("DELETE FROM class_cancellations WHERE classId = :classId AND epochDay = :epochDay")
    suspend fun deleteCancellation(classId: Long, epochDay: Long)

    @Query("SELECT * FROM events ORDER BY startDay ASC, startMold ASC")
    fun observeAllEvents(): Flow<List<EventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EventEntity): Long

    @Update
    suspend fun updateEvent(event: EventEntity)

    @Delete
    suspend fun deleteEvent(event: EventEntity)

    @Query("SELECT * FROM event_exceptions WHERE epochDay = :epochDay")
    fun observeEventExceptionsForDay(epochDay: Long): Flow<List<EventExceptionEntity>>

    @Query("SELECT * FROM event_exceptions")
    fun observeAllEventExceptions(): Flow<List<EventExceptionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertEventException(exception: EventExceptionEntity): Long

    @Query("DELETE FROM event_exceptions WHERE eventId = :eventId")
    suspend fun deleteExceptionsForEvent(eventId: Long)

    @Query("SELECT * FROM semester WHERE id = 1 LIMIT 1")
    suspend fun getSemester(): SemesterEntity?

    @Query("SELECT * FROM holidays ORDER BY startDay ASC")
    suspend fun getAllHolidays(): List<HolidayEntity>

    @Query("DELETE FROM holidays WHERE id = :holidayId")
    suspend fun deleteHolidayById(holidayId: Long)

    @Query("SELECT * FROM classes ORDER BY startMold ASC")
    suspend fun getAllClasses(): List<ClassEntity>

    @Query("DELETE FROM classes WHERE id = :classId")
    suspend fun deleteClassById(classId: Long)

    @Query("SELECT * FROM class_cancellations WHERE epochDay = :epochDay")
    suspend fun getCancellationsForDay(epochDay: Long): List<ClassCancellationEntity>

    @Query("SELECT * FROM events ORDER BY startDay ASC, startMold ASC")
    suspend fun getAllEvents(): List<EventEntity>

    @Query("SELECT * FROM events WHERE id = :eventId")
    suspend fun getEventById(eventId: Long): EventEntity?

    @Query("DELETE FROM events WHERE id = :eventId")
    suspend fun deleteEventById(eventId: Long)

    @Query("SELECT * FROM event_exceptions WHERE epochDay = :epochDay")
    suspend fun getEventExceptionsForDay(epochDay: Long): List<EventExceptionEntity>

    @Query("SELECT * FROM event_exceptions WHERE eventId = :eventId AND epochDay = :epochDay LIMIT 1")
    suspend fun getEventException(eventId: Long, epochDay: Long): EventExceptionEntity?

    @Transaction
    suspend fun deleteEventAndExceptions(eventId: Long) {
        deleteExceptionsForEvent(eventId)
        deleteEventById(eventId)
    }
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM task_lists ORDER BY sortOrder ASC")
    fun observeTaskLists(): Flow<List<TaskListEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTaskList(list: TaskListEntity): Long

    @Delete
    suspend fun deleteTaskList(list: TaskListEntity)

    @Query("SELECT * FROM tasks ORDER BY createdAtMillis ASC")
    fun observeAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: Long): TaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("SELECT * FROM subtasks WHERE taskId = :taskId ORDER BY sortOrder ASC")
    fun observeSubtasksForTask(taskId: Long): Flow<List<SubtaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubtask(subtask: SubtaskEntity): Long

    @Update
    suspend fun updateSubtask(subtask: SubtaskEntity)

    @Delete
    suspend fun deleteSubtask(subtask: SubtaskEntity)

    @Query("SELECT * FROM task_reminders WHERE taskId = :taskId")
    fun observeRemindersForTask(taskId: Long): Flow<List<TaskReminderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: TaskReminderEntity): Long

    @Delete
    suspend fun deleteReminder(reminder: TaskReminderEntity)
}
