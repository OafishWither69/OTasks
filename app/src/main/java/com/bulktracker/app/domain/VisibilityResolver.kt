package com.bulktracker.app.domain

import com.bulktracker.app.data.db.DailyHabitEntity
import com.bulktracker.app.data.db.DayOverrideEntity
import com.bulktracker.app.data.db.FoodItemEntity
import com.bulktracker.app.data.db.LogEntryEntity
import com.bulktracker.app.data.db.SectionEntity
import com.bulktracker.app.data.db.SettingsEntity
import java.time.LocalDate

data class ResolvedItem(
    val item: FoodItemEntity,
    val isChecked: Boolean,
    val isMergedFromOtherSection: Boolean = false,
    val originalSectionName: String? = null
)

data class ResolvedSection(
    val section: SectionEntity,
    val isSkipped: Boolean,
    val pinnedMold: Int?,
    val items: List<ResolvedItem>,
    val customEntries: List<LogEntryEntity>,
    val effectiveTimeMold: Int,
    val slotStatus: String = "NORMAL"
) {
    val totalKcal: Int
        get() {
            var sum = 0
            for (ri in items) {
                sum += ri.item.kcal ?: 0
            }
            for (ce in customEntries) {
                sum += ce.kcalSnapshot ?: 0
            }
            return sum
        }

    val eatenCount: Int
        get() = items.count { it.isChecked }

    val totalNonOptionalCount: Int
        get() = items.count { !it.item.isOptional }

    val isAllDone: Boolean
        get() = totalNonOptionalCount > 0 && items.filter { !it.item.isOptional }.all { it.isChecked }
}

data class ResolvedDay(
    val day: LocalDate,
    val habit: DailyHabitEntity?,
    val settings: SettingsEntity,
    val sections: List<ResolvedSection>,
    val allVisibleItems: List<FoodItemEntity>,
    val logEntries: List<LogEntryEntity>,
    val totals: DayTotals,
    val dayPlan: DayPlan = DayPlan(wakeMold = settings.noClassWakeMold)
)

object VisibilityResolver {
    fun inRange(
        activeFromDay: Long,
        activeUntilDay: Long?,
        weekdaysMask: Int,
        epochDay: Long,
        weekdayValue: Int // 1 = Monday ... 7 = Sunday
    ): Boolean {
        if (epochDay < activeFromDay) return false
        if (activeUntilDay != null && epochDay > activeUntilDay) return false
        val bit = 1 shl (weekdayValue - 1)
        return (weekdaysMask and bit) != 0
    }

    fun isItemVisible(item: FoodItemEntity, epochDay: Long): Boolean {
        val date = LocalDate.ofEpochDay(epochDay)
        return inRange(item.activeFromDay, item.activeUntilDay, item.weekdaysMask, epochDay, date.dayOfWeek.value)
    }

    fun isSectionVisible(section: SectionEntity, epochDay: Long): Boolean {
        val date = LocalDate.ofEpochDay(epochDay)
        return inRange(section.activeFromDay, section.activeUntilDay, section.weekdaysMask, epochDay, date.dayOfWeek.value)
    }

