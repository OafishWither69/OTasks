package com.bulktracker.app

import com.bulktracker.app.data.db.DailyHabitEntity
import com.bulktracker.app.data.db.DayOverrideEntity
import com.bulktracker.app.data.db.FoodItemEntity
import com.bulktracker.app.data.db.LogEntryEntity
import com.bulktracker.app.data.db.SectionEntity
import com.bulktracker.app.data.db.SettingsEntity
import com.bulktracker.app.domain.TotalsCalculator
import com.bulktracker.app.domain.VisibilityResolver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class Phase2Test {

    @Test
    fun testCustomEntryNormalPlate() {
        val section = SectionEntity(
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
            activeFromDay = 0
        )

        val log = LogEntryEntity(
            id = 10,
            epochDay = 100,
            sectionId = 5,
            foodItemId = null,
            nameSnapshot = "Paneer + 2 roti",
            portionSnapshot = "Normal plate",
            kcalSnapshot = 450,
            proteinSnapshot = 12.0,
            multiplier = 1.0,
            isCustom = true,
            createdAtMillis = 1000
        )

        val totals = TotalsCalculator.calculate(
            visibleItems = emptyList(),
            logEntries = listOf(log),
            habit = null,
            settings = SettingsEntity()
        )

        assertEquals(450, totals.caloriesEaten)
        assertEquals(12.0, totals.proteinEatenG, 0.01)
        assertEquals(0, totals.unknownCount)
    }

    @Test
    fun testCustomEntryUnknownCalories() {
        val log = LogEntryEntity(
            id = 11,
            epochDay = 100,
            sectionId = 2,
            foodItemId = null,
            nameSnapshot = "Samosa",
            portionSnapshot = "",
            kcalSnapshot = null,
            proteinSnapshot = null,
            multiplier = 1.0,
            isCustom = true,
            createdAtMillis = 1000
        )

        val totals = TotalsCalculator.calculate(
            visibleItems = emptyList(),
            logEntries = listOf(log),
            habit = null,
            settings = SettingsEntity()
        )

        assertEquals(0, totals.caloriesEaten)
        assertEquals(1, totals.unknownCount)
    }

    @Test
    fun testMultiplier() {
        val banana = FoodItemEntity(
            id = 10,
            sectionId = 4,
            name = "Bananas (big)",
            portionLabel = "2 bananas",
            kcal = 180,
            proteinG = 2.5,
            isPriority = false,
            sortOrder = 0,
            activeFromDay = 0
        )

        val log = LogEntryEntity(
            id = 12,
            epochDay = 100,
            sectionId = 4,
            foodItemId = 10,
            nameSnapshot = "Bananas (big)",
            portionSnapshot = "2 bananas",
            kcalSnapshot = 180,
            proteinSnapshot = 2.5,
            multiplier = 1.5,
            isCustom = false,
            createdAtMillis = 1000
        )

        val totals = TotalsCalculator.calculate(
            visibleItems = listOf(banana),
            logEntries = listOf(log),
            habit = null,
            settings = SettingsEntity()
        )

        // 180 * 1.5 = 270
        assertEquals(270, totals.caloriesEaten)
        // 2.5 * 1.5 = 3.75
        assertEquals(3.75, totals.proteinEatenG, 0.01)
    }

    @Test
    fun testScopeRemoveJustThisDay() {
        val tuesday = LocalDate.of(2026, 10, 6)
        val wednesday = tuesday.plusDays(1)

        val honey = FoodItemEntity(
            id = 20,
            sectionId = 4,
            name = "Honey",
            portionLabel = "1 tsp",
            kcal = 20,
            proteinG = 0.0,
            sortOrder = 0,
            activeFromDay = 0
        )

        val section = SectionEntity(
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
            activeFromDay = 0
        )

        val override = DayOverrideEntity(
            epochDay = tuesday.toEpochDay(),
            targetType = "ITEM",
            targetId = 20,
            action = "REMOVE"
        )

        // Tuesday: Honey is removed by override
        val resolvedTue = VisibilityResolver.resolve(
            day = tuesday,
            sections = listOf(section),
            foodItems = listOf(honey),
            overrides = listOf(override),
            logEntries = emptyList(),
            habit = null,
            settings = SettingsEntity()
        )
        assertTrue(resolvedTue.sections[0].items.isEmpty())

        // Wednesday: Honey is back!
        val resolvedWed = VisibilityResolver.resolve(
            day = wednesday,
            sections = listOf(section),
            foodItems = listOf(honey),
            overrides = listOf(override), // override was only for Tuesday
            logEntries = emptyList(),
            habit = null,
            settings = SettingsEntity()
        )
        assertEquals(1, resolvedWed.sections[0].items.size)
        assertEquals("Honey", resolvedWed.sections[0].items[0].item.name)
    }

    @Test
    fun testScopeAddMealSundaysOnly() {
        val sunday = LocalDate.of(2026, 10, 11) // Sunday
        val monday = sunday.plusDays(1) // Monday

        // Sunday weekday value is 7 -> bit 6 = 1 shl 6 = 64
        val sundaySection = SectionEntity(
            id = 100,
            name = "Sunday brunch",
            role = "CUSTOM",
            size = "BIG",
            timing = "FIXED",
            idealMold = 660, // 11:00 AM
            durationMin = 30,
            colorHex = "#3B82F6",
            sortOrder = 10,
            isMealSection = false,
            activeFromDay = sunday.toEpochDay(),
            activeUntilDay = null,
            weekdaysMask = 64 // Sundays only
        )

        val resolvedSunday = VisibilityResolver.resolve(
            day = sunday,
            sections = listOf(sundaySection),
            foodItems = emptyList(),
            overrides = emptyList(),
            logEntries = emptyList(),
            habit = null,
            settings = SettingsEntity()
        )
        assertEquals(1, resolvedSunday.sections.size)
        assertEquals("Sunday brunch", resolvedSunday.sections[0].section.name)

        val resolvedMonday = VisibilityResolver.resolve(
            day = monday,
            sections = listOf(sundaySection),
            foodItems = emptyList(),
            overrides = emptyList(),
            logEntries = emptyList(),
            habit = null,
            settings = SettingsEntity()
        )
        assertTrue(resolvedMonday.sections.isEmpty())
    }

    @Test
    fun testBareMinimumMode() {
        val today = LocalDate.of(2026, 10, 5)

        val sec1 = SectionEntity(id = 1, name = "Sec1", role = "MORNING_SHAKE", size = "BIG", timing = "WAKE_RELATIVE", idealMold = 570, durationMin = 15, colorHex = "#3B82F6", sortOrder = 0, isMealSection = false, activeFromDay = 0)
        val sec2 = SectionEntity(id = 2, name = "Sec2", role = "SNACK_1", size = "SNACK", timing = "FLEXIBLE", idealMold = 720, durationMin = 15, colorHex = "#0D9488", sortOrder = 1, isMealSection = false, activeFromDay = 0)

        val item1 = FoodItemEntity(id = 1, sectionId = 1, name = "Priority Milk", portionLabel = "500ml", kcal = 400, proteinG = 18.0, isPriority = true, activeFromDay = 0, sortOrder = 0)
        val item2 = FoodItemEntity(id = 2, sectionId = 2, name = "Normal Apple", portionLabel = "1", kcal = 95, proteinG = 0.5, isPriority = false, activeFromDay = 0, sortOrder = 0)

        val habitBareMin = DailyHabitEntity(epochDay = today.toEpochDay(), bareMinimum = true)

        val resolved = VisibilityResolver.resolve(
            day = today,
            sections = listOf(sec1, sec2),
            foodItems = listOf(item1, item2),
            overrides = emptyList(),
            logEntries = emptyList(),
            habit = habitBareMin,
            settings = SettingsEntity()
        )

        // sec2 has no priority items, so it is hidden in bare minimum mode
        assertEquals(1, resolved.sections.size)
        assertEquals("Sec1", resolved.sections[0].section.name)
        assertEquals(1, resolved.sections[0].items.size)
        assertEquals("Priority Milk", resolved.sections[0].items[0].item.name)

        // Calorie target becomes the sum of visible priority items (400 kcal, 18g protein)
        assertEquals(400, resolved.totals.caloriesTarget)
        assertEquals(18.0, resolved.totals.proteinTargetG, 0.01)
        assertTrue(resolved.totals.isBareMinimum)
    }
}
