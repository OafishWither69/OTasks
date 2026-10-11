// SPDX-License-Identifier: GPL-3.0-or-later
package com.otasks.domain.meals
import com.otasks.domain.Settings
import java.time.LocalDate

data class Targets(
    val goalType: String,
    val kcal: Int?,
    val proteinG: Double?,
    val source: String,
)

object DayTargets {
    fun forDay(
        day: LocalDate,
        settings: Settings,
        weekdayGoals: List<WeekdayGoal>,
        state: DayState?,
        visibleItems: List<VisibleItem>,
        historyDay: HistoryDay? = null,
    ): Targets {
        if (historyDay !=
            null
        ) {
            require(historyDay.epochDay == day.toEpochDay())
            return Targets(historyDay.goalType, historyDay.kcalTarget, historyDay.proteinTargetG, historyDay.targetSource)
        }
        if (state?.bareMinimum ==
            true
        ) {
            return Targets(
                settings.goalType,
                visibleItems.sumOf { it.nutrients.kcal ?: 0 },
                visibleItems.sumOf {
                    it.nutrients.proteinG
                        ?: 0.0
                },
                "BARE_MINIMUM",
            )
        }
        val weekday = weekdayGoals.firstOrNull { settings.weekdayGoalsEnabled && it.weekday == day.dayOfWeek.value }
        return Targets(
            settings.goalType,
            weekday?.kcalTarget ?: settings.kcalTarget,
            weekday?.proteinTargetG ?: settings.proteinTargetG,
            if (weekday !=
                null
            ) {
                "WEEKDAY"
            } else {
                "EVERYDAY"
            },
        )
    }
}

enum class GoalTone { ACCENT, SUCCESS, WARNING, DANGER }

enum class GoalMessage { TO_GO, REACHED, ON_TARGET, OVER, LEFT, OVER_LIMIT }

data class GoalProgress(
    val fraction: Double,
    val tone: GoalTone,
    val message: GoalMessage,
    val remaining: Double,
)

object Goals {
    fun calories(
        total: Int,
        target: Int?,
        type: String,
    ): GoalProgress? {
        if (target == null) return null
        require(target > 0 && total >= 0)
        val fraction = total.toDouble() / target
        return when (type) {
            "GAIN" -> {
                GoalProgress(
                    fraction,
                    if (fraction >=
                        1
                    ) {
                        GoalTone.SUCCESS
                    } else {
                        GoalTone.ACCENT
                    },
                    if (fraction >=
                        1
                    ) {
                        GoalMessage.REACHED
                    } else {
                        GoalMessage.TO_GO
                    },
                    maxOf(0, target - total).toDouble(),
                )
            }

            "MAINTAIN" -> {
                when {
                    fraction < 0.9 -> {
                        GoalProgress(fraction, GoalTone.ACCENT, GoalMessage.TO_GO, (target - total).toDouble())
                    }

                    fraction <=
                        1.1 -> {
                        GoalProgress(fraction, GoalTone.SUCCESS, GoalMessage.ON_TARGET, 0.0)
                    }

                    else -> {
                        GoalProgress(
                            fraction,
                            GoalTone.WARNING,
                            GoalMessage.OVER,
                            (
                                total -
                                    target
                            ).toDouble(),
                        )
                    }
                }
            }

            "LOSE" -> {
                when {
                    fraction < 0.9 -> {
                        GoalProgress(fraction, GoalTone.ACCENT, GoalMessage.LEFT, (target - total).toDouble())
                    }

                    fraction <= 1 -> {
                        GoalProgress(
                            fraction,
                            GoalTone.WARNING,
                            GoalMessage.LEFT,
                            (
                                target -
                                    total
                            ).toDouble(),
                        )
                    }
                    ;else

                    -> {
                        GoalProgress(fraction, GoalTone.DANGER, GoalMessage.OVER_LIMIT, (total - target).toDouble())
                    }
                }
            }

            else -> {
                error("Unknown goal type")
            }
        }
    }

    fun protein(
        total: Double,
        target: Double?,
    ): GoalProgress? {
        if (target ==
            null
        ) {
            return null
        }
        require(total.isFinite() && total >= 0 && target.isFinite() && target > 0)
        return GoalProgress(
            total / target,
            if (total >=
                target
            ) {
                GoalTone.SUCCESS
            } else {
                GoalTone.ACCENT
            },
            if (total >=
                target
            ) {
                GoalMessage.REACHED
            } else {
                GoalMessage.TO_GO
            },
            maxOf(0.0, target - total),
        )
    }
}
