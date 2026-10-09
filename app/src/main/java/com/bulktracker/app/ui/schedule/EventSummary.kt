package com.bulktracker.app.ui.schedule

import com.bulktracker.app.data.db.EventEntity
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val shortDayFormat = DateTimeFormatter.ofPattern("EEE d MMM", Locale.US)
private val untilDayFormat = DateTimeFormatter.ofPattern("d MMM", Locale.US)

/**
 * Formats a time range using 12-hour format with en dash (–).
 * E.g., "6:00–7:30 PM", or if start and end have different AM/PM: "11:00 AM–1:00 PM".
 */
fun formatTimeRange(startMold: Int, endMold: Int): String {
    fun parseTime(mold: Int): Triple<Int, Int, String> {
        val totalMinutes = mold % 1440
        val normalized = if (totalMinutes < 0) totalMinutes + 1440 else totalMinutes
        val h24 = normalized / 60
        val m = normalized % 60
        val amPm = if (h24 < 12) "AM" else "PM"
        val h12 = when (val h = h24 % 12) {
            0 -> 12
            else -> h
        }
        return Triple(h12, m, amPm)
    }

    val (sH, sM, sAmPm) = parseTime(startMold)
    val (eH, eM, eAmPm) = parseTime(endMold)

    val startStr = if (sAmPm == eAmPm) {
        String.format(Locale.US, "%d:%02d", sH, sM)
    } else {
        String.format(Locale.US, "%d:%02d %s", sH, sM, sAmPm)
    }
    val endStr = String.format(Locale.US, "%d:%02d %s", eH, eM, eAmPm)
    return "$startStr–$endStr"
}

/**
 * Pure function producing the event summary string for list rows.
 * Examples:
 * - "Once · Sat 11 Oct · 5:00–9:00 PM"
 * - "Every day · 7:00–8:00 AM"
 * - "Every Tue, Thu · 6:00–7:30 PM · until 20 Dec"
 * - "Every 2 weeks on Mon · 10:00–11:00 AM · after 6 times"
 * - "Monthly on day 15 · 5:00–6:00 PM"
 */
fun eventSummary(e: EventEntity): String {
    val timeRange = formatTimeRange(e.startMold, e.endMold)

    if (e.repeatType == "NONE") {
        val date = LocalDate.ofEpochDay(e.startDay)
        val dateStr = date.format(shortDayFormat)
        return "Once · $dateStr · $timeRange"
    }

    val repeatDesc = when (e.repeatType) {
        "DAILY" -> {
            if (e.repeatInterval <= 1) "Every day" else "Every ${e.repeatInterval} days"
        }
        "WEEKLY" -> {
            val weekdays = listOf(
                0 to "Mon",
                1 to "Tue",
                2 to "Wed",
                3 to "Thu",
                4 to "Fri",
                5 to "Sat",
                6 to "Sun"
            )
            val selectedDays = weekdays
                .filter { (bit, _) -> (e.weekdaysMask and (1 shl bit)) != 0 }
                .map { it.second }
            val daysStr = selectedDays.joinToString(", ")

            if (e.repeatInterval <= 1) {
                if (daysStr.isEmpty()) "Every week" else "Every $daysStr"
            } else {
                if (daysStr.isEmpty()) {
                    "Every ${e.repeatInterval} weeks"
                } else {
                    "Every ${e.repeatInterval} weeks on $daysStr"
                }
            }
        }
        "MONTHLY" -> {
            val date = LocalDate.ofEpochDay(e.startDay)
            "Monthly on day ${date.dayOfMonth}"
        }
        else -> "Every day"
    }

    val endDesc = when (e.endType) {
        "UNTIL" -> {
            if (e.untilDay != null) {
                val untilDate = LocalDate.ofEpochDay(e.untilDay)
                " · until ${untilDate.format(untilDayFormat)}"
            } else {
                ""
            }
        }
        "COUNT" -> {
            if (e.count != null) {
                " · after ${e.count} times"
            } else {
                ""
            }
        }
        else -> ""
    }

    return "$repeatDesc · $timeRange$endDesc"
}
