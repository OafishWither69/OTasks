// SPDX-License-Identifier: GPL-3.0-or-later
package com.otasks.app.data.db
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "preferences")
data class PreferenceEntity(
    @PrimaryKey val key: String,
    val value: String,
)

@Entity(tableName = "alarm_profiles")
data class AlarmProfileEntity(
    @PrimaryKey val kind: String,
    val useDefault: Boolean = true,
    val soundUri: String? = null,
    val volumeMode: String = "SYSTEM",
    val volumePercent: Int = 70,
    val rampSeconds: Int = 0,
    val vibrationPattern: String = "STANDARD",
    val ringSeconds: Int = 60,
    val snoozeMin: Int = 10,
    val maxSnoozes: Int = 3,
    val flashScreen: Boolean = false,
    val whenNormal: String = "SOUND_VIBRATE",
    val whenVibrate: String = "VIBRATE",
    val whenSilent: String = "VIBRATE",
    val whenDnd: String = "VIBRATE",
    val whenInCall: String = "VIBRATE",
)

@Entity(tableName = "weekday_goals")
data class WeekdayGoalEntity(
    @PrimaryKey val weekday: Int,
    val kcalTarget: Int? = null,
    val proteinTargetG: Double? = null,
)

@Entity(tableName = "foods", indices = [Index(value = ["name"])])
data class FoodEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val basis: String,
    val servingLabel: String = "",
    val recipeServings: Double? = null,
    val isPartial: Boolean = false,
    val defaultGrams: Double? = null,
    val kcal: Int? = null,
    val proteinG: Double? = null,
    val isFavorite: Boolean = false,
    val isArchived: Boolean = false,
    val createdAtMillis: Long,
    val lastUsedAtMillis: Long,
)

