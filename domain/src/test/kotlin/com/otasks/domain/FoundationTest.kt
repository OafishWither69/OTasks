// SPDX-License-Identifier: GPL-3.0-or-later
package com.otasks.domain
import com.otasks.domain.meals.ActiveRange
import com.otasks.domain.meals.Amount
import com.otasks.domain.meals.Completion
import com.otasks.domain.meals.DayOverride
import com.otasks.domain.meals.DayState
import com.otasks.domain.meals.DayTargets
import com.otasks.domain.meals.DayTotals
import com.otasks.domain.meals.Food
import com.otasks.domain.meals.GoalTone
import com.otasks.domain.meals.Goals
import com.otasks.domain.meals.HistoryDay
import com.otasks.domain.meals.LogEntry
import com.otasks.domain.meals.MealItem
import com.otasks.domain.meals.Scope
import com.otasks.domain.meals.Scopes
import com.otasks.domain.meals.Section
import com.otasks.domain.meals.Targets
import com.otasks.domain.meals.Totals
import com.otasks.domain.meals.VisibilityResolver
import com.otasks.domain.meals.VisibleItem
import com.otasks.domain.meals.WeekdayGoal
import com.otasks.domain.time.LogicalDay
import com.otasks.domain.time.Mold
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.Locale

class FoundationTest {
    private val day = LocalDate.of(2026, 10, 10)
    private val section =
        Section(
            1,
            "Breakfast",
            "BIG",
            "FIXED",
            480,
            durationMin = 20,
            colorKey = "blue",
            sortOrder = 0,
            activeFromDay = day.minusDays(10).toEpochDay(),
        )
    private val food = Food(1, "Oats", "SERVING", "1 bowl", kcal = 100, proteinG = 5.0, createdAtMillis = 0, lastUsedAtMillis = 0)
    private val item = MealItem(1, 1, 1, sortOrder = 0, activeFromDay = section.activeFromDay)

    private fun resolve(
        items: List<MealItem> = listOf(item),
        changes: List<DayOverride> = emptyList(),
        state: DayState? = null,
        sections: List<Section> = listOf(section),
    ): List<VisibleItem> = VisibilityResolver.resolve(day, sections, items, listOf(food), changes, state, emptyList()).items

    private fun log(
        id: Long? = 1,
        kcal: Int? = 100,
        partial: Boolean = false,
    ) = LogEntry(
        epochDay = day.toEpochDay(),
        sectionId = 1,
        sectionNameSnapshot = "Breakfast",
        mealItemId = id,
        nameSnapshot = "Oats",
        portionSnapshot = "1 bowl",
        kcalSnapshot = kcal,
        proteinSnapshot = 5.0,
        isCustom =
            id == null,
        partialSnapshot = partial,
        createdAtMillis = 0,
    )

    @Test fun logicalBoundaryAndPickerRoundTrip() {
        assertEquals(day.minusDays(1), LogicalDay.logicalToday(day.atTime(3, 59), 4))
        assertEquals(day, LogicalDay.logicalToday(day.atTime(4, 0), 4))
        for (hour in 0..6) {
            for (mold in Mold.range(hour)) {
                val time = Mold.toPicker(mold, hour)
                assertEquals(mold, Mold.fromPicker(time.hour, time.minute, hour))
            }
        }
        assertThrows(IllegalArgumentException::class.java) { Mold.taskInstant(day, 1440, ZoneOffset.UTC) }
    }

    @Test fun dstUsesJavaTimeGapAndEarlierOverlap() {
        val zone = ZoneId.of("America/New_York")
        assertEquals(Instant.parse("2026-03-08T07:30:00Z"), Mold.instant(LocalDate.of(2026, 3, 8), 150, zone))
        assertEquals(Instant.parse("2026-11-01T05:30:00Z"), Mold.instant(LocalDate.of(2026, 11, 1), 90, zone))
    }

    @Test fun amountAndSnapshotTotals() {
        assertEquals(150, Amount.nutrients(food, item.copy(servings = 1.5)).kcal)
        assertEquals(0.3, Amount.factor("PER_100G", 1.0, 30.0), 0.0)
        assertThrows(IllegalArgumentException::class.java) { Amount.factor("PER_100G", 1.0, null) }
        assertThrows(IllegalArgumentException::class.java) { Amount.factor("SERVING", Double.NaN, null) }
        assertEquals(DayTotals(150, 17.5, 2), Totals.forDay(listOf(log().copy(multiplier = 1.5), log(null, null), log(2, 0, true))))
    }

    @Test fun visibilityLifecycleOverridesAndBareMinimum() {
        val add = DayOverride(epochDay = day.toEpochDay(), targetType = "ITEM", targetId = 1, action = "ADD")
        assertTrue(resolve(listOf(item.copy(weekdaysMask = 0))).isEmpty())
        assertEquals(1, resolve(listOf(item.copy(weekdaysMask = 0)), listOf(add)).size)
        assertTrue(resolve(listOf(item.copy(activeUntilDay = day.minusDays(1).toEpochDay())), listOf(add)).isEmpty())
        assertTrue(resolve(state = DayState(day.toEpochDay(), bareMinimum = true)).isEmpty())
        assertEquals(1, resolve(listOf(item.copy(isPriority = true)), state = DayState(day.toEpochDay(), bareMinimum = true)).size)
    }

