package com.bulktracker.app

import com.bulktracker.app.data.db.FoodItemEntity
import com.bulktracker.app.data.db.LogEntryEntity
import com.bulktracker.app.data.db.SectionEntity
import com.bulktracker.app.data.db.SettingsEntity
import com.bulktracker.app.domain.TotalsCalculator
import com.bulktracker.app.domain.VisibilityResolver
import com.bulktracker.app.util.DateUtils
import com.bulktracker.app.util.Format
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class Phase1Test {

    @Test
    fun testMoldFormatting() {
        assertEquals("9:30 AM", DateUtils.formatMold(570))
        assertEquals("12:00 PM", DateUtils.formatMold(720))
        assertEquals("1:30 PM", DateUtils.formatMold(810))
        assertEquals("4:00 PM", DateUtils.formatMold(960))
        assertEquals("8:30 PM", DateUtils.formatMold(1230))
        assertEquals("11:15 PM", DateUtils.formatMold(1395))
        assertEquals("12:30 AM", DateUtils.formatMold(1470))
    }

    @Test
    fun testTotalsCalculation() {
        val items = listOf(
            FoodItemEntity(id = 1, sectionId = 1, name = "Milk", portionLabel = "500 ml", kcal = 400, proteinG = 18.0, isPriority = true, sortOrder = 0, activeFromDay = 0),
            FoodItemEntity(id = 2, sectionId = 1, name = "Gainer", portionLabel = "1 scoop", kcal = 310, proteinG = 12.5, isPriority = true, sortOrder = 1, activeFromDay = 0),
            FoodItemEntity(id = 3, sectionId = 2, name = "Apple", portionLabel = "1 medium", kcal = 95, proteinG = 0.5, isPriority = false, sortOrder = 0, activeFromDay = 0)
        )

        val logs = listOf(
            LogEntryEntity(id = 1, epochDay = 100, sectionId = 1, foodItemId = 1, nameSnapshot = "Milk", portionSnapshot = "500 ml", kcalSnapshot = 400, proteinSnapshot = 18.0, multiplier = 1.0, isCustom = false, createdAtMillis = 1000)
        )

        val totals = TotalsCalculator.calculate(
            visibleItems = items,
            logEntries = logs,
            habit = null,
            settings = SettingsEntity(kcalTarget = 3200, proteinTarget = 115.0)
        )

        assertEquals(400, totals.caloriesEaten)
        assertEquals(3200, totals.caloriesTarget)
        assertEquals(18.0, totals.proteinEatenG, 0.01)
        assertEquals(115.0, totals.proteinTargetG, 0.01)
        assertEquals(0, totals.unknownCount)
        // 1 of 3 items checked = 33%
        assertEquals(33, totals.completionPercent)
        assertEquals(1, totals.priorityItemsLeftCount) // Gainer is still left
    }

    @Test
    fun testVisibilityResolver() {
        val day = LocalDate.of(2026, 10, 5) // Monday
        val sections = listOf(
            SectionEntity(id = 1, name = "Morning Shake", role = "MORNING_SHAKE", size = "BIG", timing = "WAKE_RELATIVE", idealMold = 570, durationMin = 15, colorHex = "#3B82F6", sortOrder = 0, isMealSection = false, activeFromDay = 0),
            SectionEntity(id = 2, name = "Between Classes", role = "SNACK_1", size = "SNACK", timing = "FLEXIBLE", idealMold = 720, durationMin = 15, colorHex = "#0D9488", sortOrder = 1, isMealSection = false, activeFromDay = 0)
        )
        val items = listOf(
            FoodItemEntity(id = 1, sectionId = 1, name = "Milk", portionLabel = "500 ml", kcal = 400, proteinG = 18.0, isPriority = true, sortOrder = 0, activeFromDay = 0),
            FoodItemEntity(id = 2, sectionId = 2, name = "Apple", portionLabel = "1 medium", kcal = 95, proteinG = 0.5, isPriority = false, sortOrder = 0, activeFromDay = 0)
        )

        val resolved = VisibilityResolver.resolve(
            day = day,
            sections = sections,
            foodItems = items,
            overrides = emptyList(),
            logEntries = emptyList(),
            habit = null,
            settings = SettingsEntity()
        )

        assertEquals(2, resolved.sections.size)
        assertEquals(1, resolved.sections[0].items.size)
        assertFalse(resolved.sections[0].items[0].isChecked)
    }

    @Test
    fun testFormatters() {
        assertEquals("3,200", Format.formatKcal(3200))
        assertEquals("—", Format.formatKcal(null))
        assertEquals("96", Format.formatProtein(96.0))
        assertEquals("12.5", Format.formatProtein(12.5))
        assertEquals("—", Format.formatProtein(null))
    }
}