@Entity(
    tableName = "recipe_ingredients",
    foreignKeys = [
        ForeignKey(
            entity = FoodEntity::class,
            parentColumns = ["id"],
            childColumns = ["recipeFoodId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = FoodEntity::class,
            parentColumns = ["id"],
            childColumns = ["ingredientFoodId"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [
        Index(value = ["ingredientFoodId"]),
        Index(value = ["recipeFoodId"]),
    ],
)
data class RecipeIngredientEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val recipeFoodId: Long,
    val ingredientFoodId: Long,
    val servings: Double? = null,
    val grams: Double? = null,
    val sortOrder: Int,
)

@Entity(tableName = "sections")
data class SectionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val size: String,
    val timing: String,
    val idealMold: Int,
    val offsetMin: Int = 0,
    val anchorSectionId: Long? = null,
    val restOffsetMin: Int? = null,
    val durationMin: Int,
    val colorKey: String,
    val sortOrder: Int,
    val alarmEnabled: Boolean = true,
    val activeFromDay: Long,
    val activeUntilDay: Long? = null,
    val weekdaysMask: Int = 127,
)

@Entity(
    tableName = "meal_items",
    foreignKeys = [
        ForeignKey(
            entity = SectionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sectionId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = FoodEntity::class,
            parentColumns = ["id"],
            childColumns = ["foodId"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [
        Index(value = ["foodId"]),
        Index(value = ["sectionId"]),
    ],
)
data class MealItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sectionId: Long,
    val foodId: Long,
    val servings: Double = 1.0,
    val grams: Double? = null,
    val isPriority: Boolean = false,
    val isOptional: Boolean = false,
    val sortOrder: Int,
    val activeFromDay: Long,
    val activeUntilDay: Long? = null,
    val weekdaysMask: Int = 127,
)

@Entity(tableName = "day_overrides", indices = [Index(value = ["epochDay", "targetType", "targetId"], unique = true)])
data class DayOverrideEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val epochDay: Long,
    val targetType: String,
    val targetId: Long,
    val action: String,
    val mergeIntoSectionId: Long? = null,
    val pinnedMold: Int? = null,
)

@Entity(tableName = "log_entries", indices = [Index(value = ["epochDay", "mealItemId"], unique = true)])
data class LogEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val epochDay: Long,
    val sectionId: Long,
    val sectionNameSnapshot: String,
    val mealItemId: Long? = null,
    val foodId: Long? = null,
    val nameSnapshot: String,
    val portionSnapshot: String,
    val kcalSnapshot: Int? = null,
    val proteinSnapshot: Double? = null,
    val multiplier: Double = 1.0,
    val isCustom: Boolean,
    val partialSnapshot: Boolean = false,
    val createdAtMillis: Long,
)

@Entity(tableName = "day_states")
data class DayStateEntity(
    @PrimaryKey val epochDay: Long,
    val bareMinimum: Boolean = false,
    val alarmsPaused: Boolean = false,
    val workoutPlan: String = "DEFAULT",
)

@Entity(tableName = "history_days")
data class HistoryDayEntity(
    @PrimaryKey val epochDay: Long,
    val goalType: String,
    val kcalTarget: Int? = null,
    val proteinTargetG: Double? = null,
    val targetSource: String,
    val bareMinimum: Boolean,
    val isHoliday: Boolean,
)

@Entity(
    tableName = "history_items",
    primaryKeys = [
        "epochDay",
        "mealItemId",
    ],
    foreignKeys = [
        ForeignKey(
            entity = HistoryDayEntity::class,
            parentColumns = ["epochDay"],
            childColumns = ["epochDay"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["epochDay"])],
)
data class HistoryItemEntity(
    val epochDay: Long,
    val mealItemId: Long,
    val sectionId: Long,
    val sectionName: String,
    val sectionColorKey: String,
    val sectionSortOrder: Int,
    val plannedMold: Int? = null,
    val durationMin: Int,
    val slotStatus: String,
    val itemSortOrder: Int,
    val name: String,
    val portion: String,
    val kcal: Int? = null,
    val proteinG: Double? = null,
    val partial: Boolean,
    val isPriority: Boolean,
    val isOptional: Boolean,
    val sectionSkipped: Boolean,
)

@Entity(tableName = "weights", indices = [Index(value = ["epochDay"], unique = true)])
data class WeightEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val epochDay: Long,
    val kg: Double,
    val note: String = "",
)

@Entity(tableName = "semesters")
data class SemesterEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val startDay: Long,
    val endDay: Long,
)

@Entity(
    tableName = "holidays",
    foreignKeys = [
        ForeignKey(
            entity = SemesterEntity::class,
            parentColumns = ["id"],
            childColumns = ["semesterId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["semesterId"])],
)
data class HolidayEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val semesterId: Long,
    val name: String,
    val startDay: Long,
    val endDay: Long,
)

@Entity(
    tableName = "courses",
    foreignKeys = [
        ForeignKey(
            entity = SemesterEntity::class,
            parentColumns = ["id"],
            childColumns = ["semesterId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["semesterId"])],
)
data class CourseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val semesterId: Long,
    val name: String,
    val code: String,
    val location: String,
    val colorKey: String,
)

@Entity(
    tableName = "class_sessions",
    foreignKeys = [
        ForeignKey(
            entity = CourseEntity::class,
            parentColumns = ["id"],
            childColumns = ["courseId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["courseId"])],
)
data class ClassSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val courseId: Long,
    val weekday: Int,
    val startMold: Int,
    val endMold: Int,
    val location: String? = null,
    val intervalWeeks: Int = 1,
    val weekOffset: Int = 0,
)

@Entity(
    tableName = "class_exceptions",
    foreignKeys = [
        ForeignKey(
            entity = ClassSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(
            value = [
                "sessionId",
                "epochDay",
            ],
            unique = true,
        ),
    ],
)
data class ClassExceptionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val epochDay: Long,
    val type: String,
    val newStartMold: Int? = null,
    val newEndMold: Int? = null,
)

@Entity(
    tableName = "events",
    foreignKeys = [
        ForeignKey(
            entity = CourseEntity::class,
            parentColumns = ["id"],
            childColumns = ["courseId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [
        Index(
            value = [
                "importUid",
                "importRecurrenceId",
            ],
            unique = true,
        ),
        Index(value = ["courseId"]),
    ],
)
data class EventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val kind: String,
    val courseId: Long? = null,
    val title: String,
    val colorKey: String,
    val location: String,
    val note: String,
    val allDay: Boolean = false,
    val startDay: Long,
    val startMold: Int,
    val endMold: Int,
    val repeatType: String,
    val repeatInterval: Int = 1,
    val weekdaysMask: Int = 0,
    val endType: String = "NEVER",
    val untilDay: Long? = null,
    val count: Int? = null,
    val importUid: String? = null,
    val importRecurrenceId: String = "",
)

@Entity(
    tableName = "event_exceptions",
    foreignKeys = [
        ForeignKey(
            entity = EventEntity::class,
            parentColumns = ["id"],
            childColumns = ["eventId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(
            value = [
                "eventId",
                "epochDay",
            ],
            unique = true,
        ),
    ],
)
data class EventExceptionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val eventId: Long,
    val epochDay: Long,
    val type: String,
    val newStartMold: Int? = null,
    val newEndMold: Int? = null,
)

@Entity(tableName = "task_lists")
data class TaskListEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val colorKey: String,
    val sortOrder: Int,
)

@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = TaskListEntity::class,
            parentColumns = ["id"],
            childColumns = ["listId"],
            onDelete = ForeignKey.SET_NULL,
        ),
        ForeignKey(
            entity = CourseEntity::class,
            parentColumns = ["id"],
            childColumns = ["courseId"],
            onDelete = ForeignKey.SET_NULL,
        ),
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["previousOccurrenceId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [
        Index(
            value = [
                "importUid",
                "importRecurrenceId",
            ],
            unique = true,
        ),
        Index(
            value = ["previousOccurrenceId"],
            unique = true,
        ),
        Index(value = ["completedAtMillis"]),
        Index(value = ["courseId"]),
        Index(value = ["deletedAtMillis"]),
        Index(value = ["dueDay"]),
        Index(value = ["listId"]),
    ],
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val listId: Long? = null,
    val title: String,
    val notes: String,
    val priority: Int = 0,
    val dueDay: Long? = null,
    val dueMold: Int? = null,
    val courseId: Long? = null,
    val repeatType: String = "NONE",
    val repeatInterval: Int = 1,
    val repeatWeekdaysMask: Int = 0,
    val repeatStartDay: Long? = null,
    val repeatFrom: String = "DUE_DATE",
    val completedAtMillis: Long? = null,
    val closeType: String = "DONE",
    val deletedAtMillis: Long? = null,
    val importUid: String? = null,
    val importRecurrenceId: String = "",
    val previousOccurrenceId: Long? = null,
    val createdAtMillis: Long,
    val sortOrder: Long,
)

@Entity(
    tableName = "subtasks",
    foreignKeys = [
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["taskId"])],
)
data class SubtaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskId: Long,
    val title: String,
    val done: Boolean,
    val sortOrder: Int,
)

@Entity(tableName = "tags", indices = [Index(value = ["name"], unique = true)])
data class TagEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
)

@Entity(
    tableName = "task_tags",
    primaryKeys = [
        "taskId",
        "tagId",
    ],
    foreignKeys = [
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = TagEntity::class,
            parentColumns = ["id"],
            childColumns = ["tagId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["tagId"]),
        Index(value = ["taskId"]),
    ],
)
data class TaskTagEntity(
    val taskId: Long,
    val tagId: Long,
)

@Entity(
    tableName = "task_reminders",
    foreignKeys = [
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["taskId"])],
)
data class TaskReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskId: Long,
    val offsetMin: Int,
    val style: String,
)

@Entity(tableName = "alarm_log")
data class AlarmLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val atMillis: Long,
    val event: String,
    val kind: String,
    val refId: Long? = null,
    val detail: String,
)

@Entity(tableName = "alarm_state")
data class AlarmStateEntity(
    @PrimaryKey val uri: String,
    val kind: String,
    val refId: Long? = null,
    val epochDay: Long? = null,
    val triggerMillis: Long? = null,
    val snoozeCount: Int = 0,
    val consumed: Boolean = false,
    val noticeSent: Boolean = false,
)
