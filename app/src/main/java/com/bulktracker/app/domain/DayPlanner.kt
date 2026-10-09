package com.bulktracker.app.domain

import com.bulktracker.app.data.db.DailyHabitEntity
import com.bulktracker.app.data.db.DayOverrideEntity
import com.bulktracker.app.data.db.SectionEntity
import com.bulktracker.app.data.db.SettingsEntity
import java.time.LocalDate

data class SlotPlan(
    val sectionId: Long,
    val mold: Int,
    val status: String, // "NORMAL" | "MOVED" | "PINNED" | "SKIPPED" | "MERGED" | "NOT_FITTING"
    val reason: String? = null
)

data class DayPlan(
    val wakeMold: Int,
    val gymStart: Int? = null,
    val gymEnd: Int? = null,
    val slots: List<SlotPlan> = emptyList(),
    val warnings: List<String> = emptyList(),
    val notFitting: List<SlotPlan> = emptyList()
)

interface DayPlanner {
    fun plan(
        day: LocalDate,
        sections: List<SectionEntity>,
        overrides: List<DayOverrideEntity>,
        busy: List<BusyBlock>,
        habit: DailyHabitEntity?,
        settings: SettingsEntity
    ): DayPlan
}
