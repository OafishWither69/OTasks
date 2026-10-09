package com.bulktracker.app

import com.bulktracker.app.data.db.EventEntity
import com.bulktracker.app.data.db.EventExceptionEntity
import com.bulktracker.app.domain.occursOn
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

class RecurrenceExpanderTest {

    // (a) NONE true on its day, false the day after
    @Test
    fun testNoneOccursOnlyOnDay() {
        val start = LocalDate.of(2026, 10, 10)
        val event = EventEntity(
            id = 1,
            kind = "PERSONAL",
            title = "Doctor appointment",
            startDay = start.toEpochDay(),
            startMold = 600,
            endMold = 660,
            repeatType = "NONE"
        )

        assertTrue(occursOn(event, start, emptyList()))
        assertFalse(occursOn(event, start.plusDays(1), emptyList()))
        assertFalse(occursOn(event, start.minusDays(1), emptyList()))
    }

    // (b) WEEKLY Tue+Thu every week
    @Test
    fun testWeeklyTueThuEveryWeek() {
        val start = LocalDate.of(2026, 10, 6) // Tuesday
        val mask = (1 shl (DayOfWeek.TUESDAY.value - 1)) or (1 shl (DayOfWeek.THURSDAY.value - 1))
        val event = EventEntity(
            id = 2,
            kind = "EXTRA_CLASS",
            title = "Physics Lecture",
            startDay = start.toEpochDay(),
            startMold = 540,
            endMold = 600,
            repeatType = "WEEKLY",
            repeatInterval = 1,
            weekdaysMask = mask
        )

        // Tuesday week 0
        assertTrue(occursOn(event, LocalDate.of(2026, 10, 6), emptyList()))
        // Wednesday week 0
        assertFalse(occursOn(event, LocalDate.of(2026, 10, 7), emptyList()))
        // Thursday week 0
        assertTrue(occursOn(event, LocalDate.of(2026, 10, 8), emptyList()))
        // Friday week 0
        assertFalse(occursOn(event, LocalDate.of(2026, 10, 9), emptyList()))
        // Tuesday week 1
        assertTrue(occursOn(event, LocalDate.of(2026, 10, 13), emptyList()))
        // Thursday week 1
        assertTrue(occursOn(event, LocalDate.of(2026, 10, 15), emptyList()))
    }

    // (c) WEEKLY Mon every 2 weeks: true, false, true on three consecutive Mondays from the start
    @Test
    fun testWeeklyMonEveryTwoWeeks() {
        val start = LocalDate.of(2026, 10, 5) // Monday
        val mask = 1 shl (DayOfWeek.MONDAY.value - 1)
        val event = EventEntity(
            id = 3,
            kind = "PERSONAL",
            title = "Bi-weekly sync",
            startDay = start.toEpochDay(),
            startMold = 600,
            endMold = 660,
            repeatType = "WEEKLY",
            repeatInterval = 2,
            weekdaysMask = mask
        )

        val mon1 = LocalDate.of(2026, 10, 5)
        val mon2 = LocalDate.of(2026, 10, 12)
        val mon3 = LocalDate.of(2026, 10, 19)

        assertTrue(occursOn(event, mon1, emptyList()))
        assertFalse(occursOn(event, mon2, emptyList()))
        assertTrue(occursOn(event, mon3, emptyList()))
    }

    // (d) MONTHLY on the 31st skips 30-day months
    @Test
    fun testMonthlyOn31stSkips30DayMonths() {
        val start = LocalDate.of(2026, 1, 31) // January 31
        val event = EventEntity(
            id = 4,
            kind = "PERSONAL",
            title = "End of month check",
            startDay = start.toEpochDay(),
            startMold = 720,
            endMold = 780,
            repeatType = "MONTHLY",
            repeatInterval = 1
        )

        // Jan 31: true
        assertTrue(occursOn(event, LocalDate.of(2026, 1, 31), emptyList()))

        // Feb (no 31st): test all days in Feb 2026 (28 days) -> all false
        for (day in 1..28) {
            assertFalse(occursOn(event, LocalDate.of(2026, 2, day), emptyList()))
        }

        // Mar 31: true
        assertTrue(occursOn(event, LocalDate.of(2026, 3, 31), emptyList()))

        // Apr (30 days): all days in Apr 2026 -> all false
        for (day in 1..30) {
            assertFalse(occursOn(event, LocalDate.of(2026, 4, day), emptyList()))
        }

        // May 31: true
        assertTrue(occursOn(event, LocalDate.of(2026, 5, 31), emptyList()))
    }

    // (e) DAILY with COUNT 3: true days 1-3, false day 4
    @Test
    fun testDailyCountThree() {
        val start = LocalDate.of(2026, 10, 1)
        val event = EventEntity(
            id = 5,
            kind = "PERSONAL",
            title = "Three-day workshop",
            startDay = start.toEpochDay(),
            startMold = 600,
            endMold = 720,
            repeatType = "DAILY",
            repeatInterval = 1,
            endType = "COUNT",
            count = 3
        )

        // Day 1
        assertTrue(occursOn(event, LocalDate.of(2026, 10, 1), emptyList()))
        // Day 2
        assertTrue(occursOn(event, LocalDate.of(2026, 10, 2), emptyList()))
        // Day 3
        assertTrue(occursOn(event, LocalDate.of(2026, 10, 3), emptyList()))
        // Day 4
        assertFalse(occursOn(event, LocalDate.of(2026, 10, 4), emptyList()))
        // Day 5
        assertFalse(occursOn(event, LocalDate.of(2026, 10, 5), emptyList()))
    }

    // (f) UNTIL is inclusive
    @Test
    fun testUntilIsInclusive() {
        val start = LocalDate.of(2026, 10, 1)
        val until = LocalDate.of(2026, 10, 5)
        val event = EventEntity(
            id = 6,
            kind = "PERSONAL",
            title = "Until event",
            startDay = start.toEpochDay(),
            startMold = 600,
            endMold = 660,
            repeatType = "DAILY",
            repeatInterval = 1,
            endType = "UNTIL",
            untilDay = until.toEpochDay()
        )

        // Days 1 through 5: true
        assertTrue(occursOn(event, LocalDate.of(2026, 10, 1), emptyList()))
        assertTrue(occursOn(event, LocalDate.of(2026, 10, 4), emptyList()))
        assertTrue(occursOn(event, LocalDate.of(2026, 10, 5), emptyList())) // until day itself is true

        // Day 6: false
        assertFalse(occursOn(event, LocalDate.of(2026, 10, 6), emptyList()))
    }

    // (g) a CANCELLED exception makes that day false
    @Test
    fun testCancelledExceptionMakesDayFalse() {
        val start = LocalDate.of(2026, 10, 1)
        val event = EventEntity(
            id = 7,
            kind = "PERSONAL",
            title = "Weekly meeting",
            startDay = start.toEpochDay(),
            startMold = 600,
            endMold = 660,
            repeatType = "DAILY",
            repeatInterval = 1
        )

        val cancelledDay = LocalDate.of(2026, 10, 3)
        val exception = EventExceptionEntity(
            id = 10,
            eventId = 7,
            epochDay = cancelledDay.toEpochDay(),
            type = "CANCELLED"
        )

        assertTrue(occursOn(event, LocalDate.of(2026, 10, 1), listOf(exception)))
        assertTrue(occursOn(event, LocalDate.of(2026, 10, 2), listOf(exception)))
        assertFalse(occursOn(event, cancelledDay, listOf(exception))) // cancelled
        assertTrue(occursOn(event, LocalDate.of(2026, 10, 4), listOf(exception)))
    }
}
