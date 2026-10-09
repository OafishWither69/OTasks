package com.bulktracker.app.ui.schedule

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bulktracker.app.data.db.ClassEntity
import com.bulktracker.app.data.db.EventEntity
import com.bulktracker.app.data.db.HolidayEntity
import com.bulktracker.app.ui.components.ConfirmDialog
import com.bulktracker.app.ui.theme.BulkTrackerTheme
import com.bulktracker.app.ui.theme.SansFamily
import com.bulktracker.app.ui.theme.TitleLg
import com.bulktracker.app.ui.theme.TitleMd
import com.bulktracker.app.ui.theme.Tokens
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    viewModel: ScheduleViewModel,
    uiState: ScheduleUiState
) {
    val colors = BulkTrackerTheme.colors

    var editingClass by remember { mutableStateOf<ClassEntity?>(null) }
    var showClassSheet by remember { mutableStateOf(false) }

    var editingEvent by remember { mutableStateOf<EventEntity?>(null) }
    var showEventSheet by remember { mutableStateOf(false) }
    var eventInitialKind by remember { mutableStateOf("PERSONAL") }

    val onSelectSegment = remember(viewModel) { { index: Int -> viewModel.selectSegment(index) } }
    val onSwitchToSemester = remember(viewModel) { { viewModel.selectSegment(2) } }
    val onClassClick = remember { { classEntity: ClassEntity -> editingClass = classEntity; showClassSheet = true } }
    val onAddPersonal = remember { { editingEvent = null; eventInitialKind = "PERSONAL"; showEventSheet = true } }
    val onAddExtraClass = remember { { editingEvent = null; eventInitialKind = "EXTRA_CLASS"; showEventSheet = true } }
    val onEventClick = remember { { event: EventEntity -> editingEvent = event; eventInitialKind = event.kind; showEventSheet = true } }

    val onSaveSemester = remember(viewModel) { { start: LocalDate, end: LocalDate -> viewModel.saveSemester(start, end) } }
    val onAddHoliday = remember(viewModel) { { name: String, start: LocalDate, end: LocalDate -> viewModel.addHoliday(name, start, end) } }
    val onDeleteHoliday = remember(viewModel) { { holidayId: Long -> viewModel.deleteHoliday(holidayId) } }

    val onDismissClassSheet = remember { { showClassSheet = false; editingClass = null } }
    val onSaveClassAdd = remember(viewModel) {
        { name: String, weekdays: Set<Int>, startMold: Int, endMold: Int, location: String ->
            viewModel.saveClasses(name, weekdays, startMold, endMold, location)
        }
    }
    val onSaveClassEdit = remember(viewModel) {
        { id: Long, name: String, weekday: Int, startMold: Int, endMold: Int, location: String ->
            viewModel.updateClass(id, name, weekday, startMold, endMold, location)
        }
    }
    val onDeleteClass = remember(viewModel) { { id: Long -> viewModel.deleteClass(id) } }

    val onDismissEventSheet = remember { { showEventSheet = false; editingEvent = null } }
    val onSaveEvent = remember(viewModel) {
        { savedEvent: EventEntity ->
            viewModel.saveEvent(savedEvent)
            showEventSheet = false
            editingEvent = null
        }
    }
    val onDeleteEvent = remember(viewModel) {
        { id: Long ->
            viewModel.deleteEvent(id)
            showEventSheet = false
            editingEvent = null
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = colors.background,
        floatingActionButton = {
            if (uiState.selectedSegment == 0) {
                FloatingActionButton(
                    onClick = {
                        editingClass = null
                        showClassSheet = true
                    },
                    containerColor = colors.accent,
                    contentColor = colors.onAccentIcon,
                    shape = RoundedCornerShape(Tokens.RadiusSquircle),
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 0.dp,
                        pressedElevation = 0.dp,
                        focusedElevation = 0.dp,
                        hoveredElevation = 0.dp
                    ),
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add class",
                        tint = colors.onAccentIcon
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
        ) {
            // Plain 56dp Row title "Schedule" (titleLg)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = Tokens.SpaceLg),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Schedule",
                    style = TitleLg,
                    color = colors.textPrimary,
                    modifier = Modifier.semantics { heading() }
                )
            }

            // SingleChoiceSegmentedButtonRow with "Week", "Events", "Semester"
            val segments = listOf("Week", "Events", "Semester")
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Tokens.SpaceLg, vertical = Tokens.SpaceSm)
            ) {
                segments.forEachIndexed { index, label ->
                    SegmentedButton(
                        selected = uiState.selectedSegment == index,
                        onClick = { onSelectSegment(index) },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = segments.size),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = colors.ink,
                            activeContentColor = colors.onInk,
                            inactiveContainerColor = colors.card,
                            inactiveContentColor = colors.textPrimary,
                            activeBorderColor = colors.border,
                            inactiveBorderColor = colors.border
                        )
                    ) {
                        Text(
                            text = label,
                            fontSize = Tokens.BodySmSize,
                            fontFamily = SansFamily
                        )
                    }
                }
            }

            // Segment content
            when (uiState.selectedSegment) {
                0 -> WeekSegmentContent(
                    uiState = uiState,
                    onSwitchToSemester = onSwitchToSemester,
                    onClassClick = onClassClick
                )
                1 -> EventsSegmentContent(
                    uiState = uiState,
                    onAddPersonal = onAddPersonal,
                    onAddExtraClass = onAddExtraClass,
                    onEventClick = onEventClick
                )
                2 -> SemesterSegmentContent(
                    uiState = uiState,
                    onSaveSemester = onSaveSemester,
                    onAddHoliday = onAddHoliday,
                    onDeleteHoliday = onDeleteHoliday
                )
            }
        }
    }

    if (showClassSheet) {
        ClassFormSheet(
            classEntity = editingClass,
            onDismiss = onDismissClassSheet,
            onSaveAdd = onSaveClassAdd,
            onSaveEdit = onSaveClassEdit,
            onDelete = onDeleteClass
        )
    }

    if (showEventSheet) {
        EventFormSheet(
            event = editingEvent,
            initialKind = eventInitialKind,
            initialDate = uiState.logicalToday,
            dayStartHour = uiState.dayStartHour,
            onDismiss = onDismissEventSheet,
            onSave = onSaveEvent,
            onDelete = onDeleteEvent
        )
    }
}

