package com.bulktracker.app.domain

import com.bulktracker.app.data.db.EventEntity
import com.bulktracker.app.data.db.EventExceptionEntity
import java.time.LocalDate

fun mondayOf(day: LocalDate): Long {
    return day.toEpochDay() - (day.dayOfWeek.value - 1)
}

private fun matchesBase(e: EventEntity, day: LocalDate, startDate: LocalDate): Boolean {
    return when (e.repeatType) {
        "NONE" -> day.toEpochDay() == e.startDay
        "DAILY" -> {
            val diff = day.toEpochDay() - e.startDay
            diff >= 0 && (diff % e.repeatInterval == 0L)
        }
        "WEEKLY" -> {
            val bit = 1 shl (day.dayOfWeek.value - 1)
            val isBitSet = (e.weekdaysMask and bit) != 0
            val weeksDiff = (mondayOf(day) - mondayOf(startDate)) / 7
            isBitSet && weeksDiff >= 0 && (weeksDiff % e.repeatInterval == 0L)
        }
        "MONTHLY" -> {
            day.dayOfMonth == startDate.dayOfMonth
        }
        else -> false
    }
}

fun occursOn(e: EventEntity, day: LocalDate, exceptions: List<EventExceptionEntity>): Boolean {
    if (day.toEpochDay() < e.startDay) return false

    val startDate = LocalDate.ofEpochDay(e.startDay)
    val base = matchesBase(e, day, startDate)
    if (!base) return false

    if (e.endType == "UNTIL" && e.untilDay != null && day.toEpochDay() > e.untilDay) {
        return false
    }

    if (e.endType == "COUNT" && e.count != null) {
        var count = 0
        var cur = startDate
        var daysIterated = 0
        val targetEpoch = day.toEpochDay()
        while (cur.toEpochDay() <= targetEpoch && daysIterated < 1100) {
            if (matchesBase(e, cur, startDate)) {
                count++
            }
            cur = cur.plusDays(1)
            daysIterated++
        }
        if (count > e.count) return false
    }

    val exception = exceptions.firstOrNull { it.eventId == e.id && it.epochDay == day.toEpochDay() }
    if (exception?.type == "CANCELLED") return false

    return true
}

object RecurrenceExpander {
    fun occursOn(e: EventEntity, day: LocalDate, exceptions: List<EventExceptionEntity>): Boolean =
        com.bulktracker.app.domain.occursOn(e, day, exceptions)
}
