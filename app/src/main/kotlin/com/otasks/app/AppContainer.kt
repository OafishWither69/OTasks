// SPDX-License-Identifier: GPL-3.0-or-later
package com.otasks.app
import android.content.Context
import android.os.UserManager
import android.util.Log
import com.otasks.app.data.db.OTasksDatabase
import com.otasks.app.data.prefs.PreferenceStore
import com.otasks.app.data.repository.HistoryRepository
import com.otasks.app.data.repository.MealRepository
import com.otasks.domain.time.LogicalDay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.Duration
import java.time.LocalDate

class AppContainer(
    private val context: Context,
    private val clock: () -> Clock = { Clock.systemDefaultZone() },
) {
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    val selectedDate = MutableStateFlow<LocalDate>(LogicalDay.today(clock(), 4))
    val database: OTasksDatabase by lazy {
        check(context.getSystemService(UserManager::class.java).isUserUnlocked)
        OTasksDatabase.open(context)
    }
    val history: HistoryRepository by lazy { HistoryRepository(database, clock) }
    val preferences: PreferenceStore by lazy { PreferenceStore(database, scope, history) }
    val meals: MealRepository by lazy { MealRepository(database, history, clock) }
    private var started = false
    private var lastToday = selectedDate.value
    private var ticker: Job? = null

    fun resume() {
        if (!context.getSystemService(UserManager::class.java).isUserUnlocked) return
        scope.launch(Dispatchers.IO) { refreshDay() }
        if (ticker?.isActive == true) return
        ticker =
            scope.launch(Dispatchers.IO) {
                preferences.settings.map { it.dayStartHour }.distinctUntilChanged().collectLatest { hour ->
                    while (true) {
                        refreshDay()
                        val now = clock()
                        delay(Duration.between(now.instant(), LogicalDay.nextBoundary(now, hour)).toMillis().coerceAtLeast(1))
                    }
                }
            }
    }

    fun stopTicker() {
        ticker?.cancel()
        ticker = null
    }

    private suspend fun refreshDay() {
        history.catchUp()
        val today = history.today()
        if (selectedDate.value == lastToday) selectedDate.value = today
        lastToday = today
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    @Synchronized
    fun startUnlocked() {
        if (started || !context.getSystemService(UserManager::class.java).isUserUnlocked) return
        started = true
        scope.launch(Dispatchers.IO) {
            history.catchUp()
            selectedDate.value = history.today()
            merge(
                database.invalidationTracker.createFlow(*ALARM_TABLES),
                preferences.settings
                    .map { s ->
                        listOf(
                            s.goalType,
                            s.kcalTarget,
                            s.proteinTargetG,
                            s.weekdayGoalsEnabled,
                            s.dayStartHour,
                            s.wakeMold,
                            s.wakeBeforeFirstClassMin,
                            s.minGapBigMin,
                            s.minGapSnackMin,
                            s.maxFixedShiftMin,
                            s.latestMealMold,
                            s.workoutEnabled,
                            s.workoutDaysMask,
                            s.workoutTiming,
                            s.workoutAnchorSectionId,
                            s.workoutOffsetMin,
                            s.workoutStartMold,
                            s.workoutDurationMin,
                            s.mealAlarmsEnabled,
                            s.followUpMin,
                            s.missedNotification,
                            s.weighInEnabled,
                            s.weighInWeekday,
                            s.weighInMold,
                            s.weighInStyle,
                            s.defaultTaskReminderStyle,
                            s.stopAlarmsAfterTarget,
                            s.dailySummaryEnabled,
                            s.dailySummaryMold,
                            s.upcomingNoticeMin,
                            s.dayOverviewEnabled,
                            s.dayOverviewMold,
                            s.alarmDetailsOnLockScreen,
                            s.ringBeforeUnlock,
                            s.showCalories,
                            s.showProtein,
                            s.showNextMealCard,
                            s.themeMode,
                            s.dynamicColor,
                            s.accentKey,
                            s.timeFormat,
                        )
                    }.distinctUntilChanged(),
            ).debounce(500).collect { Log.d("Reschedule", "Phase 1 invalidation") }
        }
    }

    companion object {
        val ALARM_TABLES =
            arrayOf(
                "alarm_profiles",
                "weekday_goals",
                "sections",
                "meal_items",
                "foods",
                "day_overrides",
                "log_entries",
                "day_states",
                "semesters",
                "holidays",
                "courses",
                "class_sessions",
                "class_exceptions",
                "events",
                "event_exceptions",
                "tasks",
                "task_reminders",
                "weights",
            )
    }
}
