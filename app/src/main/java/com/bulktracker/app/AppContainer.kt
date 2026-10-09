package com.bulktracker.app

import android.content.Context
import androidx.room.Room
import com.bulktracker.app.alarm.AlarmScheduler
import com.bulktracker.app.data.db.AppDatabase
import com.bulktracker.app.data.repo.ScheduleRepository
import com.bulktracker.app.data.repo.TrackerRepository
import com.bulktracker.app.domain.DayPlanner
import com.bulktracker.app.domain.IdealTimePlanner
import com.bulktracker.app.util.DateUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime

@OptIn(FlowPreview::class)
class AppContainer(val context: Context) {
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val database: AppDatabase = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "bulk_tracker.db"
    ).build()

    val dayPlanner: DayPlanner = IdealTimePlanner()

    val scheduleRepository: ScheduleRepository = ScheduleRepository(database.scheduleDao())
    val trackerRepository: TrackerRepository = TrackerRepository(database.trackerDao(), dayPlanner, scheduleRepository)

    val selectedDate: MutableStateFlow<LocalDate> = MutableStateFlow(
        DateUtils.logicalToday(LocalDateTime.now(), 4)
    )

    init {
        // Trigger AlarmScheduler.rescheduleAll() on Dispatchers.IO when tracked database tables change
        applicationScope.launch(Dispatchers.IO) {
            database.invalidationTracker.createFlow(
                *TRACKED_ALARM_TABLES
            ).debounce(500).collect {
                AlarmScheduler.rescheduleAll(context)
            }
        }
    }

    fun updateSelectedDateIfToday(dayStartHour: Int) {
        val newToday = DateUtils.logicalToday(LocalDateTime.now(), dayStartHour)
        selectedDate.value = newToday
    }

    companion object {
        private val TRACKED_ALARM_TABLES = arrayOf(
            "sections",
            "food_items",
            "day_overrides",
            "log_entries",
            "daily_habits",
            "settings",
            "semester",
            "holidays",
            "classes",
            "class_cancellations",
            "events",
            "event_exceptions",
            "tasks",
            "task_reminders"
        )
    }
}
