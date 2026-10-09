package com.bulktracker.app.data.repo

import com.bulktracker.app.data.db.DailyHabitEntity
import com.bulktracker.app.data.db.DayOverrideEntity
import com.bulktracker.app.data.db.FoodItemEntity
import com.bulktracker.app.data.db.LogEntryEntity
import com.bulktracker.app.data.db.SectionEntity
import com.bulktracker.app.data.db.SettingsEntity
import com.bulktracker.app.data.db.TrackerDao
import com.bulktracker.app.domain.DayPlanner
import com.bulktracker.app.domain.IdealTimePlanner
import com.bulktracker.app.domain.ResolvedDay
import com.bulktracker.app.domain.VisibilityResolver
import com.bulktracker.app.util.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime

enum class ScopeOptionType {
    JUST_THIS_DAY,
    EVERY_WEEKDAY_FROM_NOW,
    FOREVER_FROM_TODAY,
    DATE_RANGE
}

data class ScopeSelection(
    val type: ScopeOptionType,
    val rangeStart: LocalDate? = null,
    val rangeEnd: LocalDate? = null
)

class TrackerRepository(
    val trackerDao: TrackerDao,
    val dayPlanner: DayPlanner = IdealTimePlanner(),
    val scheduleRepository: ScheduleRepository? = null
) {

    fun observeSettings(): Flow<SettingsEntity> {
        return trackerDao.observeSettings().map { it ?: SettingsEntity() }
    }

    suspend fun getSettings(): SettingsEntity {
        return withContext(Dispatchers.IO) {
            trackerDao.getSettings() ?: SettingsEntity()
        }
    }

    suspend fun upsertSettings(settings: SettingsEntity) {
        withContext(Dispatchers.IO) {
            trackerDao.upsertSettings(settings)
        }
    }

    suspend fun seedIfEmpty() {
        withContext(Dispatchers.IO) {
            val count = trackerDao.getSectionCount()
            if (count > 0) return@withContext

            val currentSettings = trackerDao.getSettings() ?: SettingsEntity()
            val startDay = DateUtils.logicalToday(LocalDateTime.now(), currentSettings.dayStartHour).toEpochDay()

            // Insert settings if missing
            if (trackerDao.getSettings() == null) {
                trackerDao.upsertSettings(SettingsEntity())
            }

            // Seed Sections
            val sections = listOf(
                SectionEntity(
                    id = 1,
                    name = "Morning Shake",
                    role = "MORNING_SHAKE",
                    size = "BIG",
                    timing = "WAKE_RELATIVE",
                    idealMold = 570,
                    durationMin = 15,
                    colorHex = "#3B82F6",
                    sortOrder = 0,
                    isMealSection = false,
                    alarmEnabled = true,
                    activeFromDay = startDay,
                    activeUntilDay = null,
                    weekdaysMask = 127
                ),
                SectionEntity(
                    id = 2,
                    name = "Between Classes",
                    role = "SNACK_1",
                    size = "SNACK",
                    timing = "FLEXIBLE",
                    idealMold = 720,
                    durationMin = 15,
                    colorHex = "#0D9488",
                    sortOrder = 1,
                    isMealSection = false,
                    alarmEnabled = true,
                    activeFromDay = startDay,
                    activeUntilDay = null,
                    weekdaysMask = 127
                ),
                SectionEntity(
                    id = 3,
                    name = "Lunch",
                    role = "LUNCH",
                    size = "BIG",
                    timing = "FIXED",
                    idealMold = 810,
                    durationMin = 30,
                    colorHex = "#EA580C",
                    sortOrder = 2,
                    isMealSection = true,
                    alarmEnabled = true,
                    activeFromDay = startDay,
                    activeUntilDay = null,
                    weekdaysMask = 127
                ),
                SectionEntity(
                    id = 4,
                    name = "4 PM Snack",
                    role = "SNACK_2",
                    size = "SNACK",
                    timing = "FLEXIBLE",
                    idealMold = 960,
                    durationMin = 20,
                    colorHex = "#8B5CF6",
                    sortOrder = 3,
                    isMealSection = false,
                    alarmEnabled = true,
                    activeFromDay = startDay,
                    activeUntilDay = null,
                    weekdaysMask = 127
                ),
                SectionEntity(
                    id = 5,
                    name = "Dinner",
                    role = "DINNER",
                    size = "BIG",
                    timing = "FIXED",
                    idealMold = 1230,
                    durationMin = 30,
                    colorHex = "#DC2626",
                    sortOrder = 4,
                    isMealSection = true,
                    alarmEnabled = true,
                    activeFromDay = startDay,
                    activeUntilDay = null,
                    weekdaysMask = 127
                ),
                SectionEntity(
                    id = 6,
                    name = "Night Shake (post-gym)",
                    role = "NIGHT_SHAKE",
                    size = "BIG",
                    timing = "AFTER_GYM",
                    idealMold = 1395,
                    durationMin = 15,
                    colorHex = "#6366F1",
                    sortOrder = 5,
                    isMealSection = false,
                    alarmEnabled = true,
                    activeFromDay = startDay,
                    activeUntilDay = null,
                    weekdaysMask = 127
                )
            )
            trackerDao.insertSections(sections)

            // Seed Items
            val items = listOf(
                // Morning Shake (section 1)
                FoodItemEntity(sectionId = 1, name = "Full cream milk", portionLabel = "500 ml", kcal = 400, proteinG = 18.0, isPriority = true, isOptional = false, sortOrder = 0, activeFromDay = startDay, activeUntilDay = null, weekdaysMask = 127),
                FoodItemEntity(sectionId = 1, name = "ON Serious Mass", portionLabel = "1/2 scoop (~83 g)", kcal = 310, proteinG = 12.5, isPriority = true, isOptional = false, sortOrder = 1, activeFromDay = startDay, activeUntilDay = null, weekdaysMask = 127),

                // Between Classes (section 2)
                FoodItemEntity(sectionId = 2, name = "Apple", portionLabel = "1 medium, cut up", kcal = 95, proteinG = 0.5, isPriority = false, isOptional = false, sortOrder = 0, activeFromDay = startDay, activeUntilDay = null, weekdaysMask = 127),
                FoodItemEntity(sectionId = 2, name = "Cashews", portionLabel = "6 nuts (~10 g)", kcal = 57, proteinG = 1.8, isPriority = false, isOptional = false, sortOrder = 1, activeFromDay = startDay, activeUntilDay = null, weekdaysMask = 127),
                FoodItemEntity(sectionId = 2, name = "Pistachios", portionLabel = "15 shelled (~10 g)", kcal = 57, proteinG = 2.0, isPriority = false, isOptional = false, sortOrder = 2, activeFromDay = startDay, activeUntilDay = null, weekdaysMask = 127),
                FoodItemEntity(sectionId = 2, name = "Dried figs, soaked", portionLabel = "2 figs", kcal = 60, proteinG = 0.7, isPriority = false, isOptional = false, sortOrder = 3, activeFromDay = startDay, activeUntilDay = null, weekdaysMask = 127),
                FoodItemEntity(sectionId = 2, name = "Roasted chana", portionLabel = "small handful (30 g)", kcal = 110, proteinG = 6.0, isPriority = false, isOptional = true, sortOrder = 4, activeFromDay = startDay, activeUntilDay = null, weekdaysMask = 127),

                // Lunch (section 3)
                FoodItemEntity(sectionId = 3, name = "Oil drizzled on food", portionLabel = "1 tbsp", kcal = 120, proteinG = 0.0, isPriority = true, isOptional = false, sortOrder = 0, activeFromDay = startDay, activeUntilDay = null, weekdaysMask = 127),
                FoodItemEntity(sectionId = 3, name = "Boiled egg", portionLabel = "1 egg", kcal = 70, proteinG = 6.0, isPriority = false, isOptional = false, sortOrder = 1, activeFromDay = startDay, activeUntilDay = null, weekdaysMask = 127),
                FoodItemEntity(sectionId = 3, name = "Lemon squeezed on food", portionLabel = "1/2 lime", kcal = 5, proteinG = 0.0, isPriority = false, isOptional = false, sortOrder = 2, activeFromDay = startDay, activeUntilDay = null, weekdaysMask = 127),

                // 4 PM Snack (section 4)
                FoodItemEntity(sectionId = 4, name = "Bananas (big)", portionLabel = "2 bananas", kcal = 180, proteinG = 2.5, isPriority = false, isOptional = false, sortOrder = 0, activeFromDay = startDay, activeUntilDay = null, weekdaysMask = 127),
                FoodItemEntity(sectionId = 4, name = "Yogurt (regular)", portionLabel = "100 g", kcal = 100, proteinG = 3.0, isPriority = false, isOptional = false, sortOrder = 1, activeFromDay = startDay, activeUntilDay = null, weekdaysMask = 127),
                FoodItemEntity(sectionId = 4, name = "Yogurt (high-protein)", portionLabel = "100 g", kcal = 100, proteinG = 7.0, isPriority = false, isOptional = true, sortOrder = 2, activeFromDay = startDay, activeUntilDay = null, weekdaysMask = 127),
                FoodItemEntity(sectionId = 4, name = "Peanut butter (100%)", portionLabel = "1 heaped tbsp (~16 g)", kcal = 100, proteinG = 4.5, isPriority = false, isOptional = false, sortOrder = 3, activeFromDay = startDay, activeUntilDay = null, weekdaysMask = 127),
                FoodItemEntity(sectionId = 4, name = "Honey", portionLabel = "1 tsp", kcal = 20, proteinG = 0.0, isPriority = false, isOptional = false, sortOrder = 4, activeFromDay = startDay, activeUntilDay = null, weekdaysMask = 127),
                FoodItemEntity(sectionId = 4, name = "Chia seeds", portionLabel = "2 tbsp", kcal = 120, proteinG = 4.0, isPriority = false, isOptional = false, sortOrder = 5, activeFromDay = startDay, activeUntilDay = null, weekdaysMask = 127),
                FoodItemEntity(sectionId = 4, name = "Pumpkin seeds", portionLabel = "2 tbsp (~20 g)", kcal = 110, proteinG = 6.0, isPriority = false, isOptional = false, sortOrder = 6, activeFromDay = startDay, activeUntilDay = null, weekdaysMask = 127),
                FoodItemEntity(sectionId = 4, name = "Creatine", portionLabel = "3 g in water", kcal = 0, proteinG = 0.0, isPriority = false, isOptional = false, sortOrder = 7, activeFromDay = startDay, activeUntilDay = null, weekdaysMask = 127),

                // Dinner (section 5)
                FoodItemEntity(sectionId = 5, name = "Oil drizzled on food", portionLabel = "1 tbsp", kcal = 120, proteinG = 0.0, isPriority = true, isOptional = false, sortOrder = 0, activeFromDay = startDay, activeUntilDay = null, weekdaysMask = 127),

                // Night Shake (section 6)
                FoodItemEntity(sectionId = 6, name = "Full cream milk", portionLabel = "500 ml", kcal = 400, proteinG = 18.0, isPriority = true, isOptional = false, sortOrder = 0, activeFromDay = startDay, activeUntilDay = null, weekdaysMask = 127),
                FoodItemEntity(sectionId = 6, name = "ON Gold Standard whey", portionLabel = "1 scoop", kcal = 120, proteinG = 24.0, isPriority = true, isOptional = false, sortOrder = 1, activeFromDay = startDay, activeUntilDay = null, weekdaysMask = 127),
                FoodItemEntity(sectionId = 6, name = "ON Serious Mass", portionLabel = "1 heaped tbsp (~15 g)", kcal = 55, proteinG = 2.0, isPriority = false, isOptional = false, sortOrder = 2, activeFromDay = startDay, activeUntilDay = null, weekdaysMask = 127)
            )
            trackerDao.insertFoodItems(items)
        }
    }

    fun observeDay(day: LocalDate): Flow<ResolvedDay> {
        val epochDay = day.toEpochDay()
        val flowA = combine(
            trackerDao.observeAllSections(),
            trackerDao.observeAllFoodItems(),
            trackerDao.observeOverridesForDay(epochDay)
        ) { sections, items, overrides ->
            Triple(sections, items, overrides)
        }

        val flowB = combine(
            trackerDao.observeLogEntriesForDay(epochDay),
            trackerDao.observeHabitForDay(epochDay),
            observeSettings()
        ) { logs, habit, settings ->
            Triple(logs, habit, settings)
        }

        val flowBusy = scheduleRepository?.busyBlocksFor(day) ?: flowOf(emptyList())

        return combine(flowA, flowB, flowBusy) { a, b, busy ->
            VisibilityResolver.resolve(
                day = day,
                sections = a.first,
                foodItems = a.second,
                overrides = a.third,
                logEntries = b.first,
                habit = b.second,
                settings = b.third,
                dayPlanner = dayPlanner,
                busy = busy
            )
        }
    }

    suspend fun getSettingsDirect(): SettingsEntity = withContext(Dispatchers.IO) {
        trackerDao.getSettings() ?: SettingsEntity()
    }

    suspend fun setAlarmsEnabled(enabled: Boolean) = withContext(Dispatchers.IO) {
        val current = trackerDao.getSettings() ?: SettingsEntity()
        trackerDao.upsertSettings(current.copy(alarmsEnabled = enabled))
    }

    suspend fun setSectionAlarmEnabled(sectionId: Long, enabled: Boolean) = withContext(Dispatchers.IO) {
        val section = trackerDao.getSectionById(sectionId) ?: return@withContext
        trackerDao.updateSection(section.copy(alarmEnabled = enabled))
    }

    suspend fun setSavedAlarmVolume(volume: Int?) = withContext(Dispatchers.IO) {
        val current = trackerDao.getSettings() ?: SettingsEntity()
        trackerDao.upsertSettings(current.copy(savedAlarmVolume = volume))
    }

    suspend fun markAllEatenForAlarm(sectionId: Long, epochDay: Long) = withContext(Dispatchers.IO) {
        val allItems = trackerDao.getItemsForSection(sectionId)
        val overrides = trackerDao.getOverridesForDay(epochDay)
        val itemOverrides = overrides.filter { it.targetType == "ITEM" }.associateBy { it.targetId }
        val visibleNonOptional = allItems.filter { item ->
            !item.isOptional &&
            VisibilityResolver.isItemVisible(item, epochDay) &&
            itemOverrides[item.id]?.action != "REMOVE"
        }
        val logEntries = trackerDao.getLogEntriesForDay(epochDay)
        val checkedItemIds = logEntries.mapNotNull { it.foodItemId }.toSet()

        for (item in visibleNonOptional) {
            if (!checkedItemIds.contains(item.id)) {
                val entry = LogEntryEntity(
                    epochDay = epochDay,
                    sectionId = sectionId,
                    foodItemId = item.id,
                    nameSnapshot = item.name,
                    portionSnapshot = item.portionLabel,
                    kcalSnapshot = item.kcal,
                    proteinSnapshot = item.proteinG,
                    multiplier = 1.0,
                    isCustom = false,
                    createdAtMillis = System.currentTimeMillis()
                )
                trackerDao.insertLogEntry(entry)
            }
        }
    }

    suspend fun skipSectionForDay(sectionId: Long, epochDay: Long) = withContext(Dispatchers.IO) {
        trackerDao.upsertDayOverride(
            DayOverrideEntity(
                epochDay = epochDay,
                targetType = "SECTION",
                targetId = sectionId,
                action = "SKIP"
            )
        )
    }

    suspend fun toggleItem(day: LocalDate, itemId: Long) {
        withContext(Dispatchers.IO) {
            trackerDao.toggleItem(day.toEpochDay(), itemId)
        }
    }

    suspend fun toggleBareMinimum(day: LocalDate) {
        withContext(Dispatchers.IO) {
            val epochDay = day.toEpochDay()
            val current = trackerDao.getHabitForDay(epochDay) ?: DailyHabitEntity(epochDay = epochDay)
            trackerDao.upsertHabit(current.copy(bareMinimum = !current.bareMinimum))
        }
    }

    suspend fun setGymPlan(day: LocalDate, gymPlan: String) {
        withContext(Dispatchers.IO) {
            val epochDay = day.toEpochDay()
            val current = trackerDao.getHabitForDay(epochDay) ?: DailyHabitEntity(epochDay = epochDay)
            trackerDao.upsertHabit(current.copy(gymPlan = gymPlan))
        }
    }

    suspend fun markAllEaten(day: LocalDate, sectionId: Long) {
        withContext(Dispatchers.IO) {
            val epochDay = day.toEpochDay()
            val allItems = trackerDao.getAllFoodItems().filter { it.sectionId == sectionId && !it.isOptional }
            val existing = trackerDao.getLogEntriesForDay(epochDay).filter { it.foodItemId != null }.mapNotNull { it.foodItemId }.toSet()
            val now = System.currentTimeMillis()
            for (item in allItems) {
                if (item.id !in existing) {
                    trackerDao.insertLogEntry(
                        LogEntryEntity(
                            epochDay = epochDay,
                            sectionId = item.sectionId,
                            foodItemId = item.id,
                            nameSnapshot = item.name,
                            portionSnapshot = item.portionLabel,
                            kcalSnapshot = item.kcal,
                            proteinSnapshot = item.proteinG,
                            multiplier = 1.0,
                            isCustom = false,
                            createdAtMillis = now
                        )
                    )
                }
            }
        }
    }

    suspend fun undoLogEntry(logEntryId: Long) {
        withContext(Dispatchers.IO) {
            trackerDao.deleteLogEntryById(logEntryId)
        }
    }

    suspend fun addCustomLogEntry(
        day: LocalDate,
        sectionId: Long,
        name: String,
        portion: String,
        kcal: Int?,
        protein: Double?
    ): Long = withContext(Dispatchers.IO) {
        trackerDao.insertLogEntry(
            LogEntryEntity(
                epochDay = day.toEpochDay(),
                sectionId = sectionId,
                foodItemId = null,
                nameSnapshot = name,
                portionSnapshot = portion,
                kcalSnapshot = kcal,
                proteinSnapshot = protein,
                multiplier = 1.0,
                isCustom = true,
                createdAtMillis = System.currentTimeMillis()
            )
        )
    }

    suspend fun updateCustomLogEntry(
        logEntryId: Long,
        kcal: Int?,
        protein: Double?,
        multiplier: Double
    ) = withContext(Dispatchers.IO) {
        val existing = trackerDao.getLogEntryById(logEntryId) ?: return@withContext
        trackerDao.updateLogEntry(
            existing.copy(
                kcalSnapshot = kcal,
                proteinSnapshot = protein,
                multiplier = multiplier
            )
        )
    }

    suspend fun deleteCustomLogEntry(logEntryId: Long): LogEntryEntity? = withContext(Dispatchers.IO) {
        val existing = trackerDao.getLogEntryById(logEntryId)
        if (existing != null) {
            trackerDao.deleteLogEntryById(logEntryId)
        }
        existing
    }

    suspend fun restoreLogEntry(entry: LogEntryEntity) = withContext(Dispatchers.IO) {
        trackerDao.insertLogEntry(entry)
    }

    suspend fun updateItemMultiplierAndNutrition(
        day: LocalDate,
        itemId: Long,
        multiplier: Double,
        customKcal: Int?,
        customProtein: Double?
    ) = withContext(Dispatchers.IO) {
        val epochDay = day.toEpochDay()
        val existing = trackerDao.findLogEntry(epochDay, itemId)
        if (existing != null) {
            trackerDao.updateLogEntry(
                existing.copy(
                    multiplier = multiplier,
                    kcalSnapshot = customKcal,
                    proteinSnapshot = customProtein
                )
            )
        } else {
            val item = trackerDao.getFoodItemById(itemId) ?: return@withContext
            trackerDao.insertLogEntry(
                LogEntryEntity(
                    epochDay = epochDay,
                    sectionId = item.sectionId,
                    foodItemId = item.id,
                    nameSnapshot = item.name,
                    portionSnapshot = item.portionLabel,
                    kcalSnapshot = customKcal ?: item.kcal,
                    proteinSnapshot = customProtein ?: item.proteinG,
                    multiplier = multiplier,
                    isCustom = false,
                    createdAtMillis = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun pinSectionTime(day: LocalDate, sectionId: Long, pinnedMold: Int) = withContext(Dispatchers.IO) {
        trackerDao.upsertDayOverride(
            DayOverrideEntity(
                epochDay = day.toEpochDay(),
                targetType = "SECTION",
                targetId = sectionId,
                action = "PIN_TIME",
                pinnedMold = pinnedMold
            )
        )
    }

    suspend fun skipSection(day: LocalDate, sectionId: Long) = withContext(Dispatchers.IO) {
        trackerDao.upsertDayOverride(
            DayOverrideEntity(
                epochDay = day.toEpochDay(),
                targetType = "SECTION",
                targetId = sectionId,
                action = "SKIP"
            )
        )
    }

    suspend fun restoreSectionOverride(day: LocalDate, sectionId: Long) = withContext(Dispatchers.IO) {
        trackerDao.deleteDayOverride(day.toEpochDay(), "SECTION", sectionId)
    }

    suspend fun removeItemWithScope(day: LocalDate, itemId: Long, scope: ScopeSelection) = withContext(Dispatchers.IO) {
        val epochDay = day.toEpochDay()
        val item = trackerDao.getFoodItemById(itemId) ?: return@withContext
        val weekdayBit = 1 shl (day.dayOfWeek.value - 1)

        when (scope.type) {
            ScopeOptionType.JUST_THIS_DAY -> {
                trackerDao.upsertDayOverride(
                    DayOverrideEntity(
                        epochDay = epochDay,
                        targetType = "ITEM",
                        targetId = itemId,
                        action = "REMOVE"
                    )
                )
                trackerDao.deleteLogsForItemOnDay(epochDay, itemId)
            }
            ScopeOptionType.EVERY_WEEKDAY_FROM_NOW -> {
                val newMask = item.weekdaysMask and weekdayBit.inv()
                if (newMask == 0) {
                    trackerDao.updateFoodItem(item.copy(activeUntilDay = epochDay - 1))
                } else if (item.activeFromDay < epochDay) {
                    trackerDao.updateFoodItem(item.copy(activeUntilDay = epochDay - 1))
                    val newItem = item.copy(
                        id = 0,
                        activeFromDay = epochDay,
                        weekdaysMask = newMask
                    )
                    val newId = trackerDao.insertFoodItem(newItem)
                    trackerDao.updateOverridesItemId(itemId, newId, epochDay)
                } else {
                    trackerDao.updateFoodItem(item.copy(weekdaysMask = newMask))
                }
            }
            ScopeOptionType.FOREVER_FROM_TODAY -> {
                trackerDao.updateFoodItem(item.copy(activeUntilDay = epochDay - 1))
            }
            ScopeOptionType.DATE_RANGE -> {
                val start = scope.rangeStart ?: day
                val end = scope.rangeEnd ?: day
                val sEpoch = start.toEpochDay()
                val eEpoch = end.toEpochDay().coerceAtMost(sEpoch + 365)
                for (d in sEpoch..eEpoch) {
                    trackerDao.upsertDayOverride(
                        DayOverrideEntity(
                            epochDay = d,
                            targetType = "ITEM",
                            targetId = itemId,
                            action = "REMOVE"
                        )
                    )
                }
            }
        }
    }

    suspend fun removeSectionWithScope(day: LocalDate, sectionId: Long, scope: ScopeSelection) = withContext(Dispatchers.IO) {
        val epochDay = day.toEpochDay()
        val section = trackerDao.getSectionById(sectionId) ?: return@withContext
        val weekdayBit = 1 shl (day.dayOfWeek.value - 1)

        when (scope.type) {
            ScopeOptionType.JUST_THIS_DAY -> {
                trackerDao.upsertDayOverride(
                    DayOverrideEntity(
                        epochDay = epochDay,
                        targetType = "SECTION",
                        targetId = sectionId,
                        action = "REMOVE"
                    )
                )
                trackerDao.deleteLogsForSectionOnDay(epochDay, sectionId)
            }
            ScopeOptionType.EVERY_WEEKDAY_FROM_NOW -> {
                val newMask = section.weekdaysMask and weekdayBit.inv()
                if (newMask == 0) {
                    trackerDao.updateSection(section.copy(activeUntilDay = epochDay - 1))
                } else if (section.activeFromDay < epochDay) {
                    trackerDao.updateSection(section.copy(activeUntilDay = epochDay - 1))
                    val newSec = section.copy(
                        id = 0,
                        activeFromDay = epochDay,
                        weekdaysMask = newMask
                    )
                    val newSecId = trackerDao.insertSection(newSec)

                    // Copy only active items
                    val items = trackerDao.getItemsForSection(sectionId)
                    for (it in items) {
                        if (it.activeUntilDay == null || it.activeUntilDay >= epochDay) {
                            val newIt = it.copy(
                                id = 0,
                                sectionId = newSecId,
                                activeFromDay = maxOf(it.activeFromDay, epochDay)
                            )
                            val newItId = trackerDao.insertFoodItem(newIt)
                            trackerDao.updateOverridesItemId(it.id, newItId, epochDay)
                        }
                    }
                    trackerDao.updateOverridesSectionId(sectionId, newSecId, epochDay)
                } else {
                    trackerDao.updateSection(section.copy(weekdaysMask = newMask))
                }
            }
            ScopeOptionType.FOREVER_FROM_TODAY -> {
                trackerDao.updateSection(section.copy(activeUntilDay = epochDay - 1))
            }
            ScopeOptionType.DATE_RANGE -> {
                val start = scope.rangeStart ?: day
                val end = scope.rangeEnd ?: day
                val sEpoch = start.toEpochDay()
                val eEpoch = end.toEpochDay().coerceAtMost(sEpoch + 365)
                for (d in sEpoch..eEpoch) {
                    trackerDao.upsertDayOverride(
                        DayOverrideEntity(
                            epochDay = d,
                            targetType = "SECTION",
                            targetId = sectionId,
                            action = "REMOVE"
                        )
                    )
                }
            }
        }
    }

    suspend fun addItemWithScope(
        day: LocalDate,
        sectionId: Long,
        name: String,
        portion: String,
        kcal: Int?,
        protein: Double?,
        scope: ScopeSelection
    ): Long = withContext(Dispatchers.IO) {
        val epochDay = day.toEpochDay()
        val weekdayBit = 1 shl (day.dayOfWeek.value - 1)

        val (activeFrom, activeUntil, mask) = when (scope.type) {
            ScopeOptionType.JUST_THIS_DAY -> Triple(epochDay, epochDay, 127)
            ScopeOptionType.EVERY_WEEKDAY_FROM_NOW -> Triple(epochDay, null, weekdayBit)
            ScopeOptionType.FOREVER_FROM_TODAY -> Triple(epochDay, null, 127)
            ScopeOptionType.DATE_RANGE -> {
                val s = scope.rangeStart?.toEpochDay() ?: epochDay
                val e = scope.rangeEnd?.toEpochDay() ?: epochDay
                Triple(s, e, 127)
            }
        }

        val existingItems = trackerDao.getItemsForSection(sectionId)
        val nextSortOrder = (existingItems.maxOfOrNull { it.sortOrder } ?: -1) + 1

        val newItem = FoodItemEntity(
            sectionId = sectionId,
            name = name,
            portionLabel = portion,
            kcal = kcal,
            proteinG = protein,
            isPriority = false,
            isOptional = false,
            sortOrder = nextSortOrder,
            activeFromDay = activeFrom,
            activeUntilDay = activeUntil,
            weekdaysMask = mask
        )
        val newId = trackerDao.insertFoodItem(newItem)

        trackerDao.insertLogEntry(
            LogEntryEntity(
                epochDay = epochDay,
                sectionId = sectionId,
                foodItemId = newId,
                nameSnapshot = name,
                portionSnapshot = portion,
                kcalSnapshot = kcal,
                proteinSnapshot = protein,
                multiplier = 1.0,
                isCustom = false,
                createdAtMillis = System.currentTimeMillis()
            )
        )

        newId
    }

    suspend fun addSectionWithScope(
        day: LocalDate,
        name: String,
        idealMold: Int,
        size: String,
        colorHex: String,
        scope: ScopeSelection
    ): Long = withContext(Dispatchers.IO) {
        val epochDay = day.toEpochDay()
        val weekdayBit = 1 shl (day.dayOfWeek.value - 1)

        val (activeFrom, activeUntil, mask) = when (scope.type) {
            ScopeOptionType.JUST_THIS_DAY -> Triple(epochDay, epochDay, 127)
            ScopeOptionType.EVERY_WEEKDAY_FROM_NOW -> Triple(epochDay, null, weekdayBit)
            ScopeOptionType.FOREVER_FROM_TODAY -> Triple(epochDay, null, 127)
            ScopeOptionType.DATE_RANGE -> {
                val s = scope.rangeStart?.toEpochDay() ?: epochDay
                val e = scope.rangeEnd?.toEpochDay() ?: epochDay
                Triple(s, e, 127)
            }
        }

        val allSections = trackerDao.getAllSections()
        val nextSortOrder = (allSections.maxOfOrNull { it.sortOrder } ?: -1) + 1
        val dur = if (size == "SNACK") 20 else 30

        val newSection = SectionEntity(
            name = name,
            role = "CUSTOM",
            size = size,
            timing = "FIXED",
            idealMold = idealMold,
            durationMin = dur,
            colorHex = colorHex,
            sortOrder = nextSortOrder,
            isMealSection = false,
            alarmEnabled = true,
            activeFromDay = activeFrom,
            activeUntilDay = activeUntil,
            weekdaysMask = mask
        )
        trackerDao.insertSection(newSection)
    }

    suspend fun updateFoodItem(
        itemId: Long,
        name: String,
        portionLabel: String,
        kcal: Int?,
        proteinG: Double?,
        isPriority: Boolean,
        isOptional: Boolean
    ) = withContext(Dispatchers.IO) {
        val existing = trackerDao.getFoodItemById(itemId) ?: return@withContext
        val updated = existing.copy(
            name = name,
            portionLabel = portionLabel,
            kcal = kcal,
            proteinG = proteinG,
            isPriority = isPriority,
            isOptional = isOptional
        )
        trackerDao.updateFoodItem(updated)
    }

    suspend fun updateSection(
        sectionId: Long,
        name: String,
        idealMold: Int,
        size: String,
        colorHex: String
    ) = withContext(Dispatchers.IO) {
        val existing = trackerDao.getSectionById(sectionId) ?: return@withContext
        val updated = existing.copy(
            name = name,
            idealMold = idealMold,
            size = size,
            colorHex = colorHex
        )
        trackerDao.updateSection(updated)
    }
}
