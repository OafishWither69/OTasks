package com.bulktracker.app.ui.schedule

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.bulktracker.app.AppContainer
import com.bulktracker.app.data.db.ClassEntity
import com.bulktracker.app.data.db.EventEntity
import com.bulktracker.app.data.db.EventExceptionEntity
import com.bulktracker.app.data.db.HolidayEntity
import com.bulktracker.app.data.db.SemesterEntity
import com.bulktracker.app.domain.occursOn
import com.bulktracker.app.util.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime

@Immutable
data class EventWithNextOccurrence(
    val event: EventEntity,
    val nextOccurrence: LocalDate?,
    val summary: String
)

private data class ScheduleWeekData(
    val semester: SemesterEntity?,
    val holidays: List<HolidayEntity>,
    val classes: List<ClassEntity>,
    val segment: Int
)

@Immutable
data class ScheduleUiState(
    val semester: SemesterEntity? = null,
    val holidays: List<HolidayEntity> = emptyList(),
    val classes: List<ClassEntity> = emptyList(),
    val events: List<EventEntity> = emptyList(),
    val sortedEvents: List<EventWithNextOccurrence> = emptyList(),
    val dayStartHour: Int = 4,
    val logicalToday: LocalDate = LocalDate.now(),
    val selectedSegment: Int = 0 // 0: Week, 1: Events, 2: Semester
)

class ScheduleViewModel(
    private val appContainer: AppContainer
) : ViewModel() {
    private val scheduleRepo = appContainer.scheduleRepository
    private val trackerRepo = appContainer.trackerRepository

    private val selectedSegmentFlow = MutableStateFlow(0)

    val uiState: StateFlow<ScheduleUiState> = combine(
        combine(
            scheduleRepo.observeSemester(),
            scheduleRepo.observeHolidays(),
            scheduleRepo.observeClasses(),
            selectedSegmentFlow
        ) { semester, holidays, classes, segment ->
            ScheduleWeekData(semester, holidays, classes, segment)
        },
        combine(
            scheduleRepo.observeEvents(),
            scheduleRepo.observeEventExceptions(),
            trackerRepo.observeSettings()
        ) { events, exceptions, settings ->
            Triple(events, exceptions, settings)
        }
    ) { (semester, holidays, classes, segment), (events, exceptions, settings) ->
        val dayStart = settings.dayStartHour
        val today = DateUtils.logicalToday(LocalDateTime.now(), dayStart)

        // Sort events by next occurrence from logical today up to 400 days and compute eventSummary
        val sortedList = events.map { event ->
            var nextOccur: LocalDate? = null
            var cur = today
            for (i in 0..400) {
                if (occursOn(event, cur, exceptions)) {
                    nextOccur = cur
                    break
                }
                cur = cur.plusDays(1)
            }
            EventWithNextOccurrence(
                event = event,
                nextOccurrence = nextOccur,
                summary = eventSummary(event)
            )
        }.sortedWith(
            compareBy<EventWithNextOccurrence> {
                it.nextOccurrence?.toEpochDay() ?: Long.MAX_VALUE
            }.thenBy {
                it.event.startMold
            }.thenBy {
                it.event.title
            }
        )

        ScheduleUiState(
            semester = semester,
            holidays = holidays,
            classes = classes,
            events = events,
            sortedEvents = sortedList,
            dayStartHour = dayStart,
            logicalToday = today,
            selectedSegment = segment
        )
    }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ScheduleUiState()
        )

    fun selectSegment(segment: Int) {
        selectedSegmentFlow.value = segment
    }

    fun saveSemester(startDay: LocalDate, endDay: LocalDate) {
        viewModelScope.launch {
            scheduleRepo.saveSemester(startDay.toEpochDay(), endDay.toEpochDay())
        }
    }

    fun addHoliday(name: String, startDay: LocalDate, endDay: LocalDate) {
        viewModelScope.launch {
            scheduleRepo.addHoliday(name.trim(), startDay.toEpochDay(), endDay.toEpochDay())
        }
    }

    fun deleteHoliday(holidayId: Long) {
        viewModelScope.launch {
            scheduleRepo.deleteHoliday(holidayId)
        }
    }

    fun saveClasses(name: String, weekdays: Set<Int>, startMold: Int, endMold: Int, location: String) {
        viewModelScope.launch {
            for (w in weekdays) {
                scheduleRepo.saveClass(
                    ClassEntity(
                        name = name.trim(),
                        weekday = w,
                        startMold = startMold,
                        endMold = endMold,
                        location = location.trim()
                    )
                )
            }
        }
    }

    fun updateClass(classId: Long, name: String, weekday: Int, startMold: Int, endMold: Int, location: String) {
        viewModelScope.launch {
            scheduleRepo.saveClass(
                ClassEntity(
                    id = classId,
                    name = name.trim(),
                    weekday = weekday,
                    startMold = startMold,
                    endMold = endMold,
                    location = location.trim()
                )
            )
        }
    }

    fun deleteClass(classId: Long) {
        viewModelScope.launch {
            scheduleRepo.deleteClass(classId)
        }
    }

    fun saveEvent(event: EventEntity) {
        viewModelScope.launch {
            scheduleRepo.saveEvent(event)
        }
    }

    fun deleteEvent(eventId: Long) {
        viewModelScope.launch {
            scheduleRepo.deleteEvent(eventId)
        }
    }

    companion object {
        fun provideFactory(appContainer: AppContainer): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ScheduleViewModel(appContainer) as T
                }
            }
    }
}
