package com.bulktracker.app.data.repo

import com.bulktracker.app.data.db.ClassCancellationEntity
import com.bulktracker.app.data.db.ClassEntity
import com.bulktracker.app.data.db.EventEntity
import com.bulktracker.app.data.db.EventExceptionEntity
import com.bulktracker.app.data.db.HolidayEntity
import com.bulktracker.app.data.db.ScheduleDao
import com.bulktracker.app.data.db.SemesterEntity
import com.bulktracker.app.domain.BusyBlock
import com.bulktracker.app.domain.occursOn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.time.LocalDate

class ScheduleRepository(private val scheduleDao: ScheduleDao) {

    fun observeSemester(): Flow<SemesterEntity?> = scheduleDao.observeSemester()

    fun observeHolidays(): Flow<List<HolidayEntity>> = scheduleDao.observeAllHolidays()

    fun observeClasses(): Flow<List<ClassEntity>> = scheduleDao.observeAllClasses()
 
    fun observeEvents(): Flow<List<EventEntity>> = scheduleDao.observeAllEvents()

    fun observeEventExceptions(): Flow<List<EventExceptionEntity>> = scheduleDao.observeAllEventExceptions()

    fun busyBlocksFor(day: LocalDate): Flow<List<BusyBlock>> {
        val epochDay = day.toEpochDay()
        val weekday = day.dayOfWeek.value

        val classesFlow = combine(
            scheduleDao.observeSemester(),
            scheduleDao.observeAllHolidays(),
            scheduleDao.observeAllClasses(),
            scheduleDao.observeCancellationsForDay(epochDay)
        ) { semester, holidays, classes, cancellations ->
            val inSemester = semester != null && epochDay >= semester.startDay && epochDay <= semester.endDay
            val isHoliday = holidays.any { epochDay >= it.startDay && epochDay <= it.endDay }

            if (!inSemester || isHoliday) {
                emptyList<BusyBlock>()
            } else {
                val cancelledIds = cancellations.map { it.classId }.toSet()
                classes
                    .filter { it.weekday == weekday && it.id !in cancelledIds }
                    .map { c ->
                        BusyBlock(
                            startMold = c.startMold,
                            endMold = c.endMold,
                            label = c.name,
                            kind = "CLASS"
                        )
                    }
            }
        }

        val eventsFlow = combine(
            scheduleDao.observeAllEvents(),
            scheduleDao.observeEventExceptionsForDay(epochDay)
        ) { events, exceptions ->
            events
                .filter { event -> occursOn(event, day, exceptions) }
                .map { event ->
                    val ex = exceptions.firstOrNull { it.eventId == event.id && it.epochDay == epochDay }
                    val start = if (ex?.type == "MOVED" && ex.newStartMold != null) ex.newStartMold else event.startMold
                    val end = if (ex?.type == "MOVED" && ex.newEndMold != null) ex.newEndMold else event.endMold
                    val kind = if (event.kind == "EXTRA_CLASS") "CLASS" else "EVENT"
                    BusyBlock(
                        startMold = start,
                        endMold = end,
                        label = event.title,
                        kind = kind
                    )
                }
        }

        return combine(classesFlow, eventsFlow) { classBlocks, eventBlocks ->
            (classBlocks + eventBlocks).sortedBy { it.startMold }
        }
    }

    suspend fun getBusyBlocks(day: LocalDate): List<BusyBlock> = withContext(Dispatchers.IO) {
        busyBlocksFor(day).first()
    }

    suspend fun saveSemester(semester: SemesterEntity) = withContext(Dispatchers.IO) {
        scheduleDao.upsertSemester(semester)
    }

    suspend fun saveSemester(startDay: Long, endDay: Long) = withContext(Dispatchers.IO) {
        scheduleDao.upsertSemester(SemesterEntity(id = 1, startDay = startDay, endDay = endDay))
    }

    suspend fun addHoliday(holiday: HolidayEntity): Long = withContext(Dispatchers.IO) {
        scheduleDao.insertHoliday(holiday)
    }

    suspend fun addHoliday(name: String, startDay: Long, endDay: Long): Long = withContext(Dispatchers.IO) {
        scheduleDao.insertHoliday(HolidayEntity(name = name, startDay = startDay, endDay = endDay))
    }

    suspend fun deleteHoliday(holiday: HolidayEntity) = withContext(Dispatchers.IO) {
        scheduleDao.deleteHoliday(holiday)
    }

    suspend fun deleteHoliday(holidayId: Long) = withContext(Dispatchers.IO) {
        scheduleDao.deleteHolidayById(holidayId)
    }

    suspend fun saveClass(classEntity: ClassEntity): Long = withContext(Dispatchers.IO) {
        scheduleDao.insertClass(classEntity)
    }

    suspend fun deleteClass(classEntity: ClassEntity) = withContext(Dispatchers.IO) {
        scheduleDao.deleteClass(classEntity)
    }

    suspend fun deleteClass(classId: Long) = withContext(Dispatchers.IO) {
        scheduleDao.deleteClassById(classId)
    }

    suspend fun cancelClassOn(classId: Long, day: LocalDate) = withContext(Dispatchers.IO) {
        scheduleDao.insertCancellation(
            ClassCancellationEntity(
                classId = classId,
                epochDay = day.toEpochDay()
            )
        )
    }

    suspend fun restoreClassOn(classId: Long, day: LocalDate) = withContext(Dispatchers.IO) {
        scheduleDao.deleteCancellation(classId, day.toEpochDay())
    }

    suspend fun saveEvent(event: EventEntity): Long = withContext(Dispatchers.IO) {
        if (event.id == 0L) {
            scheduleDao.insertEvent(event)
        } else {
            scheduleDao.updateEvent(event)
            event.id
        }
    }

    suspend fun deleteEvent(eventId: Long) = withContext(Dispatchers.IO) {
        scheduleDao.deleteEventAndExceptions(eventId)
    }

    suspend fun deleteEvent(event: EventEntity) = withContext(Dispatchers.IO) {
        scheduleDao.deleteEventAndExceptions(event.id)
    }

    suspend fun cancelEventOn(eventId: Long, day: LocalDate) = withContext(Dispatchers.IO) {
        val epochDay = day.toEpochDay()
        val existing = scheduleDao.getEventException(eventId, epochDay)
        val exception = EventExceptionEntity(
            id = existing?.id ?: 0,
            eventId = eventId,
            epochDay = epochDay,
            type = "CANCELLED",
            newStartMold = null,
            newEndMold = null
        )
        scheduleDao.upsertEventException(exception)
    }

    suspend fun moveEventOn(eventId: Long, day: LocalDate, newStart: Int, newEnd: Int) = withContext(Dispatchers.IO) {
        val epochDay = day.toEpochDay()
        val existing = scheduleDao.getEventException(eventId, epochDay)
        val exception = EventExceptionEntity(
            id = existing?.id ?: 0,
            eventId = eventId,
            epochDay = epochDay,
            type = "MOVED",
            newStartMold = newStart,
            newEndMold = newEnd
        )
        scheduleDao.upsertEventException(exception)
    }
}