@Composable
private fun WeekSegmentContent(
    uiState: ScheduleUiState,
    onSwitchToSemester: () -> Unit,
    onClassClick: (ClassEntity) -> Unit
) {
    val colors = BulkTrackerTheme.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 96.dp)
    ) {
        // If no semester is saved: card "Set your semester dates so this timetable repeats every week"
        if (uiState.semester == null) {
            Card(
                shape = RoundedCornerShape(Tokens.RadiusLg),
                colors = CardDefaults.cardColors(containerColor = colors.card),
                border = BorderStroke(Tokens.BorderThin, colors.border),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Tokens.SpaceLg, vertical = Tokens.SpaceSm)
            ) {
                Column(modifier = Modifier.padding(Tokens.SpaceLg)) {
                    Text(
                        text = "Set your semester dates so this timetable repeats every week",
                        fontSize = Tokens.BodySize,
                        lineHeight = Tokens.BodyLineHeight,
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(Tokens.SpaceMd))
                    Button(
                        onClick = onSwitchToSemester,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.ink,
                            contentColor = colors.onInk
                        ),
                        shape = RoundedCornerShape(Tokens.RadiusMd)
                    ) {
                        Text(
                            text = "Set semester",
                            fontSize = Tokens.BodySmSize,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = SansFamily
                        )
                    }
                }
            }
        }

        // Empty state when there are no classes
        if (uiState.classes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Tokens.SpaceLg, vertical = Tokens.SpaceLg),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No classes yet. Tap + to add your timetable.",
                    fontSize = Tokens.BodySize,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(Tokens.SpaceXs))

        // Week Grid
        WeekGrid(
            classes = uiState.classes,
            onClassClick = onClassClick,
            modifier = Modifier.padding(horizontal = Tokens.SpaceMd)
        )
    }
}

