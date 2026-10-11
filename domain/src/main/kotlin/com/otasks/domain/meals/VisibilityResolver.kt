// SPDX-License-Identifier: GPL-3.0-or-later
package com.otasks.domain.meals
import java.time.LocalDate

data class VisibleItem(
    val item: MealItem,
    val section: Section,
    val sourceSection: Section,
    val food: Food,
    val skipped: Boolean,
    val pinnedMold: Int?,
) {
    val nutrients: Nutrients get() = Amount.nutrients(food, item)
}

data class ResolvedMeals(
    val items: List<VisibleItem>,
    val logs: List<LogEntry>,
)

object VisibilityResolver {
    fun lifecycle(
        from: Long,
        until: Long?,
        day: Long,
    ): Boolean = from <= day && (until == null || day <= until)

    fun inRange(
        from: Long,
        until: Long?,
        mask: Int,
        day: LocalDate,
    ): Boolean = lifecycle(from, until, day.toEpochDay()) && mask and (1 shl (day.dayOfWeek.value - 1)) != 0

    fun resolve(
        day: LocalDate,
        sections: List<Section>,
        items: List<MealItem>,
        foods: List<Food>,
        overrides: List<DayOverride>,
        state: DayState?,
        logs: List<LogEntry>,
    ): ResolvedMeals {
        val epoch = day.toEpochDay()
        val changes = overrides.filter { it.epochDay == epoch }.associateBy { it.targetType to it.targetId }
        val sectionMap = sections.associateBy { it.id }
        val foodMap = foods.associateBy { it.id }

        fun visible(s: Section): Boolean {
            if (!lifecycle(s.activeFromDay, s.activeUntilDay, epoch)) return false
            val action = changes["SECTION" to s.id]?.action
            return action != "REMOVE" && (action != null || inRange(s.activeFromDay, s.activeUntilDay, s.weekdaysMask, day))
        }
        val resolved =
            items
                .mapNotNull { item ->
                    val source = sectionMap[item.sectionId] ?: return@mapNotNull null
                    if (!visible(source) || !lifecycle(item.activeFromDay, item.activeUntilDay, epoch)) return@mapNotNull null
                    val itemAction = changes["ITEM" to item.id]?.action
                    if (itemAction == "REMOVE" || (
                            itemAction != "ADD" &&
                                !inRange(
                                    item.activeFromDay,
                                    item.activeUntilDay,
                                    item.weekdaysMask,
                                    day,
                                )
                        ) ||
                        (state?.bareMinimum == true && !item.isPriority)
                    ) {
                        return@mapNotNull null
                    }
                    val sourceChange = changes["SECTION" to source.id]
                    val target =
                        if (sourceChange?.action ==
                            "MERGE_INTO"
                        ) {
                            sectionMap[sourceChange.mergeIntoSectionId]?.takeIf {
                                visible(it) &&
                                    changes["SECTION" to it.id]?.action != "MERGE_INTO"
                            }
                                ?: return@mapNotNull null
                        } else {
                            source
                        }
                    val change = changes["SECTION" to target.id]
                    VisibleItem(
                        item,
                        target,
                        source,
                        foodMap[item.foodId] ?: return@mapNotNull null,
                        change?.action == "SKIP",
                        change?.pinnedMold?.takeIf {
                            change.action ==
                                "PIN_TIME"
                        },
                    )
                }.sortedWith(
                    compareBy<VisibleItem> { it.section.sortOrder }
                        .thenBy {
                            if (it.sourceSection.id ==
                                it.section.id
                            ) {
                                0
                            } else {
                                1
                            }
                        }.thenBy { it.sourceSection.sortOrder }
                        .thenBy { it.item.sortOrder }
                        .thenBy { it.item.id },
                )
        return ResolvedMeals(resolved, logs.filter { it.epochDay == epoch })
    }
}
