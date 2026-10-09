package com.bulktracker.app.domain

import com.bulktracker.app.data.db.DailyHabitEntity
import com.bulktracker.app.data.db.DayOverrideEntity
import com.bulktracker.app.data.db.SectionEntity
import com.bulktracker.app.data.db.SettingsEntity
import java.time.LocalDate

class IdealTimePlanner : DayPlanner {
    override fun plan(
        day: LocalDate,
        sections: List<SectionEntity>,
        overrides: List<DayOverrideEntity>,
        busy: List<BusyBlock>,
        habit: DailyHabitEntity?,
        settings: SettingsEntity
    ): DayPlan {
        // IdealTimePlanner (Phase 3) implements it WITHOUT busy blocks:
        // wake = settings.noClassWakeMold;
        // steps 2 and 3 below;
        // MORNING_SHAKE at wake + morningShakeAfterWakeMin;
        // every other section at idealMold, except the NIGHT_SHAKE on a rest day (step 5 rule) at dinner idealMold + restDayNightShakeAfterDinnerMin;
        // all NORMAL; no warnings;
        // no gym block on rest days, otherwise gym as step 5.
        val wake = settings.noClassWakeMold

        val overrideMap = overrides.associateBy { it.targetId }
        val dinnerSection = sections.firstOrNull { it.role == "DINNER" }

        // Step 5: Gym
        // isRest = (habit?.gymPlan == "REST") OR (habit?.gymPlan != "TRAIN" AND bit (D.dayOfWeek.value - 1) is set in settings.gymRestDaysMask)
        val weekdayBit = 1 shl (day.dayOfWeek.value - 1)
        val isRest = when {
            habit?.gymPlan == "REST" -> true
            habit?.gymPlan == "TRAIN" -> false
            (settings.gymRestDaysMask and weekdayBit) != 0 -> true
            else -> false
        }

        val dinnerOverride = dinnerSection?.let { overrideMap[it.id] }
        val dinnerPlacedTime: Int? = when {
            dinnerSection == null -> null
            dinnerOverride?.action == "SKIP" || dinnerOverride?.action == "MERGE_INTO" -> null
            dinnerOverride?.action == "PIN_TIME" && dinnerOverride.pinnedMold != null -> dinnerOverride.pinnedMold
            else -> dinnerSection.idealMold
        }

        val gymStart: Int?
        val gymEnd: Int?
        if (isRest || dinnerPlacedTime == null) {
            gymStart = null
            gymEnd = null
        } else {
            gymStart = dinnerPlacedTime + settings.gymAfterDinnerMin
            gymEnd = gymStart + settings.gymDurationMin
        }

        val slots = mutableListOf<SlotPlan>()
        for (section in sections) {
            val ov = overrideMap[section.id]
            when (ov?.action) {
                "SKIP" -> {
                    // Step 2: Sections with override SKIP -> status SKIPPED, mold = their ideal, not placed
                    slots.add(SlotPlan(sectionId = section.id, mold = section.idealMold, status = "SKIPPED"))
                }
                "MERGE_INTO" -> {
                    // Step 2: Sections with override MERGE_INTO -> status MERGED, not placed
                    slots.add(SlotPlan(sectionId = section.id, mold = section.idealMold, status = "MERGED"))
                }
                "PIN_TIME" -> {
                    // Step 3: Sections with override PIN_TIME -> status PINNED, mold = pinnedMold, placed
                    val mold = ov.pinnedMold ?: section.idealMold
                    slots.add(SlotPlan(sectionId = section.id, mold = mold, status = "PINNED"))
                }
                else -> {
                    // Step 4 & 6:
                    val mold = when {
                        section.role == "MORNING_SHAKE" -> wake + settings.morningShakeAfterWakeMin
                        (section.role == "NIGHT_SHAKE" || section.timing == "AFTER_GYM") && isRest -> {
                            val dinnerIdeal = dinnerSection?.idealMold ?: 1230
                            dinnerIdeal + settings.restDayNightShakeAfterDinnerMin
                        }
                        else -> section.idealMold
                    }
                    slots.add(SlotPlan(sectionId = section.id, mold = mold, status = "NORMAL"))
                }
            }
        }

        slots.sortBy { it.mold }

        return DayPlan(
            wakeMold = wake,
            gymStart = gymStart,
            gymEnd = gymEnd,
            slots = slots,
            warnings = emptyList(),
            notFitting = emptyList()
        )
    }
}
