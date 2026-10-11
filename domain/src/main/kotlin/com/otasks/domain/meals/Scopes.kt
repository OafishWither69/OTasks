// SPDX-License-Identifier: GPL-3.0-or-later
package com.otasks.domain.meals
import java.time.LocalDate

sealed interface Scope {
    data object JustDay : Scope

    data object WeekdayFromNow : Scope

    data object FromNow : Scope

    data class DateRange(
        val start: LocalDate,
        val end: LocalDate,
    ) : Scope
}

data class ActiveRange(
    val from: Long,
    val until: Long?,
    val weekdaysMask: Int,
)

object Scopes {
    fun validate(
        scope: Scope,
        day: LocalDate,
        today: LocalDate,
    ) {
        require(!day.isBefore(today)) { "Past plans are preserved" }
        if (scope is Scope.DateRange) {
            require(
                !scope.start.isBefore(today) && !scope.end.isBefore(scope.start) && scope.end.toEpochDay() - scope.start.toEpochDay() < 366,
            ) {
                "Invalid scope range"
            }
        }
    }

    fun adding(
        scope: Scope,
        day: LocalDate,
        today: LocalDate,
    ): ActiveRange {
        validate(scope, day, today)
        return when (scope) {
            Scope.JustDay -> ActiveRange(day.toEpochDay(), day.toEpochDay(), 127)
            Scope.WeekdayFromNow -> ActiveRange(day.toEpochDay(), null, 1 shl (day.dayOfWeek.value - 1))
            Scope.FromNow -> ActiveRange(day.toEpochDay(), null, 127)
            is Scope.DateRange -> ActiveRange(scope.start.toEpochDay(), scope.end.toEpochDay(), 127)
        }
    }

    fun removingWeekday(
        mask: Int,
        day: LocalDate,
    ): Int = mask and (1 shl (day.dayOfWeek.value - 1)).inv()
}