    @Test fun mergesSkippedAndCompletion() {
        val target = section.copy(id = 2, name = "Lunch", sortOrder = 1)
        val change =
            DayOverride(epochDay = day.toEpochDay(), targetType = "SECTION", targetId = 1, action = "MERGE_INTO", mergeIntoSectionId = 2)
        val resolved = resolve(changes = listOf(change), sections = listOf(section, target))
        assertEquals(2L, resolved.single().section.id)
        assertEquals(1L, resolved.single().sourceSection.id)
        assertEquals(100, Completion.percent(resolve(), listOf(log())))
        assertEquals(0, Completion.percent(resolve(changes = listOf(change.copy(action = "SKIP"))), listOf(log())))
        assertEquals(0, Completion.percent(resolve(listOf(item.copy(isOptional = true))), listOf(log(null))))
    }

    @Test fun targetPrecedenceAndGoalBoundaries() {
        val settings = Settings(kcalTarget = 2000, proteinTargetG = 100.0, weekdayGoalsEnabled = true)
        val weekday = listOf(WeekdayGoal(6, 1500, null))
        assertEquals(Targets("MAINTAIN", 1500, 100.0, "WEEKDAY"), DayTargets.forDay(day, settings, weekday, null, resolve()))
        assertEquals(2000, DayTargets.forDay(day, settings.copy(weekdayGoalsEnabled = false), weekday, null, resolve()).kcal)
        assertEquals(100, DayTargets.forDay(day, settings, weekday, DayState(day.toEpochDay(), true), resolve()).kcal)
        val old = HistoryDay(day.toEpochDay(), "GAIN", 3000, 120.0, "EVERYDAY", false, false)
        assertEquals(3000, DayTargets.forDay(day, settings, weekday, DayState(day.toEpochDay(), true), resolve(), old).kcal)
        assertEquals(GoalTone.SUCCESS, Goals.calories(900, 1000, "MAINTAIN")?.tone)
        assertEquals(GoalTone.SUCCESS, Goals.calories(1100, 1000, "MAINTAIN")?.tone)
        assertEquals(GoalTone.WARNING, Goals.calories(1101, 1000, "MAINTAIN")?.tone)
        assertEquals(GoalTone.WARNING, Goals.calories(1000, 1000, "LOSE")?.tone)
        assertEquals(GoalTone.DANGER, Goals.calories(1001, 1000, "LOSE")?.tone)
        assertNull(Goals.calories(1000, null, "GAIN"))
        assertEquals(GoalTone.SUCCESS, Goals.protein(100.0, 100.0)?.tone)
    }

    @Test fun scopeRangesAndLimits() {
        assertEquals(ActiveRange(day.toEpochDay(), day.toEpochDay(), 127), Scopes.adding(Scope.JustDay, day, day))
        assertEquals(32, Scopes.adding(Scope.WeekdayFromNow, day, day).weekdaysMask)
        assertEquals(95, Scopes.removingWeekday(127, day))
        assertEquals(127, Scopes.adding(Scope.FromNow, day, day).weekdaysMask)
        assertEquals(day.plusDays(365).toEpochDay(), Scopes.adding(Scope.DateRange(day, day.plusDays(365)), day, day).until)
        assertThrows(IllegalArgumentException::class.java) { Scopes.adding(Scope.DateRange(day, day.plusDays(366)), day, day) }
        assertThrows(IllegalArgumentException::class.java) { Scopes.validate(Scope.JustDay, day.minusDays(1), day) }
    }

    @Test fun preferencesDefaultsNullInvalidUnknownAndRoundTrip() {
        assertEquals(1080, PreferenceRegistry.decode(emptyMap()).starredNudgeMold)
        assertNull(PreferenceRegistry.decode(mapOf("starredNudgeMold" to "null")).starredNudgeMold)
        val invalid = mutableListOf<String>()
        val decoded =
            PreferenceRegistry.decode(
                mapOf(
                    "dayStartHour" to "99",
                    "goalType" to "bad",
                    "proteinTargetG" to "NaN",
                    "futureKey" to "value",
                ),
                invalid::add,
            )
        assertEquals(Settings(), decoded)
        assertEquals(setOf("dayStartHour", "goalType", "proteinTargetG"), invalid.toSet())
        val original = Settings(starredNudgeMold = null, autoBackupTreeUri = "content://folder", kcalTarget = 2000)
        assertEquals(original, PreferenceRegistry.decode(PreferenceRegistry.encode(original)))
        assertEquals(80, PreferenceRegistry.keys.size)
        assertEquals("LB", defaultWeightUnit(Locale.US))
        assertEquals("KG", defaultWeightUnit(Locale.UK))
    }
}
