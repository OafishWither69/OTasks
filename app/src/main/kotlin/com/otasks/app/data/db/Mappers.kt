// SPDX-License-Identifier: GPL-3.0-or-later
package com.otasks.app.data.db
import com.otasks.domain.AlarmProfile
import com.otasks.domain.meals.DayOverride
import com.otasks.domain.meals.DayState
import com.otasks.domain.meals.Food
import com.otasks.domain.meals.HistoryDay
import com.otasks.domain.meals.HistoryItem
import com.otasks.domain.meals.LogEntry
import com.otasks.domain.meals.MealItem
import com.otasks.domain.meals.Section
import com.otasks.domain.meals.WeekdayGoal

fun WeekdayGoalEntity.toDomain(): WeekdayGoal = WeekdayGoal(weekday, kcalTarget, proteinTargetG)

fun WeekdayGoal.toEntity(): WeekdayGoalEntity = WeekdayGoalEntity(weekday, kcalTarget, proteinTargetG)

fun FoodEntity.toDomain(): Food =
    Food(
        id,
        name,
        basis,
        servingLabel,
        recipeServings,
        isPartial,
        defaultGrams,
        kcal,
        proteinG,
        isFavorite,
        isArchived,
        createdAtMillis,
        lastUsedAtMillis,
    )

fun Food.toEntity(): FoodEntity =
    FoodEntity(
        id,
        name,
        basis,
        servingLabel,
        recipeServings,
        isPartial,
        defaultGrams,
        kcal,
        proteinG,
        isFavorite,
        isArchived,
        createdAtMillis,
        lastUsedAtMillis,
    )

fun SectionEntity.toDomain(): Section =
    Section(
        id,
        name,
        size,
        timing,
        idealMold,
        offsetMin,
        anchorSectionId,
        restOffsetMin,
        durationMin,
        colorKey,
        sortOrder,
        alarmEnabled,
        activeFromDay,
        activeUntilDay,
        weekdaysMask,
    )

fun Section.toEntity(): SectionEntity =
    SectionEntity(
        id,
        name,
        size,
        timing,
        idealMold,
        offsetMin,
        anchorSectionId,
        restOffsetMin,
        durationMin,
        colorKey,
        sortOrder,
        alarmEnabled,
        activeFromDay,
        activeUntilDay,
        weekdaysMask,
    )

fun MealItemEntity.toDomain(): MealItem =
    MealItem(id, sectionId, foodId, servings, grams, isPriority, isOptional, sortOrder, activeFromDay, activeUntilDay, weekdaysMask)

fun MealItem.toEntity(): MealItemEntity =
    MealItemEntity(id, sectionId, foodId, servings, grams, isPriority, isOptional, sortOrder, activeFromDay, activeUntilDay, weekdaysMask)

fun DayOverrideEntity.toDomain(): DayOverride = DayOverride(id, epochDay, targetType, targetId, action, mergeIntoSectionId, pinnedMold)

fun DayOverride.toEntity(): DayOverrideEntity =
    DayOverrideEntity(id, epochDay, targetType, targetId, action, mergeIntoSectionId, pinnedMold)

fun LogEntryEntity.toDomain(): LogEntry =
    LogEntry(
        id,
        epochDay,
        sectionId,
        sectionNameSnapshot,
        mealItemId,
        foodId,
        nameSnapshot,
        portionSnapshot,
        kcalSnapshot,
        proteinSnapshot,
        multiplier,
        isCustom,
        partialSnapshot,
        createdAtMillis,
    )

fun LogEntry.toEntity(): LogEntryEntity =
    LogEntryEntity(
        id,
        epochDay,
        sectionId,
        sectionNameSnapshot,
        mealItemId,
        foodId,
        nameSnapshot,
        portionSnapshot,
        kcalSnapshot,
        proteinSnapshot,
        multiplier,
        isCustom,
        partialSnapshot,
        createdAtMillis,
    )

fun DayStateEntity.toDomain(): DayState = DayState(epochDay, bareMinimum, alarmsPaused, workoutPlan)

fun DayState.toEntity(): DayStateEntity = DayStateEntity(epochDay, bareMinimum, alarmsPaused, workoutPlan)

fun HistoryDayEntity.toDomain(): HistoryDay =
    HistoryDay(epochDay, goalType, kcalTarget, proteinTargetG, targetSource, bareMinimum, isHoliday)

fun HistoryDay.toEntity(): HistoryDayEntity =
    HistoryDayEntity(epochDay, goalType, kcalTarget, proteinTargetG, targetSource, bareMinimum, isHoliday)

fun HistoryItemEntity.toDomain(): HistoryItem =
    HistoryItem(
        epochDay,
        mealItemId,
        sectionId,
        sectionName,
        sectionColorKey,
        sectionSortOrder,
        plannedMold,
        durationMin,
        slotStatus,
        itemSortOrder,
        name,
        portion,
        kcal,
        proteinG,
        partial,
        isPriority,
        isOptional,
        sectionSkipped,
    )

fun HistoryItem.toEntity(): HistoryItemEntity =
    HistoryItemEntity(
        epochDay,
        mealItemId,
        sectionId,
        sectionName,
        sectionColorKey,
        sectionSortOrder,
        plannedMold,
        durationMin,
        slotStatus,
        itemSortOrder,
        name,
        portion,
        kcal,
        proteinG,
        partial,
        isPriority,
        isOptional,
        sectionSkipped,
    )

fun AlarmProfileEntity.toDomain(): AlarmProfile =
    AlarmProfile(
        kind,
        useDefault,
        soundUri,
        volumeMode,
        volumePercent,
        rampSeconds,
        vibrationPattern,
        ringSeconds,
        snoozeMin,
        maxSnoozes,
        flashScreen,
        whenNormal,
        whenVibrate,
        whenSilent,
        whenDnd,
        whenInCall,
    )

fun AlarmProfile.toEntity(): AlarmProfileEntity =
    AlarmProfileEntity(
        kind,
        useDefault,
        soundUri,
        volumeMode,
        volumePercent,
        rampSeconds,
        vibrationPattern,
        ringSeconds,
        snoozeMin,
        maxSnoozes,
        flashScreen,
        whenNormal,
        whenVibrate,
        whenSilent,
        whenDnd,
        whenInCall,
    )
