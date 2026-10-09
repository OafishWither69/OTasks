package com.bulktracker.app.util

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale

object DateUtils {
    fun logicalToday(now: LocalDateTime, dayStartHour: Int): LocalDate {
        return if (now.hour < dayStartHour) {
            now.toLocalDate().minusDays(1)
        } else {
            now.toLocalDate()
        }
    }

    fun moldToLocalDateTime(day: LocalDate, mold: Int): LocalDateTime {
        return day.atStartOfDay().plusMinutes(mold.toLong())
    }

    fun formatMold(mold: Int): String {
        val totalMinutes = mold % 1440
        val normalizedMinutes = if (totalMinutes < 0) totalMinutes + 1440 else totalMinutes
        val hour24 = normalizedMinutes / 60
        val minute = normalizedMinutes % 60
        val amPm = if (hour24 < 12) "AM" else "PM"
        val hour12 = when (val h = hour24 % 12) {
            0 -> 12
            else -> h
        }
        return String.format(Locale.US, "%d:%02d %s", hour12, minute, amPm)
    }

    fun mondayOf(day: LocalDate): LocalDate {
        return day.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    }

    fun nowToMold(now: LocalDateTime, logicalDay: LocalDate): Int {
        return java.time.Duration.between(logicalDay.atStartOfDay(), now).toMinutes().toInt()
    }
}