@Composable
private fun EventsSegmentContent(
    uiState: ScheduleUiState,
    onAddPersonal: () -> Unit,
    onAddExtraClass: () -> Unit,
    onEventClick: (EventEntity) -> Unit
) {
    val colors = BulkTrackerTheme.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Top button row: "+ Personal event" and "+ Extra class" (Secondary buttons, 48dp height)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Tokens.SpaceLg, vertical = Tokens.SpaceSm),
            horizontalArrangement = Arrangement.spacedBy(Tokens.SpaceSm)
        ) {
            OutlinedButton(
                onClick = onAddPersonal,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(Tokens.RadiusMd),
                border = BorderStroke(Tokens.BorderThin, colors.border),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = colors.textPrimary
                )
            ) {
                Text(
                    text = "+ Personal event",
                    fontSize = Tokens.BodySmSize,
                    fontFamily = SansFamily
                )
            }

            OutlinedButton(
                onClick = onAddExtraClass,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(Tokens.RadiusMd),
                border = BorderStroke(Tokens.BorderThin, colors.border),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = colors.textPrimary
                )
            ) {
                Text(
                    text = "+ Extra class",
                    fontSize = Tokens.BodySmSize,
                    fontFamily = SansFamily
                )
            }
        }

        // List of events sorted by next occurrence from logical today
        if (uiState.sortedEvents.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 96.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No events yet.",
                    fontSize = Tokens.CaptionSize,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = Tokens.SpaceLg,
                    end = Tokens.SpaceLg,
                    top = Tokens.SpaceXs,
                    bottom = 96.dp
                ),
                verticalArrangement = Arrangement.spacedBy(Tokens.SpaceSm)
            ) {
                items(
                    items = uiState.sortedEvents,
                    key = { item -> "e-${item.event.id}" },
                    contentType = { "event" }
                ) { item ->
                    EventCard(
                        item = item,
                        onClick = onEventClick
                    )
                }
            }
        }
    }
}

