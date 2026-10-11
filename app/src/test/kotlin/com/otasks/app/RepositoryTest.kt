// SPDX-License-Identifier: GPL-3.0-or-later
package com.otasks.app
import android.content.Context
import androidx.room.Room
import androidx.room.withTransaction
import androidx.test.core.app.ApplicationProvider
import com.otasks.app.data.db.DayOverrideEntity
import com.otasks.app.data.db.FoodEntity
import com.otasks.app.data.db.LogEntryEntity
import com.otasks.app.data.db.MealItemEntity
import com.otasks.app.data.db.OTasksDatabase
import com.otasks.app.data.db.PreferenceEntity
import com.otasks.app.data.db.SectionEntity
import com.otasks.app.data.db.toDomain
import com.otasks.app.data.prefs.PreferenceStore
import com.otasks.app.data.repository.HistoryRepository
import com.otasks.app.data.repository.MealRepository
import com.otasks.app.data.repository.Reason
import com.otasks.app.data.repository.WriteResult
import com.otasks.domain.meals.Completion
import com.otasks.domain.meals.LogEntry
import com.otasks.domain.meals.Scope
import com.otasks.domain.meals.Scopes
import com.otasks.domain.meals.Totals
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneOffset

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class RepositoryTest {
    private lateinit var db: OTasksDatabase
    private lateinit var history: HistoryRepository
    private lateinit var meals: MealRepository
    private lateinit var prefs: PreferenceStore
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val day = LocalDate.of(2026, 10, 10)
    private var current = day
    private lateinit var food: FoodEntity
    private lateinit var section: SectionEntity
    private lateinit var item: MealItemEntity

    @Before fun before() =
        runBlocking {
            db =
                Room
                    .inMemoryDatabaseBuilder(
                        ApplicationProvider.getApplicationContext<Context>(),
                        OTasksDatabase::class.java,
                    ).addCallback(OTasksDatabase.Defaults)
                    .build()
            val clock = { Clock.fixed(current.atTime(12, 0).toInstant(ZoneOffset.UTC), ZoneOffset.UTC) }
            history = HistoryRepository(db, clock)
            meals = MealRepository(db, history, clock)
            prefs = PreferenceStore(db, scope, history)
            withContext(Dispatchers.IO) {
                food =
                    FoodEntity(
                        name = "Oats",
                        basis = "SERVING",
                        servingLabel = "1 bowl",
                        kcal = 100,
                        proteinG = 5.0,
                        createdAtMillis = 0,
                        lastUsedAtMillis = 0,
                    ).let {
                        it.copy(id = db.foodDao().insert(it))
                    }
                section =
                    SectionEntity(
                        name = "Breakfast",
                        size = "BIG",
                        timing = "FIXED",
                        idealMold = 480,
                        durationMin = 20,
                        colorKey = "blue",
                        sortOrder = 0,
                        activeFromDay = day.minusDays(10).toEpochDay(),
                    ).let {
                        it.copy(id = db.sectionDao().insert(it))
                    }
                item =
                    MealItemEntity(
                        sectionId = section.id,
                        foodId = food.id,
                        sortOrder = 0,
                        activeFromDay = section.activeFromDay,
                        isPriority = true,
                    ).let {
                        it.copy(id = db.mealItemDao().insert(it))
                    }
                db.preferenceDao().save(PreferenceEntity("historyStartDay", day.toEpochDay().toString()))
            }
        }

    @After fun after() {
        scope.cancel()
        db.close()
    }

    @Test fun freshDatabaseContainsOnlyFourAlarmProfiles() =
        runBlocking {
            withContext(Dispatchers.IO) {
                val fresh =
                    Room
                        .inMemoryDatabaseBuilder(
                            ApplicationProvider.getApplicationContext<Context>(),
                            OTasksDatabase::class.java,
                        ).addCallback(OTasksDatabase.Defaults)
                        .build()
                try {
                    val sqlite = fresh.openHelper.writableDatabase
                    val tables =
                        sqlite
                            .query("SELECT name FROM sqlite_master WHERE type='table'")
                            .use { cursor ->
                                buildList {
                                    while (cursor.moveToNext()) add(cursor.getString(0))
                                }
                            }.filterNot { it.startsWith("sqlite_") || it in setOf("android_metadata", "room_master_table") }
                    assertEquals(28, tables.size)
                    tables.forEach { table ->
                        sqlite.query("SELECT COUNT(*) FROM `$table`").use { cursor ->
                            assertTrue(cursor.moveToFirst())
                            assertEquals(if (table == "alarm_profiles") 4 else 0, cursor.getInt(0))
                        }
                    }
                } finally {
                    fresh.close()
                }
            }
        }

    @Test fun concurrentTogglesAndBulkAreAtomic() =
        runBlocking {
            coroutineScope { repeat(20) { launch(Dispatchers.Default) { meals.toggleItem(day, item.id) } } }
            withContext(Dispatchers.IO) { assertTrue(db.logEntryDao().all().isEmpty()) }
            coroutineScope { repeat(10) { launch { meals.markAllEaten(day, section.id) } } }
            withContext(Dispatchers.IO) { assertEquals(1, db.logEntryDao().all().size) }
        }

    @Test fun uniqueChecklistAllowsMultipleCustomLogs() =
        runBlocking {
            meals.toggleItem(day, item.id)
            withContext(Dispatchers.IO) {
                val original = db.logEntryDao().all().single()
                assertThrows(android.database.sqlite.SQLiteConstraintException::class.java) {
                    runBlocking { db.logEntryDao().insert(original.copy(id = 0)) }
                }
                repeat(2) { db.logEntryDao().insert(original.copy(id = 0, mealItemId = null, isCustom = true)) }
                assertEquals(3, db.logEntryDao().all().size)
            }
        }

    @Test fun materializesUnopenedDaysBeforeEditAndPreservesTargetsAndDenominator() =
        runBlocking {
            prefs.set("kcalTarget", "2000")
            meals.toggleItem(day, item.id)
            current = day.plusDays(3)
            assertTrue(meals.saveFood(food.copy(name = "New name", kcal = 900).toDomain()) is WriteResult.Success)
            meals.editItem(item.copy(servings = 2.0, isOptional = true, isPriority = false).toDomain())
            prefs.set("kcalTarget", "3000")
            prefs.set("goalType", "LOSE")
            withContext(Dispatchers.IO) {
                assertEquals(3, db.historyDayDao().all().size)
                assertTrue(db.historyDayDao().all().all { it.kcalTarget == 2000 && it.goalType == "MAINTAIN" })
                assertTrue(db.historyItemDao().all().all { it.name == "Oats" && it.kcal == 100 && !it.isOptional && it.isPriority })
                val preserved =
                    db
                        .historyItemDao()
                        .all()
                        .filter { it.epochDay == day.toEpochDay() }
                        .map { it.toDomain() }
                assertEquals(100, Completion.historicalPercent(preserved, db.logEntryDao().all().map { it.toDomain() }))
            }
            meals.toggleItem(day, item.id)
            meals.toggleItem(day, item.id)
            withContext(Dispatchers.IO) {
                val log = db.logEntryDao().all().single()
                assertEquals(100, log.kcalSnapshot)
                assertEquals("Oats", log.nameSnapshot)
                meals.correctLog(log.copy(multiplier = 2.0).toDomain())
                assertEquals(200, Totals.forDay(db.logEntryDao().all().map { it.toDomain() }).kcal)
                assertTrue(db.historyItemDao().all().all { it.kcal == 100 })
            }
            assertEquals(WriteResult.Invalid(Reason.PAST_PLAN), meals.remove("ITEM", item.id, day, Scope.JustDay))
        }

    @Test fun failedMutationRollsBackSnapshots() =
        runBlocking {
            current = day.plusDays(1)
            withContext(Dispatchers.IO) {
                try {
                    db.withTransaction {
                        history.materialize()
                        db.logEntryDao().insert(
                            LogEntryEntity(
                                epochDay = 0,
                                sectionId = 1,
                                sectionNameSnapshot = "x",
                                mealItemId = 1,
                                nameSnapshot = "x",
                                portionSnapshot = "x",
                                isCustom = false,
                                createdAtMillis = 0,
                            ),
                        )
                        error("rollback")
                    }
                } catch (
                    _: IllegalStateException,
                ) {
                }
                assertTrue(db.historyDayDao().all().isEmpty())
                assertTrue(db.historyItemDao().all().isEmpty())
                assertTrue(db.logEntryDao().all().isEmpty())
            }
        }

    @Test fun emptyDaysArePreservedAndCannotGainChecklistFromLaterAdd() =
        runBlocking {
            withContext(Dispatchers.IO) { db.mealItemDao().delete(item) }
            current = day.plusDays(2)
            history.catchUp()
            meals.addItem(item.copy(id = 0).toDomain(), current, Scope.FromNow)
            withContext(Dispatchers.IO) {
                assertEquals(2, db.historyDayDao().all().size)
                assertTrue(db.historyItemDao().all().isEmpty())
            }
            assertEquals(WriteResult.Invalid(Reason.NOT_VISIBLE), meals.toggleItem(day, item.id))
        }

    @Test fun justDayRemovesOnlyThatDayLogAndCanBringBack() =
        runBlocking {
            meals.toggleItem(day, item.id)
            meals.toggleItem(day.plusDays(1), item.id)
            meals.remove("ITEM", item.id, day, Scope.JustDay)
            withContext(Dispatchers.IO) {
                assertEquals(
                    day.plusDays(1).toEpochDay(),
                    db
                        .logEntryDao()
                        .all()
                        .single()
                        .epochDay,
                )
            }
            assertTrue(meals.bringBack("ITEM", item.id, day) is WriteResult.Success)
            assertTrue(meals.toggleItem(day, item.id) is WriteResult.Success)
        }

    @Test fun weekdayItemSplitRemapsFutureOnlyAndKeepsSnapshots() =
        runBlocking {
            meals.toggleItem(day, item.id)
            meals.toggleItem(day.plusDays(1), item.id)
            meals.remove("ITEM", item.id, day.plusDays(1), Scope.WeekdayFromNow)
            withContext(Dispatchers.IO) {
                val rows = db.mealItemDao().all()
                val newer = rows.single { it.id != item.id }
                assertEquals(day.toEpochDay(), rows.single { it.id == item.id }.activeUntilDay)
                assertEquals(63, newer.weekdaysMask)
                val logs = db.logEntryDao().all().sortedBy { it.epochDay }
                assertEquals(item.id, logs[0].mealItemId)
                assertEquals(newer.id, logs[1].mealItemId)
                assertEquals(100, logs[1].kcalSnapshot)
            }
        }

    @Test fun weekdaySectionSplitRemapsCustomLogsMergeTargetsAndAnchors() =
        runBlocking {
            val future = day.plusDays(1).toEpochDay()
            withContext(Dispatchers.IO) {
                db.sectionDao().insert(section.copy(id = 0, name = "Shake", anchorSectionId = section.id))
                db.mealItemDao().insert(item.copy(id = 0, activeUntilDay = day.minusDays(1).toEpochDay()))
                db.preferenceDao().save(PreferenceEntity("workoutAnchorSectionId", section.id.toString()))
                db.dayOverrideDao().insert(
                    DayOverrideEntity(
                        epochDay = future,
                        targetType = "SECTION",
                        targetId = 2,
                        action = "MERGE_INTO",
                        mergeIntoSectionId = section.id,
                    ),
                )
                db.dayOverrideDao().insert(DayOverrideEntity(epochDay = future, targetType = "ITEM", targetId = item.id, action = "ADD"))
            }
            meals.toggleItem(day, item.id)
            meals.toggleItem(day.plusDays(1), item.id)
            meals.addCustom(
                LogEntry(
                    epochDay = future,
                    sectionId = section.id,
                    sectionNameSnapshot = "Original",
                    nameSnapshot = "Custom",
                    portionSnapshot = "1",
                    isCustom = true,
                    createdAtMillis = 0,
                ),
            )
            meals.remove("SECTION", section.id, day.plusDays(1), Scope.WeekdayFromNow)
            withContext(Dispatchers.IO) {
                val newer = db.sectionDao().all().single { it.name == "Breakfast" && it.id != section.id }
                val newItem = db.mealItemDao().all().single { it.sectionId == newer.id }
                assertEquals(
                    newer.id,
                    db
                        .sectionDao()
                        .all()
                        .single { it.name == "Shake" }
                        .anchorSectionId,
                )
                assertEquals(
                    newer.id.toString(),
                    db
                        .preferenceDao()
                        .all()
                        .single { it.key == "workoutAnchorSectionId" }
                        .value,
                )
                assertEquals(
                    newer.id,
                    db
                        .dayOverrideDao()
                        .all()
                        .single { it.action == "MERGE_INTO" }
                        .mergeIntoSectionId,
                )
                assertEquals(
                    newItem.id,
                    db
                        .dayOverrideDao()
                        .all()
                        .single { it.targetType == "ITEM" }
                        .targetId,
                )
                val futureLogs = db.logEntryDao().all().filter { it.epochDay == future }
                assertEquals(2, futureLogs.size)
                assertTrue(
                    futureLogs.all {
                        it.sectionId ==
                            newer.id
                    },
                )
                assertEquals("Original", futureLogs.single { it.isCustom }.sectionNameSnapshot)
                assertEquals(
                    section.id,
                    db
                        .logEntryDao()
                        .all()
                        .single { it.epochDay == day.toEpochDay() }
                        .sectionId,
                )
            }
        }

    @Test fun fromNowEndsReferencedRowsAndDeletesStaleOverrides() =
        runBlocking {
            meals.toggleItem(day, item.id)
            withContext(Dispatchers.IO) {
                db.dayOverrideDao().insert(
                    DayOverrideEntity(epochDay = day.plusDays(2).toEpochDay(), targetType = "ITEM", targetId = item.id, action = "ADD"),
                )
            }
            meals.remove("ITEM", item.id, day, Scope.FromNow)
            withContext(Dispatchers.IO) {
                assertEquals(
                    day.minusDays(1).toEpochDay(),
                    db
                        .mealItemDao()
                        .all()
                        .single()
                        .activeUntilDay,
                )
                assertTrue(db.dayOverrideDao().all().isEmpty())
                assertEquals(1, db.logEntryDao().all().size)
            }
            assertEquals(WriteResult.Invalid(Reason.NOT_VISIBLE), meals.bringBack("ITEM", item.id, day))
            assertEquals(WriteResult.Success(false), meals.toggleItem(day, item.id))
            assertEquals(WriteResult.Invalid(Reason.NOT_VISIBLE), meals.toggleItem(day, item.id))
        }

    @Test fun rangeRemoveRetainsLogsAndFutureRowsAreUpdatedWithoutSplit() =
        runBlocking {
            meals.toggleItem(day, item.id)
            meals.remove("ITEM", item.id, day, Scope.DateRange(day, day.plusDays(2)))
            withContext(Dispatchers.IO) {
                assertEquals(3, db.dayOverrideDao().all().size)
                assertEquals(1, db.logEntryDao().all().size)
            }
            val result = meals.addItem(item.copy(id = 0).toDomain(), day.plusDays(3), Scope.FromNow) as WriteResult.Success
            meals.remove("ITEM", result.value, day.plusDays(3), Scope.WeekdayFromNow)
            withContext(Dispatchers.IO) { assertEquals(2, db.mealItemDao().all().size) }
            meals.remove("ITEM", result.value, day.plusDays(3), Scope.FromNow)
            withContext(Dispatchers.IO) { assertEquals(1, db.mealItemDao().all().size) }
        }

    @Test fun preferenceNullSurvivesStoreRestartAndResetKeepsHistoryAndUnknownKeys() =
        runBlocking {
            prefs.set("starredNudgeMold", "null")
            withContext(Dispatchers.IO) { db.preferenceDao().save(PreferenceEntity("futureKey", "keep")) }
            val reloaded = PreferenceStore(db, scope, history)
            withTimeout(5000) { reloaded.settings.first { it.starredNudgeMold == null } }
            prefs.reset(setOf("starredNudgeMold", "historyStartDay"))
            withContext(Dispatchers.IO) {
                val rows = db.preferenceDao().all()
                assertTrue(rows.any { it.key == "historyStartDay" })
                assertTrue(
                    rows.any {
                        it.key ==
                            "futureKey"
                    },
                )
                assertFalse(rows.any { it.key == "starredNudgeMold" })
            }
        }

    @Test fun everyAddScopeAndSectionRemovalScope() =
        runBlocking {
            val scopes = listOf(Scope.JustDay, Scope.WeekdayFromNow, Scope.FromNow, Scope.DateRange(day, day.plusDays(2)))
            for (scope in scopes) {
                val expected = Scopes.adding(scope, day, day)
                val sectionId =
                    (
                        meals.addSection(
                            section.copy(id = 0, name = "New meal").toDomain(),
                            day,
                            scope,
                        ) as WriteResult.Success
                    ).value
                val itemResult = meals.addItem(item.copy(id = 0, sectionId = sectionId).toDomain(), day, scope) as WriteResult.Success
                val itemId = itemResult.value
                withContext(Dispatchers.IO) {
                    val added = db.sectionDao().all().single { it.id == sectionId }
                    val addedItem = db.mealItemDao().all().single { it.id == itemId }
                    assertEquals(expected.from, added.activeFromDay)
                    assertEquals(expected.until, added.activeUntilDay)
                    assertEquals(expected.weekdaysMask, added.weekdaysMask)
                    assertEquals(expected.until, addedItem.activeUntilDay)
                    assertEquals(expected.weekdaysMask, addedItem.weekdaysMask)
                }
                assertTrue(meals.remove("SECTION", sectionId, day, scope) is WriteResult.Success)
            }
        }

    @Test fun rejectedWriteRollsBackBackfillAndWeightCorrectionsKeepOneRow() =
        runBlocking {
            current = day.plusDays(1)
            assertEquals(WriteResult.Invalid(Reason.INVALID_VALUE), meals.saveFood(food.copy(basis = "PER_100G").toDomain()))
            withContext(Dispatchers.IO) { assertTrue(db.historyDayDao().all().isEmpty()) }
            meals.correctWeight(day, 70.0, "")
            meals.correctWeight(day, 71.25, "Correction")
            withContext(Dispatchers.IO) {
                assertEquals(
                    71.25,
                    db
                        .weightDao()
                        .all()
                        .single()
                        .kg,
                    0.0,
                )
                assertEquals(
                    100,
                    db
                        .historyItemDao()
                        .all()
                        .single()
                        .kcal,
                )
            }
            assertEquals(WriteResult.Invalid(Reason.INVALID_VALUE), meals.correctWeight(day, Double.NaN, ""))
        }

    @Test fun settingsRegistryHasExactlyEveryPublicFieldAndCorrectDefaults() =
        runBlocking {
            val registry = com.otasks.app.ui.settings.PreferenceRegistry
            val entries = registry.entries
            val internal =
                setOf(
                    "onboardingDone",
                    "planStartDay",
                    "historyStartDay",
                    "lastExportMillis",
                    "autoBackupTreeUri",
                    "lastAutoBackupMillis",
                    "batteryHelpDismissed",
                    "lastDestination",
                )
            assertEquals(com.otasks.domain.PreferenceRegistry.keys - internal, entries.map { it.key }.toSet())
            assertEquals(entries.size, entries.map { it.key }.toSet().size)
            val defaults =
                com.otasks.domain.PreferenceRegistry
                    .encode(com.otasks.domain.Settings())
            entries.forEach { assertEquals(defaults[it.key], it.defaultValue) }
            val profile = com.otasks.domain.AlarmProfile("DEFAULT")
            val fields = profile.javaClass.declaredFields.filter { !it.isSynthetic && it.name != "kind" }
            assertEquals(fields.map { it.name }.toSet(), registry.profileEntries.map { it.key }.toSet())
            registry.profileEntries.forEach { entry ->
                val field = fields.single { it.name == entry.key }
                field.isAccessible = true
                assertEquals((field.get(profile)?.toString() ?: "null"), entry.defaultValue)
            }
            registry.preferencePages.forEach { page ->
                val keys = registry.pageKeys(page)
                keys.forEach { key -> prefs.set(key, defaults.getValue(key)) }
                prefs.reset(keys)
                withContext(Dispatchers.IO) { assertTrue(db.preferenceDao().all().none { it.key in keys }) }
            }
            val profiles = withContext(Dispatchers.IO) { db.alarmProfileDao().all().map { it.toDomain() } }
            assertEquals(4, profiles.size)
            assertEquals(
                "DEFAULT",
                com.otasks.domain.AlarmProfiles
                    .effective("MEAL", profiles)
                    .kind,
            )
            assertTrue(
                prefs.setAlarmProfile(
                    profiles.single { it.kind == "MEAL" }.copy(useDefault = false, snoozeMin = 15),
                ) is WriteResult.Success,
            )
            assertEquals(WriteResult.Invalid(Reason.INVALID_VALUE), prefs.setAlarmProfile(profile.copy(soundUri = "file:///secret")))
        }
}
