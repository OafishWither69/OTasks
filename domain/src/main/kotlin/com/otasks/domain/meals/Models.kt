// SPDX-License-Identifier: GPL-3.0-or-later
package com.otasks.domain.meals

data class WeekdayGoal(
    val weekday: Int,
    val kcalTarget: Int? = null,
    val proteinTargetG: Double? = null,
)

data class Food(
    val id: Long = 0,
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

data class Section(
    val id: Long = 0,
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

data class MealItem(
    val id: Long = 0,
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

data class DayOverride(
    val id: Long = 0,
    val epochDay: Long,
    val targetType: String,
    val targetId: Long,
    val action: String,
    val mergeIntoSectionId: Long? = null,
    val pinnedMold: Int? = null,
)

data class LogEntry(
    val id: Long = 0,
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

data class DayState(
    val epochDay: Long,
    val bareMinimum: Boolean = false,
    val alarmsPaused: Boolean = false,
    val workoutPlan: String = "DEFAULT",
)

data class HistoryDay(
    val epochDay: Long,
    val goalType: String,
    val kcalTarget: Int? = null,
    val proteinTargetG: Double? = null,
    val targetSource: String,
    val bareMinimum: Boolean,
    val isHoliday: Boolean,
)

data class HistoryItem(
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