@Composable
private fun EventCard(
    item: EventWithNextOccurrence,
    onClick: (EventEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = BulkTrackerTheme.colors
    val event = item.event
    val onClickRemembered = remember(event, onClick) { { onClick(event) } }
    Card(
        shape = RoundedCornerShape(Tokens.RadiusMd),
        colors = CardDefaults.cardColors(containerColor = colors.card),
        border = BorderStroke(Tokens.BorderThin, colors.border),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClickRemembered)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .padding(horizontal = Tokens.SpaceMd, vertical = Tokens.SpaceSm),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = event.title,
                fontSize = 16.sp,
                fontFamily = SansFamily,
                fontWeight = FontWeight.Medium,
                color = colors.textPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = item.summary,
                fontSize = 13.sp,
                fontFamily = SansFamily,
                color = colors.textSecondary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun SemesterSegmentContent(
    uiState: ScheduleUiState,
    onSaveSemester: (start: LocalDate, end: LocalDate) -> Unit,
    onAddHoliday: (name: String, start: LocalDate, end: LocalDate) -> Unit,
    onDeleteHoliday: (holidayId: Long) -> Unit
) {
    val colors = BulkTrackerTheme.colors

    var startDate by remember(uiState.semester) {
        mutableStateOf(
            uiState.semester?.let { LocalDate.ofEpochDay(it.startDay) } ?: LocalDate.now()
        )
    }
    var endDate by remember(uiState.semester) {
        mutableStateOf(
            uiState.semester?.let { LocalDate.ofEpochDay(it.endDay) } ?: LocalDate.now().plusMonths(4)
        )
    }

    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }
    var showSemesterConfirm by remember { mutableStateOf(false) }

    var showAddHolidayDialog by remember { mutableStateOf(false) }
    var deletingHoliday by remember { mutableStateOf<HolidayEntity?>(null) }

    val isDateValid = !endDate.isBefore(startDate)
    val dateFormatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.US)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = Tokens.SpaceLg, end = Tokens.SpaceLg, bottom = 96.dp)
    ) {
        item(
            key = "semester_dates_header",
            contentType = "header"
        ) {
            Spacer(modifier = Modifier.height(Tokens.SpaceSm))
            Text(
                text = "Semester dates",
                style = TitleMd,
                color = colors.textPrimary,
                modifier = Modifier.semantics { heading() }
            )

            Spacer(modifier = Modifier.height(Tokens.SpaceMd))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Tokens.SpaceMd)
            ) {
                // Start date picker card
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Start date",
                        fontSize = Tokens.CaptionSize,
                        color = colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(Tokens.SpaceXs))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(Tokens.RadiusMd))
                            .background(colors.card)
                            .border(Tokens.BorderThin, colors.border, RoundedCornerShape(Tokens.RadiusMd))
                            .clickable { showStartPicker = true }
                            .padding(Tokens.SpaceMd),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = startDate.format(dateFormatter),
                            fontSize = Tokens.BodySmSize,
                            color = colors.textPrimary
                        )
                    }
                }

                // End date picker card
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "End date",
                        fontSize = Tokens.CaptionSize,
                        color = colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(Tokens.SpaceXs))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(Tokens.RadiusMd))
                            .background(colors.card)
                            .border(
                                Tokens.BorderThin,
                                if (!isDateValid) colors.danger else colors.border,
                                RoundedCornerShape(Tokens.RadiusMd)
                            )
                            .clickable { showEndPicker = true }
                            .padding(Tokens.SpaceMd),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = endDate.format(dateFormatter),
                            fontSize = Tokens.BodySmSize,
                            color = colors.textPrimary
                        )
                    }
                }
            }

            if (!isDateValid) {
                Spacer(modifier = Modifier.height(Tokens.SpaceXs))
                Text(
                    text = "End date must be on or after start date",
                    color = colors.danger,
                    fontSize = Tokens.CaptionSize
                )
            }
        }

        item(
            key = "semester_save_button",
            contentType = "button"
        ) {
            Spacer(modifier = Modifier.height(Tokens.SpaceMd))

            Button(
                onClick = { showSemesterConfirm = true },
                enabled = isDateValid,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.ink,
                    contentColor = colors.onInk,
                    disabledContainerColor = colors.card,
                    disabledContentColor = colors.textTertiary
                ),
                shape = RoundedCornerShape(Tokens.RadiusMd),
                modifier = Modifier.height(Tokens.TouchMin)
            ) {
                Text(
                    text = "Save",
                    fontSize = Tokens.BodySize,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = SansFamily
                )
            }
        }

        item(
            key = "holidays_header",
            contentType = "header"
        ) {
            Spacer(modifier = Modifier.height(Tokens.SpaceXl))

            // Holidays section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Holidays and breaks",
                    style = TitleMd,
                    color = colors.textPrimary,
                    modifier = Modifier.semantics { heading() }
                )

                OutlinedButton(
                    onClick = { showAddHolidayDialog = true },
                    modifier = Modifier.height(Tokens.TouchMin),
                    shape = RoundedCornerShape(Tokens.RadiusMd),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = colors.textPrimary
                    ),
                    border = BorderStroke(Tokens.BorderThin, colors.border)
                ) {
                    Text(
                        text = "+ Add holiday",
                        fontSize = Tokens.BodySmSize,
                        fontFamily = SansFamily
                    )
                }
            }

            Spacer(modifier = Modifier.height(Tokens.SpaceMd))

            if (uiState.holidays.isEmpty()) {
                Text(
                    text = "No holidays added.",
                    fontSize = Tokens.BodySmSize,
                    color = colors.textSecondary
                )
            }
        }

        items(
            items = uiState.holidays,
            key = { holiday -> "h-${holiday.id}" },
            contentType = { "holiday" }
        ) { holiday ->
            HolidayCard(
                holiday = holiday,
                onDelete = { deletingHoliday = it }
            )
        }
    }

    if (showStartPicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = startDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showStartPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            startDate = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                            if (endDate.isBefore(startDate)) {
                                endDate = startDate
                            }
                        }
                        showStartPicker = false
                    }
                ) {
                    Text("OK", color = colors.textPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartPicker = false }) {
                    Text("Cancel", color = colors.textPrimary)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showEndPicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = endDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showEndPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            endDate = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                        }
                        showEndPicker = false
                    }
                ) {
                    Text("OK", color = colors.textPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndPicker = false }) {
                    Text("Cancel", color = colors.textPrimary)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showSemesterConfirm) {
        val startFmt = startDate.format(DateTimeFormatter.ofPattern("d MMM", Locale.US))
        val endFmt = if (startDate.year == endDate.year) {
            endDate.format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.US))
        } else {
            endDate.format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.US))
        }
        ConfirmDialog(
            title = "Save semester",
            message = "Save semester from $startFmt to $endFmt? Your timetable repeats between these dates.",
            confirmVerb = "Save",
            onConfirm = {
                onSaveSemester(startDate, endDate)
                showSemesterConfirm = false
            },
            onDismiss = { showSemesterConfirm = false }
        )
    }

    if (showAddHolidayDialog) {
        AddHolidayDialog(
            initialStartDate = startDate,
            initialEndDate = startDate,
            onDismiss = { showAddHolidayDialog = false },
            onConfirm = { name, start, end ->
                onAddHoliday(name, start, end)
                showAddHolidayDialog = false
            }
        )
    }

    deletingHoliday?.let { holiday ->
        ConfirmDialog(
            title = "Delete holiday",
            message = "Delete ${holiday.name}?",
            confirmVerb = "Delete",
            onConfirm = {
                onDeleteHoliday(holiday.id)
                deletingHoliday = null
            },
            onDismiss = { deletingHoliday = null }
        )
    }
}

