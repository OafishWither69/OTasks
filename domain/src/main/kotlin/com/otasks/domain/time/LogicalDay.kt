// SPDX-License-Identifier: GPL-3.0-or-later
package com.otasks.domain.time
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

object LogicalDay {
    fun logicalToday(
        now: LocalDateTime,
        dayStartHour: Int,
    ): LocalDate {
        require(dayStartHour in 0..6)
        return if (now.hour < dayStartHour) now.toLocalDate().minusDays(1) else now.toLocalDate()
    }

    fun today(
        clock: Clock,
        dayStartHour: Int,
    ): LocalDate = logicalToday(LocalDateTime.now(clock), dayStartHour)

    fun nextBoundary(
        clock: Clock,
        dayStartHour: Int,
    ): Instant =
        today(clock, dayStartHour)
            .plusDays(1)
            .atTime(dayStartHour, 0)
            .atZone(clock.zone)
            .toInstant()
}

object Mold {
    fun range(dayStartHour: Int): IntRange {
        require(dayStartHour in 0..6)
        return dayStartHour * 60 until 1440 + dayStartHour * 60
    }

    fun fromPicker(
        hour: Int,
        minute: Int,
        dayStartHour: Int,
    ): Int {
        require(hour in 0..23 && minute in 0..59)
        range(dayStartHour)
        return hour * 60 +
            minute +
            if (hour < dayStartHour) 1440 else 0
    }

    fun toPicker(
        mold: Int,
        dayStartHour: Int,
    ): LocalTime {
        require(mold in range(dayStartHour))
        return LocalTime.of((mold % 1440) / 60, mold % 60)
    }

    fun instant(
        day: LocalDate,
        mold: Int,
        zone: ZoneId,
    ): Instant =
        day
            .atStartOfDay()
            .plusMinutes(mold.toLong())
            .atZone(zone)
            .toInstant()

    fun taskInstant(
        day: LocalDate,
        minute: Int,
        zone: ZoneId,
    ): Instant {
        require(minute in 0..1439)
        return instant(day, minute, zone)
    }
}