    fun resolve(
        day: LocalDate,
        sections: List<SectionEntity>,
        foodItems: List<FoodItemEntity>,
        overrides: List<DayOverrideEntity>,
        logEntries: List<LogEntryEntity>,
        habit: DailyHabitEntity?,
        settings: SettingsEntity,
        dayPlanner: DayPlanner? = null,
        busy: List<BusyBlock> = emptyList()
    ): ResolvedDay {
        val epochDay = day.toEpochDay()
        val weekdayValue = day.dayOfWeek.value
        val isBareMinimum = habit?.bareMinimum == true

        val sectionOverrides = overrides.filter { it.targetType == "SECTION" && it.epochDay == epochDay }.associateBy { it.targetId }
        val itemOverrides = overrides.filter { it.targetType == "ITEM" && it.epochDay == epochDay }.associateBy { it.targetId }
        val checkedItemIds = logEntries.mapNotNull { it.foodItemId }.toSet()
        val sectionById = sections.associateBy { it.id }

        // Determine visibility & status of each section
        // Actions: "ADD" | "REMOVE" | "SKIP" | "MERGE_INTO" | "PIN_TIME"
        class SectionStatus(
            val isVisible: Boolean,
            val isSkipped: Boolean,
            val isMerged: Boolean,
            val mergedIntoId: Long?,
            val pinnedMold: Int?
        )

        val sectionStatusMap = mutableMapOf<Long, SectionStatus>()
        for (section in sections) {
            val ov = sectionOverrides[section.id]
            if (ov != null) {
                when (ov.action) {
                    "REMOVE" -> sectionStatusMap[section.id] = SectionStatus(
                        isVisible = false,
                        isSkipped = false,
                        isMerged = false,
                        mergedIntoId = null,
                        pinnedMold = null
                    )
                    "MERGE_INTO" -> sectionStatusMap[section.id] = SectionStatus(
                        isVisible = false,
                        isSkipped = false,
                        isMerged = true,
                        mergedIntoId = ov.mergeIntoSectionId,
                        pinnedMold = null
                    )
                    "ADD" -> sectionStatusMap[section.id] = SectionStatus(
                        isVisible = true,
                        isSkipped = false,
                        isMerged = false,
                        mergedIntoId = null,
                        pinnedMold = null
                    )
                    "SKIP" -> sectionStatusMap[section.id] = SectionStatus(
                        isVisible = true,
                        isSkipped = true,
                        isMerged = false,
                        mergedIntoId = null,
                        pinnedMold = null
                    )
                    "PIN_TIME" -> sectionStatusMap[section.id] = SectionStatus(
                        isVisible = true,
                        isSkipped = false,
                        isMerged = false,
                        mergedIntoId = null,
                        pinnedMold = ov.pinnedMold
                    )
                    else -> sectionStatusMap[section.id] = SectionStatus(
                        isVisible = true,
                        isSkipped = false,
                        isMerged = false,
                        mergedIntoId = null,
                        pinnedMold = null
                    )
                }
            } else {
                val visible = inRange(
                    section.activeFromDay,
                    section.activeUntilDay,
                    section.weekdaysMask,
                    epochDay,
                    weekdayValue
                )
                sectionStatusMap[section.id] = SectionStatus(
                    isVisible = visible,
                    isSkipped = false,
                    isMerged = false,
                    mergedIntoId = null,
                    pinnedMold = null
                )
            }
        }

        // Determine items for each section, including merges
        val resolvedItemsBySection = mutableMapOf<Long, MutableList<ResolvedItem>>()
        val allVisibleItemsList = mutableListOf<FoodItemEntity>()

        for (item in foodItems.sortedBy { it.sortOrder }) {
            val itemOv = itemOverrides[item.id]
            val isItemOverriddenRemoved = itemOv?.action == "REMOVE"
            val isItemOverriddenAdded = itemOv?.action == "ADD"

            val isItemBaseVisible = inRange(
                item.activeFromDay,
                item.activeUntilDay,
                item.weekdaysMask,
                epochDay,
                weekdayValue
            )

            val isVisible = (isItemBaseVisible || isItemOverriddenAdded) && !isItemOverriddenRemoved
            if (!isVisible) continue

            val parentSectionStatus = sectionStatusMap[item.sectionId]
            if (parentSectionStatus == null || !parentSectionStatus.isVisible && !parentSectionStatus.isMerged) {
                continue
            }

            // Bare-minimum rule: "only food items with isPriority = true are visible; optional items and non-priority items are hidden"
            if (isBareMinimum && !item.isPriority) {
                continue
            }

            val isChecked = checkedItemIds.contains(item.id)

            if (parentSectionStatus.isMerged && parentSectionStatus.mergedIntoId != null) {
                val targetSecId = parentSectionStatus.mergedIntoId
                val targetSecStatus = sectionStatusMap[targetSecId]
                if (targetSecStatus != null && targetSecStatus.isVisible) {
                    val list = resolvedItemsBySection.getOrPut(targetSecId) { mutableListOf() }
                    val origSecName = sectionById[item.sectionId]?.name
                    list.add(
                        ResolvedItem(
                            item = item,
                            isChecked = isChecked,
                            isMergedFromOtherSection = true,
                            originalSectionName = origSecName
                        )
                    )
                    allVisibleItemsList.add(item)
                }
            } else if (parentSectionStatus.isVisible) {
                val list = resolvedItemsBySection.getOrPut(item.sectionId) { mutableListOf() }
                list.add(
                    ResolvedItem(
                        item = item,
                        isChecked = isChecked,
                        isMergedFromOtherSection = false,
                        originalSectionName = null
                    )
                )
                allVisibleItemsList.add(item)
            }
        }

        val customEntriesBySection = logEntries.filter { it.isCustom }.groupBy { it.sectionId }

        // Filter sections that are actually visible on this day
        val visibleSectionsForDay = mutableListOf<SectionEntity>()
        for (section in sections.sortedBy { it.sortOrder }) {
            val status = sectionStatusMap[section.id] ?: continue
            if (!status.isVisible) continue

            val items = resolvedItemsBySection[section.id] ?: emptyList()
            if (isBareMinimum) {
                val hasPriorityItem = items.any { it.item.isPriority }
                if (!hasPriorityItem) continue
            }
            visibleSectionsForDay.add(section)
        }

        // Compute DayPlan using the provided DayPlanner
        val planner = dayPlanner ?: IdealTimePlanner()
        val dayPlan = planner.plan(
            day = day,
            sections = visibleSectionsForDay,
            overrides = overrides.filter { it.epochDay == epochDay },
            busy = busy,
            habit = habit,
            settings = settings
        )

        val slotBySectionId = dayPlan.slots.associateBy { it.sectionId }

        // Construct final resolved sections
        val resolvedSections = mutableListOf<ResolvedSection>()
        for (section in visibleSectionsForDay) {
            val status = sectionStatusMap[section.id] ?: continue
            val items = resolvedItemsBySection[section.id] ?: emptyList()
            val slot = slotBySectionId[section.id]
            val effectiveTime = slot?.mold ?: (status.pinnedMold ?: section.idealMold)
            val slotStatus = slot?.status ?: (if (status.isSkipped) "SKIPPED" else if (status.pinnedMold != null) "PINNED" else "NORMAL")

            resolvedSections.add(
                ResolvedSection(
                    section = section,
                    isSkipped = status.isSkipped,
                    pinnedMold = status.pinnedMold,
                    items = items,
                    customEntries = customEntriesBySection[section.id] ?: emptyList(),
                    effectiveTimeMold = effectiveTime,
                    slotStatus = slotStatus
                )
            )
        }

        resolvedSections.sortBy { it.effectiveTimeMold }

        val totals = TotalsCalculator.calculate(
            visibleItems = allVisibleItemsList,
            logEntries = logEntries,
            habit = habit,
            settings = settings
        )

        return ResolvedDay(
            day = day,
            habit = habit,
            settings = settings,
            sections = resolvedSections,
            allVisibleItems = allVisibleItemsList,
            logEntries = logEntries,
            totals = totals,
            dayPlan = dayPlan
        )
    }
}
