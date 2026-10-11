// SPDX-License-Identifier: GPL-3.0-or-later
package com.otasks.domain.meals
import kotlin.math.roundToInt

data class Nutrients(
    val kcal: Int?,
    val proteinG: Double?,
    val partial: Boolean = false,
)

object Amount {
    fun factor(
        basis: String,
        servings: Double,
        grams: Double?,
    ): Double =
        when (basis) {
            "SERVING", "RECIPE" -> {
                require(servings.isFinite() && servings in 0.25..20.0)
                servings
            }

            "PER_100G" -> {
                require(grams != null && grams.isFinite() && grams in 1.0..2000.0)
                grams / 100.0
            }

            else -> {
                error("Unknown food basis")
            }
        }

    fun nutrients(
        food: Food,
        item: MealItem,
    ): Nutrients {
        val f = factor(food.basis, item.servings, item.grams)
        return Nutrients(
            food.kcal?.let {
                (
                    it *
                        f
                ).roundToInt()
            },
            food.proteinG?.times(f),
            food.isPartial,
        )
    }

    fun portion(
        food: Food,
        item: MealItem,
    ): String =
        if (food.basis ==
            "PER_100G"
        ) {
            "${number(requireNotNull(item.grams))} g"
        } else {
            "${number(item.servings)} × ${food.servingLabel}"
        }

    private fun number(n: Double): String = if (n % 1.0 == 0.0) n.toLong().toString() else n.toString()
}

data class DayTotals(
    val kcal: Int,
    val proteinG: Double,
    val unknownCount: Int,
)

object Totals {
    fun forDay(logs: List<LogEntry>): DayTotals =
        DayTotals(
            logs.sumOf { ((it.kcalSnapshot ?: 0) * it.multiplier).roundToInt() },
            logs.sumOf {
                (
                    it.proteinSnapshot
                        ?: 0.0
                ) *
                    it.multiplier
            },
            logs.count { it.kcalSnapshot == null || it.partialSnapshot },
        )
}

object Completion {
    fun percent(
        items: List<VisibleItem>,
        logs: List<LogEntry>,
    ): Int {
        val checklist = items.filterNot { it.item.isOptional }
        if (checklist.isEmpty()) return 0
        val checked = logs.mapNotNull { it.mealItemId }.toSet()
        return (checklist.count { !it.skipped && it.item.id in checked } * 100.0 / checklist.size).roundToInt()
    }

    fun historicalPercent(
        items: List<HistoryItem>,
        logs: List<LogEntry>,
    ): Int {
        val checklist = items.filterNot { it.isOptional }
        if (checklist.isEmpty()) return 0
        val checked = logs.mapNotNull { it.mealItemId }.toSet()
        return (checklist.count { !it.sectionSkipped && it.mealItemId in checked } * 100.0 / checklist.size).roundToInt()
    }
}
