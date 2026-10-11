// SPDX-License-Identifier: GPL-3.0-or-later
package com.otasks.app.data.repository
import com.otasks.app.data.db.DayOverrideEntity
import com.otasks.app.data.db.LogEntryEntity
import com.otasks.app.data.db.MealItemEntity
import com.otasks.app.data.db.OTasksDatabase
import com.otasks.app.data.db.PreferenceEntity
import com.otasks.app.data.db.SectionEntity
import com.otasks.app.data.db.WeightEntity
import com.otasks.app.data.db.toDomain
import com.otasks.app.data.db.toEntity
import com.otasks.domain.meals.Amount
import com.otasks.domain.meals.Food
import com.otasks.domain.meals.LogEntry
import com.otasks.domain.meals.MealItem
import com.otasks.domain.meals.ResolvedMeals
import com.otasks.domain.meals.Scope
import com.otasks.domain.meals.Scopes
import com.otasks.domain.meals.Section
import com.otasks.domain.meals.VisibilityResolver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.withContext
import java.time.Clock
import java.time.LocalDate
import kotlin.math.roundToInt

class MealRepository(
    private val db: OTasksDatabase,
    private val history: HistoryRepository,
    private val clock: () -> Clock = {
        Clock.systemDefaultZone()
    },
) {
    suspend fun correctWeight(
        day: LocalDate,
        kg: Double,
        note: String,
    ): WriteResult<Unit> =
        withContext(Dispatchers.IO) {
            if (!kg.isFinite() || kg !in 20.0..300.0 || note.length > 200) return@withContext WriteResult.Invalid(Reason.INVALID_VALUE)
            db.writeTransaction {
                history.materialize()
                val old = db.weightDao().all().firstOrNull { it.epochDay == day.toEpochDay() }
                db.weightDao().save(WeightEntity(old?.id ?: 0, day.toEpochDay(), (kg * 100).roundToInt() / 100.0, note))
                WriteResult.Success(Unit)
            }
        }

    val sections: Flow<List<Section>> = db.sectionDao().observe().map { rows -> rows.map { it.toDomain() } }
    val items: Flow<List<MealItem>> = db.mealItemDao().observe().map { rows -> rows.map { it.toDomain() } }

    fun logs(
        start: Long,
        end: Long,
    ): Flow<List<LogEntry>> =
        db.logEntryDao().observe().map { rows ->
            rows
                .filter {
                    it.epochDay in start..end
                }.map { it.toDomain() }
        }

    private suspend fun resolved(day: LocalDate): ResolvedMeals =
        VisibilityResolver.resolve(
            day,
            db.sectionDao().all().map {
                it.toDomain()
            },
            db.mealItemDao().all().map { it.toDomain() },
            db.foodDao().all().map { it.toDomain() },
            db.dayOverrideDao().all().map { it.toDomain() },
            db
                .dayStateDao()
                .all()
                .firstOrNull {
                    it.epochDay ==
                        day.toEpochDay()
                }?.toDomain(),
            db.logEntryDao().all().map { it.toDomain() },
        )

    private suspend fun snapshot(
        day: LocalDate,
        id: Long,
    ): LogEntryEntity? {
        val epoch = day.toEpochDay()
        if (day < history.today()) {
            val h = db.historyItemDao().all().firstOrNull { it.epochDay == epoch && it.mealItemId == id } ?: return null
            return LogEntryEntity(
                epochDay = epoch,
                sectionId = h.sectionId,
                sectionNameSnapshot = h.sectionName,
                mealItemId = id,
                nameSnapshot = h.name,
                portionSnapshot = h.portion,
                kcalSnapshot = h.kcal,
                proteinSnapshot = h.proteinG,
                isCustom = false,
                partialSnapshot = h.partial,
                createdAtMillis = clock().millis(),
            )
        }
        val v = resolved(day).items.firstOrNull { it.item.id == id } ?: return null
        val n = v.nutrients
        return LogEntryEntity(
            epochDay = epoch,
            sectionId = v.section.id,
            sectionNameSnapshot = v.section.name,
            mealItemId = id,
            foodId = v.food.id,
            nameSnapshot = v.food.name,
            portionSnapshot = Amount.portion(v.food, v.item),
            kcalSnapshot = n.kcal,
            proteinSnapshot = n.proteinG,
            isCustom = false,
            partialSnapshot = n.partial,
            createdAtMillis = clock().millis(),
        )
    }

    suspend fun toggleItem(
        day: LocalDate,
        id: Long,
    ): WriteResult<Boolean> =
        withContext(Dispatchers.IO) {
            db.writeTransaction {
                history.materialize()
                val old = db.logEntryDao().all().firstOrNull { it.epochDay == day.toEpochDay() && it.mealItemId == id }
                if (old != null) {
                    db.logEntryDao().delete(old)
                    return@writeTransaction WriteResult.Success(false)
                }
                val entry = snapshot(day, id) ?: return@writeTransaction WriteResult.Invalid(Reason.NOT_VISIBLE)
                db.logEntryDao().insert(entry)
                WriteResult.Success(true)
            }
        }

    suspend fun markAllEaten(
        day: LocalDate,
        sectionId: Long,
    ): WriteResult<Unit> =
        withContext(Dispatchers.IO) {
            db.writeTransaction {
                history.materialize()
                val ids =
                    if (day <
                        history.today()
                    ) {
                        db
                            .historyItemDao()
                            .all()
                            .filter {
                                it.epochDay == day.toEpochDay() && it.sectionId == sectionId &&
                                    !it.isOptional &&
                                    !it.sectionSkipped
                            }.map { it.mealItemId }
                    } else {
                        resolved(day)
                            .items
                            .filter {
                                it.section.id == sectionId &&
                                    !it.item.isOptional &&
                                    !it.skipped
                            }.map { it.item.id }
                    }
                val checked =
                    db
                        .logEntryDao()
                        .all()
                        .filter { it.epochDay == day.toEpochDay() }
                        .mapNotNull { it.mealItemId }
                        .toSet()
                ids.filterNot { it in checked }.forEach { id -> snapshot(day, id)?.let { db.logEntryDao().insert(it) } }
                WriteResult.Success(Unit)
            }
        }

    suspend fun correctLog(entry: LogEntry): WriteResult<Unit> =
        withContext(Dispatchers.IO) {
            if (!entry.multiplier.isFinite() || entry.multiplier !in 0.25..4.0 || entry.kcalSnapshot?.let { it < 0 } == true ||
                entry.proteinSnapshot?.let { !it.isFinite() || it < 0 } == true ||
                entry.nameSnapshot.length !in 1..40 ||
                entry.portionSnapshot.length > 200
            ) {
                return@withContext WriteResult.Invalid(Reason.INVALID_VALUE)
            }
            db.writeTransaction {
                history.materialize()
                val old =
                    db.logEntryDao().all().firstOrNull { it.id == entry.id } ?: return@writeTransaction WriteResult.Invalid(
                        Reason.NOT_FOUND,
                    )
                // H2: correction changes actual nutrients, never checklist identity.
                db.logEntryDao().save(
                    old.copy(
                        nameSnapshot = entry.nameSnapshot,
                        portionSnapshot = entry.portionSnapshot,
                        kcalSnapshot = entry.kcalSnapshot,
                        proteinSnapshot = entry.proteinSnapshot,
                        multiplier = entry.multiplier,
                        partialSnapshot = entry.partialSnapshot,
                    ),
                )
                WriteResult.Success(Unit)
            }
        }

    suspend fun addCustom(entry: LogEntry): WriteResult<Long> =
        withContext(Dispatchers.IO) {
            if (!entry.isCustom || entry.mealItemId != null || entry.id != 0L || entry.nameSnapshot.length !in 1..40 ||
                entry.sectionNameSnapshot.length !in 1..40 ||
                !entry.multiplier.isFinite() ||
                entry.multiplier !in 0.25..4.0 ||
                entry.kcalSnapshot?.let { it < 0 } == true ||
                entry.proteinSnapshot?.let { !it.isFinite() || it < 0 } == true
            ) {
                return@withContext WriteResult.Invalid(Reason.INVALID_VALUE)
            }
            db.writeTransaction {
                history.materialize()
                WriteResult.Success(db.logEntryDao().insert(entry.toEntity().copy(createdAtMillis = clock().millis())))
            }
        }

    suspend fun deleteCustom(id: Long): WriteResult<Unit> =
        withContext(Dispatchers.IO) {
            db.writeTransaction {
                history.materialize()
                val row =
                    db.logEntryDao().all().firstOrNull { it.id == id && it.isCustom } ?: return@writeTransaction WriteResult.Invalid(
                        Reason.NOT_FOUND,
                    )
                db.logEntryDao().delete(row)
                WriteResult.Success(Unit)
            }
        }

    suspend fun saveFood(food: Food): WriteResult<Long> =
        withContext(Dispatchers.IO) {
            if (food.name.length !in 1..40 || food.basis !in setOf("SERVING", "PER_100G", "RECIPE") || food.servingLabel.length > 30 ||
                food.kcal?.let {
                    it !in 0..if (food.basis == "PER_100G") {
                        900
                    } else if (food.basis == "RECIPE") {
                        100000
                    } else {
                        5000
                    }
                } == true ||
                food.proteinG?.let {
                    !it.isFinite() || it < 0 || it >
                        if (food.basis ==
                            "PER_100G"
                        ) {
                            100
                        } else {
                            300
                        }
                } == true
            ) {
                return@withContext WriteResult.Invalid(Reason.INVALID_VALUE)
            }
            db.writeTransaction {
                history.materialize()
                val old = db.foodDao().all().firstOrNull { it.id == food.id }
                if (old != null && old.basis != food.basis &&
                    (
                        old.basis == "RECIPE" || food.basis == "RECIPE" || db.mealItemDao().all().any { it.foodId == food.id } ||
                            db.recipeIngredientDao().all().any { it.ingredientFoodId == food.id }
                    )
                ) {
                    return@writeTransaction WriteResult.Invalid(Reason.INVALID_VALUE)
                }
                if (old ==
                    null
                ) {
                    WriteResult.Success(db.foodDao().insert(food.toEntity()))
                } else {
                    db.foodDao().save(food.toEntity())
                    WriteResult.Success(food.id)
                }
            }
        }

    suspend fun addSection(
        section: Section,
        day: LocalDate,
        scope: Scope,
    ): WriteResult<Long> =
        withContext(Dispatchers.IO) {
            val range =
                runCatching {
                    Scopes.adding(scope, day, history.today())
                }.getOrElse { return@withContext WriteResult.Invalid(Reason.INVALID_SCOPE) }
            if (!validSection(section) || section.id != 0L) return@withContext WriteResult.Invalid(Reason.INVALID_VALUE)
            db.writeTransaction {
                history.materialize()
                WriteResult.Success(
                    db.sectionDao().insert(
                        section
                            .copy(
                                activeFromDay = range.from,
                                activeUntilDay = range.until,
                                weekdaysMask = range.weekdaysMask,
                            ).toEntity(),
                    ),
                )
            }
        }

    suspend fun addItem(
        item: MealItem,
        day: LocalDate,
        scope: Scope,
    ): WriteResult<Long> =
        withContext(Dispatchers.IO) {
            val range =
                runCatching {
                    Scopes.adding(scope, day, history.today())
                }.getOrElse { return@withContext WriteResult.Invalid(Reason.INVALID_SCOPE) }
            db.writeTransaction {
                history.materialize()
                if (item.id != 0L || !validItem(item) ||
                    db.sectionDao().all().none { it.id == item.sectionId }
                ) {
                    return@writeTransaction WriteResult.Invalid(Reason.INVALID_VALUE)
                }
                WriteResult.Success(
                    db.mealItemDao().insert(
                        item.copy(activeFromDay = range.from, activeUntilDay = range.until, weekdaysMask = range.weekdaysMask).toEntity(),
                    ),
                )
            }
        }

    suspend fun editSection(section: Section): WriteResult<Unit> =
        withContext(Dispatchers.IO) {
            if (!validSection(section)) return@withContext WriteResult.Invalid(Reason.INVALID_VALUE)
            db.writeTransaction {
                history.materialize()
                val old =
                    db.sectionDao().all().firstOrNull { it.id == section.id } ?: return@writeTransaction WriteResult.Invalid(
                        Reason.NOT_FOUND,
                    )
                if (old.activeUntilDay?.let { it < history.today().toEpochDay() } ==
                    true
                ) {
                    return@writeTransaction WriteResult.Invalid(Reason.PAST_PLAN)
                }
                db.sectionDao().save(
                    section
                        .copy(
                            activeFromDay = old.activeFromDay,
                            activeUntilDay = old.activeUntilDay,
                            weekdaysMask = old.weekdaysMask,
                        ).toEntity(),
                )
                WriteResult.Success(Unit)
            }
        }

    suspend fun editItem(item: MealItem): WriteResult<Unit> =
        withContext(Dispatchers.IO) {
            db.writeTransaction {
                history.materialize()
                val old =
                    db.mealItemDao().all().firstOrNull { it.id == item.id } ?: return@writeTransaction WriteResult.Invalid(
                        Reason.NOT_FOUND,
                    )
                if (old.activeUntilDay?.let { it < history.today().toEpochDay() } ==
                    true
                ) {
                    return@writeTransaction WriteResult.Invalid(Reason.PAST_PLAN)
                }
                if (!validItem(item)) return@writeTransaction WriteResult.Invalid(Reason.INVALID_VALUE)
                db.mealItemDao().save(
                    old.copy(
                        servings = item.servings,
                        grams = item.grams,
                        isPriority = item.isPriority,
                        isOptional = item.isOptional,
                        sortOrder = item.sortOrder,
                    ),
                )
                WriteResult.Success(Unit)
            }
        }

    private fun validSection(s: Section): Boolean =
        s.name.length in 1..40 && s.size in setOf("BIG", "SNACK") &&
            s.timing in setOf("FIXED", "FLEXIBLE", "AFTER_WAKE", "AFTER_MEAL", "AFTER_WORKOUT") &&
            s.durationMin in 5..120 &&
            s.offsetMin in 0..600 &&
            s.anchorSectionId != s.id &&
            s.colorKey in setOf("blue", "teal", "orange", "violet", "red", "pink", "green", "gold")

    private suspend fun validItem(i: MealItem): Boolean {
        val food = db.foodDao().all().firstOrNull { it.id == i.foodId } ?: return false
        return runCatching { Amount.factor(food.basis, i.servings, i.grams) }.isSuccess
    }

    suspend fun remove(
        targetType: String,
        id: Long,
        day: LocalDate,
        scope: Scope,
    ): WriteResult<Unit> =
        withContext(Dispatchers.IO) {
            if (targetType !in setOf("ITEM", "SECTION")) return@withContext WriteResult.Invalid(Reason.INVALID_VALUE)
            if (day < history.today()) return@withContext WriteResult.Invalid(Reason.PAST_PLAN)
            if (runCatching {
                    Scopes.validate(
                        scope,
                        day,
                        history.today(),
                    )
                }.isFailure
            ) {
                return@withContext WriteResult.Invalid(Reason.INVALID_SCOPE)
            }
            db.writeTransaction {
                history.materialize()
                val section =
                    db
                        .sectionDao()
                        .all()
                        .firstOrNull { it.id == id }
                        .takeIf { targetType == "SECTION" }
                val item =
                    db
                        .mealItemDao()
                        .all()
                        .firstOrNull { it.id == id }
                        .takeIf { targetType == "ITEM" }
                if (section == null && item == null) return@writeTransaction WriteResult.Invalid(Reason.NOT_FOUND)
                val epoch = day.toEpochDay()
                val until = section?.activeUntilDay ?: item?.activeUntilDay
                if (until != null && until < epoch) return@writeTransaction WriteResult.Invalid(Reason.NOT_VISIBLE)
                when (scope) {
                    Scope.JustDay -> {
                        db.dayOverrideDao().insert(
                            DayOverrideEntity(epochDay = epoch, targetType = targetType, targetId = id, action = "REMOVE"),
                        )
                        val ids =
                            if (section != null) {
                                db
                                    .mealItemDao()
                                    .all()
                                    .filter { it.sectionId == id }
                                    .map { it.id }
                                    .toSet()
                            } else {
                                setOf(id)
                            }
                        db
                            .logEntryDao()
                            .all()
                            .filter {
                                it.epochDay == epoch && (it.mealItemId in ids || (section != null && it.sectionId == id))
                            }.forEach { db.logEntryDao().delete(it) }
                    }

                    is Scope.DateRange -> {
                        for (d in scope.start.toEpochDay()..scope.end.toEpochDay()) {
                            db.dayOverrideDao().insert(
                                DayOverrideEntity(epochDay = d, targetType = targetType, targetId = id, action = "REMOVE"),
                            )
                        }
                    }

                    Scope.FromNow -> {
                        end(section, item, epoch)
                    }

                    Scope.WeekdayFromNow -> {
                        val mask = Scopes.removingWeekday(section?.weekdaysMask ?: requireNotNull(item).weekdaysMask, day)
                        if (mask == 0) end(section, item, epoch) else split(section, item, epoch, mask)
                    }
                }
                WriteResult.Success(Unit)
            }
        }

    private suspend fun end(
        section: SectionEntity?,
        item: MealItemEntity?,
        epoch: Long,
    ) {
        val itemRows = if (section != null) db.mealItemDao().all().filter { it.sectionId == section.id } else listOf(requireNotNull(item))
        val ids = itemRows.map { it.id }.toSet()
        db
            .dayOverrideDao()
            .all()
            .filter {
                it.epochDay >= epoch &&
                    (
                        (it.targetType == "ITEM" && it.targetId in ids) ||
                            (
                                section != null &&
                                    ((it.targetType == "SECTION" && it.targetId == section.id) || it.mergeIntoSectionId == section.id)
                            )
                    )
            }.forEach { db.dayOverrideDao().delete(it) }
        val logs = db.logEntryDao().all()
        val historic = db.historyItemDao().all()
        for (row in itemRows) {
            if (row.activeUntilDay != null && row.activeUntilDay < epoch) continue
            val referenced = logs.any { it.mealItemId == row.id } || historic.any { it.mealItemId == row.id }
            if (row.activeFromDay >= epoch &&
                !referenced
            ) {
                db.mealItemDao().delete(row)
            } else {
                db.mealItemDao().save(
                    row.copy(
                        activeUntilDay =
                            if (row.activeFromDay <
                                epoch
                            ) {
                                epoch - 1
                            } else {
                                row.activeFromDay - 1
                            },
                    ),
                )
            }
        }
        if (section != null) {
            val referenced = logs.any { it.sectionId == section.id } || historic.any { it.sectionId == section.id }
            if (section.activeFromDay >= epoch &&
                !referenced
            ) {
                db.sectionDao().delete(section)
            } else {
                db.sectionDao().save(
                    section.copy(
                        activeUntilDay =
                            if (section.activeFromDay <
                                epoch
                            ) {
                                epoch - 1
                            } else {
                                section.activeFromDay - 1
                            },
                    ),
                )
            }
        }
    }

    private suspend fun split(
        section: SectionEntity?,
        item: MealItemEntity?,
        epoch: Long,
        mask: Int,
    ) {
        val sectionMap = mutableMapOf<Long, Long>()
        val itemMap = mutableMapOf<Long, Long>()
        val endedItems = mutableSetOf<Long>()
        if (section != null) {
            if (section.activeFromDay >= epoch) {
                db.sectionDao().save(section.copy(weekdaysMask = mask))
                return
            }
            val newSection = db.sectionDao().insert(section.copy(id = 0, activeFromDay = epoch, weekdaysMask = mask))
            sectionMap[section.id] = newSection
            db.sectionDao().save(section.copy(activeUntilDay = epoch - 1))
            db.mealItemDao().all().filter { it.sectionId == section.id }.forEach { row ->
                if (row.activeUntilDay != null && row.activeUntilDay < epoch) {
                    endedItems += row.id
                } else {
                    itemMap[row.id] =
                        db.mealItemDao().insert(row.copy(id = 0, sectionId = newSection, activeFromDay = maxOf(epoch, row.activeFromDay)))
                    db.mealItemDao().save(row.copy(activeUntilDay = epoch - 1))
                }
            }
        } else {
            val row = requireNotNull(item)
            if (row.activeFromDay >= epoch) {
                db.mealItemDao().save(row.copy(weekdaysMask = mask))
                return
            }
            itemMap[row.id] = db.mealItemDao().insert(row.copy(id = 0, activeFromDay = epoch, weekdaysMask = mask))
            db.mealItemDao().save(row.copy(activeUntilDay = epoch - 1))
        }
        db.dayOverrideDao().all().filter { it.epochDay >= epoch }.forEach { row ->
            if (row.targetType == "ITEM" && row.targetId in endedItems) {
                db.dayOverrideDao().delete(row)
            } else {
                val mapped = if (row.targetType == "ITEM") itemMap[row.targetId] else sectionMap[row.targetId]
                val merge = sectionMap[row.mergeIntoSectionId] ?: row.mergeIntoSectionId
                if (mapped != null ||
                    merge != row.mergeIntoSectionId
                ) {
                    db.dayOverrideDao().save(row.copy(targetId = mapped ?: row.targetId, mergeIntoSectionId = merge))
                }
            }
        }
        db.logEntryDao().all().filter { it.epochDay >= epoch }.forEach { row ->
            val mappedItem = itemMap[row.mealItemId] ?: row.mealItemId
            val mappedSection = sectionMap[row.sectionId] ?: row.sectionId
            if (mappedItem != row.mealItemId ||
                mappedSection != row.sectionId
            ) {
                db.logEntryDao().save(row.copy(mealItemId = mappedItem, sectionId = mappedSection))
            }
        }
        db.sectionDao().all().forEach { row ->
            sectionMap[row.anchorSectionId]?.let { db.sectionDao().save(row.copy(anchorSectionId = it)) }
        }
        val anchor = db.preferenceDao().all().firstOrNull { it.key == "workoutAnchorSectionId" }
        sectionMap[anchor?.value?.toLongOrNull()]?.let {
            db.preferenceDao().save(
                PreferenceEntity("workoutAnchorSectionId", it.toString()),
            )
        }
    }

    suspend fun bringBack(
        targetType: String,
        id: Long,
        day: LocalDate,
    ): WriteResult<Unit> =
        withContext(Dispatchers.IO) {
            if (day < history.today()) return@withContext WriteResult.Invalid(Reason.PAST_PLAN)
            db.writeTransaction {
                history.materialize()
                val epoch = day.toEpochDay()
                val active =
                    when (targetType) {
                        "ITEM" -> {
                            db.mealItemDao().all().any {
                                it.id == id &&
                                    VisibilityResolver.lifecycle(
                                        it.activeFromDay,
                                        it.activeUntilDay,
                                        epoch,
                                    )
                            }
                        }

                        "SECTION" -> {
                            db.sectionDao().all().any {
                                it.id == id &&
                                    VisibilityResolver.lifecycle(
                                        it.activeFromDay,
                                        it.activeUntilDay,
                                        epoch,
                                    )
                            }
                        }

                        else -> {
                            false
                        }
                    }
                if (!active) return@writeTransaction WriteResult.Invalid(Reason.NOT_VISIBLE)
                db.dayOverrideDao().insert(DayOverrideEntity(epochDay = epoch, targetType = targetType, targetId = id, action = "ADD"))
                WriteResult.Success(Unit)
            }
        }
}