private fun formatHolidayRange(start: LocalDate, end: LocalDate): String {
    val monthFmt = DateTimeFormatter.ofPattern("MMM", Locale.US)
    val yearFmt = DateTimeFormatter.ofPattern("MMM yyyy", Locale.US)
    return when {
        start == end -> "${start.dayOfMonth} ${start.format(monthFmt)}"
        start.month == end.month && start.year == end.year -> "${start.dayOfMonth}-${end.dayOfMonth} ${start.format(monthFmt)}"
        start.year == end.year -> "${start.dayOfMonth} ${start.format(monthFmt)} - ${end.dayOfMonth} ${end.format(monthFmt)}"
        else -> "${start.dayOfMonth} ${start.format(yearFmt)} - ${end.dayOfMonth} ${end.format(yearFmt)}"
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HolidayCard(
    holiday: HolidayEntity,
    onDelete: (HolidayEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = BulkTrackerTheme.colors
    val hStart = remember(holiday.startDay) { LocalDate.ofEpochDay(holiday.startDay) }
    val hEnd = remember(holiday.endDay) { LocalDate.ofEpochDay(holiday.endDay) }
    val rangeLabel = remember(hStart, hEnd) { formatHolidayRange(hStart, hEnd) }
    var showMenu by remember { mutableStateOf(false) }
    val onDeleteClick = remember(holiday, onDelete) { { onDelete(holiday) } }

    Box(modifier = modifier.fillMaxWidth().padding(vertical = Tokens.SpaceXs)) {
        Card(
            shape = RoundedCornerShape(Tokens.RadiusMd),
            colors = CardDefaults.cardColors(containerColor = colors.card),
            border = BorderStroke(Tokens.BorderThin, colors.border),
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = {},
                    onLongClick = { showMenu = true }
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Tokens.SpaceMd, vertical = Tokens.SpaceSm),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = holiday.name,
                        fontSize = Tokens.BodySize,
                        fontWeight = FontWeight.Medium,
                        color = colors.textPrimary
                    )
                    Text(
                        text = rangeLabel,
                        fontSize = Tokens.CaptionSize,
                        color = colors.textSecondary
                    )
                }

                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(Tokens.TouchMin)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Delete holiday",
                        tint = colors.danger
                    )
                }
            }
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text("Delete", color = colors.danger) },
                onClick = {
                    showMenu = false
                    onDelete(holiday)
                }
            )
        }
    }
}

