package com.bulktracker.app.domain

import com.bulktracker.app.data.db.DailyHabitEntity
import com.bulktracker.app.data.db.FoodItemEntity
import com.bulktracker.app.data.db.LogEntryEntity
import com.bulktracker.app.data.db.SettingsEntity
import kotlin.math.roundToInt

data class DayTotals(
    val caloriesEaten: Int,
    val caloriesTarget: Int,
    val proteinEatenG: Double,
    val proteinTargetG: Double,
    val unknownCount: Int,
    val completionPercent: Int,
    val isBareMinimum: Boolean,
    val priorityItemsLeftCount: Int
)

object TotalsCalculator {
    fun calculate(
        visibleItems: List<FoodItemEntity>,
        logEntries: List<LogEntryEntity>,
        habit: DailyHabitEntity?,
        settings: SettingsEntity
    ): DayTotals {
        val isBareMin = habit?.bareMinimum == true

        var totalKcal = 0
        var totalProtein = 0.0
        var unknownCount = 0

        for (entry in logEntries) {
            val effKcal = entry.kcalSnapshot?.let { (it * entry.multiplier).roundToInt() }
            val effProtein = entry.proteinSnapshot?.let { it * entry.multiplier }

            if (effKcal != null) {
                totalKcal += effKcal
            } else {
                unknownCount++
            }

            if (effProtein != null) {
                totalProtein += effProtein
            }
        }

        // Targets: If bare minimum, target is the sum of kcal and protein of all visible priority items.
        // Otherwise default from settings.
        val targetKcal: Int
        val targetProtein: Double
        if (isBareMin) {
            var sumKcal = 0
            var sumProtein = 0.0
            for (item in visibleItems.filter { it.isPriority }) {
                sumKcal += item.kcal ?: 0
                sumProtein += item.proteinG ?: 0.0
            }
            targetKcal = sumKcal
            targetProtein = sumProtein
        } else {
            targetKcal = settings.kcalTarget
            targetProtein = settings.proteinTarget
        }

        // Completion % for day D:
        // checked visible non-optional items / visible non-optional items, rounded to a whole number.
        // If bareMinimum == true, count only priority items (and non-optional).
        val checkedItemIds = logEntries.mapNotNull { it.foodItemId }.toSet()

        val qualifyingItems = if (isBareMin) {
            visibleItems.filter { it.isPriority && !it.isOptional }
        } else {
            visibleItems.filter { !it.isOptional }
        }

        val checkedCount = qualifyingItems.count { it.id in checkedItemIds }
        val completionPercent = if (qualifyingItems.isEmpty()) {
            0
        } else {
            ((checkedCount.toDouble() / qualifyingItems.size.toDouble()) * 100.0).roundToInt()
        }

        // Priority items left: visible priority items that are NOT checked
        val priorityItemsLeft = visibleItems.count { it.isPriority && it.id !in checkedItemIds }

        return DayTotals(
            caloriesEaten = totalKcal,
            caloriesTarget = targetKcal,
            proteinEatenG = totalProtein,
            proteinTargetG = targetProtein,
            unknownCount = unknownCount,
            completionPercent = completionPercent,
            isBareMinimum = isBareMin,
            priorityItemsLeftCount = priorityItemsLeft
        )
    }
}
