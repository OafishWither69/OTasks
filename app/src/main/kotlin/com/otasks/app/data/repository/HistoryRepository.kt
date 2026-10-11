// SPDX-License-Identifier: GPL-3.0-or-later
package com.otasks.app.data.repository
import androidx.room.withTransaction
import com.otasks.app.data.db.HistoryDayEntity
import com.otasks.app.data.db.HistoryItemEntity
import com.otasks.app.data.db.OTasksDatabase
import com.otasks.app.data.db.toDomain
import com.otasks.domain.PreferenceRegistry
import com.otasks.domain.meals.Amount
import com.otasks.domain.meals.DayTargets
import com.otasks.domain.meals.VisibilityResolver
import com.otasks.domain.time.LogicalDay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Clock
import java.time.LocalDate

class HistoryRepository(
    private val db: OTasksDatabase,
    private val clock: () -> Clock = { Clock.systemDefaultZone() },
) {
    suspend fun today(): LocalDate = LogicalDay.today(clock(), settings().dayStartHour)

    internal suspend fun settings() = PreferenceRegistry.decode(db.preferenceDao().all().associate { it.key to it.value })

    suspend fun catchUp() = withContext(Dispatchers.IO) { db.withTransaction { materialize() } }

    // H1: callers invoke inside the same transaction as the plan mutation.
    internal suspend fun materialize() {
        check(db.inTransaction())
        val settings = settings()
        val start = settings.historyStartDay ?: return
        val today = LogicalDay.today(clock(), settings.dayStartHour).toEpochDay()
        val existing =
            db
                .historyDayDao()
                .all()
                .map { it.epochDay }
                .toSet()
        if (start >= today) return
        val sections = db.sectionDao().all().map { it.toDomain() }
        val items = db.mealItemDao().all().map { it.toDomain() }
        val foods = db.foodDao().all().map { it.toDomain() }
        val overrides = db.dayOverrideDao().all().map { it.toDomain() }
        val states = db.dayStateDao().all().associateBy { it.epochDay }
        val weekdays = db.weekdayGoalDao().all().map { it.toDomain() }
        val holidays = db.holidayDao().all()
        for (epoch in start until today) {
            if (epoch in existing) continue
            val day = LocalDate.ofEpochDay(epoch)
            val state = states[epoch]?.toDomain()
            val visible = VisibilityResolver.resolve(day, sections, items, foods, overrides, state, emptyList()).items
            val target = DayTargets.forDay(day, settings, weekdays, state, visible)
            db.historyDayDao().insert(
                HistoryDayEntity(
                    epoch,
                    target.goalType,
                    target.kcal,
                    target.proteinG,
                    target.source,
                    state?.bareMinimum == true,
                    holidays.any { epoch in it.startDay..it.endDay },
                ),
            )
            visible.forEach { v ->
                val nutrients = v.nutrients
                // Phase 1 uses the stored ideal/pin; Phase 5 supplies DayPlanner's resolved slot.
                db.historyItemDao().insert(
                    HistoryItemEntity(
                        epoch,
                        v.item.id,
                        v.section.id,
                        v.section.name,
                        v.section.colorKey,
                        v.section.sortOrder,
                        v.pinnedMold ?: v.section.idealMold,
                        v.section.durationMin,
                        if (v.skipped) {
                            "SKIPPED"
                        } else if (v.sourceSection.id !=
                            v.section.id
                        ) {
                            "MERGED"
                        } else if (v.pinnedMold != null) {
                            "PINNED"
                        } else {
                            "NORMAL"
                        },
                        v.item.sortOrder,
                        v.food.name,
                        Amount.portion(v.food, v.item),
                        nutrients.kcal,
                        nutrients.proteinG,
                        nutrients.partial,
                        v.item.isPriority,
                        v.item.isOptional,
                        v.skipped,
                    ),
                )
            }
        }
    }
}
