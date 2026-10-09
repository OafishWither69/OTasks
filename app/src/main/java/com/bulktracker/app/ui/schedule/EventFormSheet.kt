package com.bulktracker.app.ui.schedule

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bulktracker.app.data.db.EventEntity
import com.bulktracker.app.ui.components.ConfirmDialog
import com.bulktracker.app.ui.components.TimePickerDialog
import com.bulktracker.app.ui.theme.BulkTrackerTheme
import com.bulktracker.app.ui.theme.SansFamily
import com.bulktracker.app.ui.theme.Tokens
import com.bulktracker.app.util.DateUtils
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

private val eventDateFormat = DateTimeFormatter.ofPattern("EEE, d MMM yyyy", Locale.US)

private enum class RepeatOption {
    NONE,
    DAILY,
    WEEKLY,
    MONTHLY,
    CUSTOM
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EventFormSheet(
    event: EventEntity?, // null = create mode
    initialKind: String = "PERSONAL", // default for create mode: "PERSONAL" | "EXTRA_CLASS"
    initialDate: LocalDate, // logical today
    dayStartHour: Int = 4,
    onDismiss: () -> Unit,
    onSave: (EventEntity) -> Unit,
    onDelete: ((Long) -> Unit)? = null
) {
    val colors = BulkTrackerTheme.colors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val isEditMode = event != null

    var title by remember { mutableStateOf(event?.title ?: "") }
    var nameTouched by remember { mutableStateOf(false) }
    var kind by remember { mutableStateOf(event?.kind ?: initialKind) }
    var selectedDate by remember {
        mutableStateOf(
            if (event != null) LocalDate.ofEpochDay(event.startDay) else initialDate
        )
    }

    // Default start 5:00 PM (17:00 -> mold 1020), end 6:00 PM (18:00 -> mold 1080)
    var startMold by remember { mutableIntStateOf(event?.startMold ?: 1020) }
    var endMold by remember { mutableIntStateOf(event?.endMold ?: 1080) }

    // Repeat options
    val initialRepeatOption = remember(event) {
        if (event == null) {
            RepeatOption.NONE
        } else {
            when (event.repeatType) {
                "NONE" -> RepeatOption.NONE
                "DAILY" -> if (event.repeatInterval > 1) RepeatOption.CUSTOM else RepeatOption.DAILY
                "WEEKLY" -> if (event.repeatInterval > 1) RepeatOption.CUSTOM else RepeatOption.WEEKLY
                "MONTHLY" -> RepeatOption.MONTHLY
                else -> RepeatOption.NONE
            }
        }
    }
    var repeatOption by remember { mutableStateOf(initialRepeatOption) }
    var repeatExpanded by remember { mutableStateOf(false) }

    // Weekdays mask (bits 0..6 for Mon..Sun)
    val initialWeekdaysMask = remember(event, selectedDate) {
        if (event != null && event.weekdaysMask != 0) {
            event.weekdaysMask
        } else {
            1 shl (selectedDate.dayOfWeek.value - 1)
        }
    }
    var weekdaysMask by remember { mutableIntStateOf(initialWeekdaysMask) }

    // Custom repeat options
    var customIntervalStr by remember {
        mutableStateOf(
            if (event != null && event.repeatInterval > 1) event.repeatInterval.toString() else "2"
        )
    }
    var customUnit by remember {
        mutableStateOf(
            if (event?.repeatType == "WEEKLY") "weeks" else "days"
        )
    }

    // Ends options (NEVER, UNTIL, COUNT)
    var endType by remember { mutableStateOf(event?.endType ?: "NEVER") }
    var untilDate by remember {
        mutableStateOf(
            if (event?.untilDay != null) LocalDate.ofEpochDay(event.untilDay) else selectedDate.plusMonths(1)
        )
    }
    var countStr by remember { mutableStateOf(event?.count?.toString() ?: "10") }

    // Dialog pickers
    var showDatePicker by remember { mutableStateOf(false) }
    var showUntilDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    // Validation
    val isTitleValid = title.trim().isNotEmpty() && title.length <= 40
    val isTimeValid = endMold > startMold

    val isRepeatValid = when (repeatOption) {
        RepeatOption.NONE -> true
        RepeatOption.DAILY -> true
        RepeatOption.WEEKLY -> weekdaysMask != 0
        RepeatOption.MONTHLY -> true
        RepeatOption.CUSTOM -> {
            val intervalValid = customIntervalStr.toIntOrNull() in 1..99
            val maskValid = if (customUnit == "weeks") weekdaysMask != 0 else true
            intervalValid && maskValid
        }
    }

    val isEndsValid = if (repeatOption == RepeatOption.NONE) {
        true
    } else {
        when (endType) {
            "NEVER" -> true
            "UNTIL" -> !untilDate.isBefore(selectedDate)
            "COUNT" -> countStr.toIntOrNull() in 1..999
            else -> true
        }
    }

    val isFormValid = isTitleValid && isTimeValid && isRepeatValid && isEndsValid

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.card,
        modifier = Modifier
            .navigationBarsPadding()
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Tokens.SpaceLg, vertical = Tokens.SpaceMd)
        ) {
            Text(
                text = if (isEditMode) "Edit event" else "Add event",
                fontSize = Tokens.TitleLgSize,
                fontFamily = SansFamily,
                color = colors.textPrimary,
                modifier = Modifier.padding(bottom = Tokens.SpaceMd)
            )

            // Title
            OutlinedTextField(
                value = title,
                onValueChange = {
                    nameTouched = true
                    if (it.length <= 40) title = it
                },
                label = { Text("Title", fontFamily = SansFamily) },
                isError = nameTouched && title.trim().isEmpty(),
                supportingText = {
                    if (nameTouched && title.trim().isEmpty()) {
                        Text(
                            text = "Enter a name",
                            color = colors.danger,
                            fontSize = Tokens.CaptionSize,
                            fontFamily = SansFamily
                        )
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.ink,
                    unfocusedBorderColor = colors.border,
                    focusedLabelColor = colors.ink,
                    cursorColor = colors.ink
                )
            )

            Spacer(modifier = Modifier.height(Tokens.SpaceSm))

            // Type (SingleChoiceSegmentedButtonRow)
            Text(
                text = "Type",
                fontSize = Tokens.CaptionSize,
                fontFamily = SansFamily,
                color = colors.textSecondary,
                modifier = Modifier.padding(bottom = Tokens.SpaceXs)
            )

            val types = listOf("PERSONAL" to "Personal", "EXTRA_CLASS" to "Extra class")
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = Tokens.SpaceMd)
            ) {
                types.forEachIndexed { index, (typeKey, label) ->
                    SegmentedButton(
                        selected = kind == typeKey,
                        onClick = { kind = typeKey },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = types.size),
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

            // Date picker field
            Text(
                text = "Date",
                fontSize = Tokens.CaptionSize,
                fontFamily = SansFamily,
                color = colors.textSecondary,
                modifier = Modifier.padding(bottom = Tokens.SpaceXs)
            )

            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true },
                shape = RoundedCornerShape(Tokens.RadiusMd),
                border = androidx.compose.foundation.BorderStroke(Tokens.BorderThin, colors.border),
                colors = androidx.compose.material3.CardDefaults.outlinedCardColors(containerColor = colors.card)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Tokens.SpaceMd, vertical = Tokens.SpaceSm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedDate.format(eventDateFormat),
                        fontSize = Tokens.BodySize,
                        fontFamily = SansFamily,
                        color = colors.textPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(Tokens.SpaceMd))

            // Start & End Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Tokens.SpaceMd)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Start",
                        fontSize = Tokens.CaptionSize,
                        fontFamily = SansFamily,
                        color = colors.textSecondary,
                        modifier = Modifier.padding(bottom = Tokens.SpaceXs)
                    )
                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showStartTimePicker = true },
                        shape = RoundedCornerShape(Tokens.RadiusMd),
                        border = androidx.compose.foundation.BorderStroke(Tokens.BorderThin, colors.border),
                        colors = androidx.compose.material3.CardDefaults.outlinedCardColors(containerColor = colors.card)
                    ) {
                        Text(
                            text = DateUtils.formatMold(startMold),
                            fontSize = Tokens.BodySize,
                            fontFamily = SansFamily,
                            color = colors.textPrimary,
                            modifier = Modifier.padding(horizontal = Tokens.SpaceMd, vertical = Tokens.SpaceSm)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "End",
                        fontSize = Tokens.CaptionSize,
                        fontFamily = SansFamily,
                        color = colors.textSecondary,
                        modifier = Modifier.padding(bottom = Tokens.SpaceXs)
                    )
                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showEndTimePicker = true },
                        shape = RoundedCornerShape(Tokens.RadiusMd),
                        border = androidx.compose.foundation.BorderStroke(
                            Tokens.BorderThin,
                            if (endMold <= startMold) colors.danger else colors.border
                        ),
                        colors = androidx.compose.material3.CardDefaults.outlinedCardColors(containerColor = colors.card)
                    ) {
                        Text(
                            text = DateUtils.formatMold(endMold),
                            fontSize = Tokens.BodySize,
                            fontFamily = SansFamily,
                            color = colors.textPrimary,
                            modifier = Modifier.padding(horizontal = Tokens.SpaceMd, vertical = Tokens.SpaceSm)
                        )
                    }
                }
            }

            if (endMold <= startMold) {
                Text(
                    text = "End must be after start",
                    color = colors.danger,
                    fontSize = Tokens.CaptionSize,
                    fontFamily = SansFamily,
                    modifier = Modifier.padding(top = Tokens.SpaceXs)
                )
            }

            Spacer(modifier = Modifier.height(Tokens.SpaceMd))

            // Repeat ExposedDropdownMenuBox
            val dayOfMonth = selectedDate.dayOfMonth
            val monthlyOptionText = "Every month on day $dayOfMonth"

            val repeatOptionsList = listOf(
                RepeatOption.NONE to "Does not repeat",
                RepeatOption.DAILY to "Every day",
                RepeatOption.WEEKLY to "Every week on…",
                RepeatOption.MONTHLY to monthlyOptionText,
                RepeatOption.CUSTOM to "Custom…"
            )

            val currentRepeatLabel = when (repeatOption) {
                RepeatOption.NONE -> "Does not repeat"
                RepeatOption.DAILY -> "Every day"
                RepeatOption.WEEKLY -> "Every week on…"
                RepeatOption.MONTHLY -> monthlyOptionText
                RepeatOption.CUSTOM -> "Custom…"
            }

            ExposedDropdownMenuBox(
                expanded = repeatExpanded,
                onExpandedChange = { repeatExpanded = !repeatExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = currentRepeatLabel,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Repeat", fontFamily = SansFamily) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = repeatExpanded) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.ink,
                        unfocusedBorderColor = colors.border,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary,
                        focusedLabelColor = colors.ink
                    ),
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = repeatExpanded,
                    onDismissRequest = { repeatExpanded = false },
                    containerColor = colors.card
                ) {
                    repeatOptionsList.forEach { (opt, label) ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = label,
                                    fontSize = Tokens.BodySize,
                                    fontFamily = SansFamily,
                                    color = colors.textPrimary
                                )
                            },
                            onClick = {
                                repeatOption = opt
                                if (opt == RepeatOption.WEEKLY && weekdaysMask == 0) {
                                    weekdaysMask = 1 shl (selectedDate.dayOfWeek.value - 1)
                                }
                                if (opt == RepeatOption.CUSTOM && customUnit == "weeks" && weekdaysMask == 0) {
                                    weekdaysMask = 1 shl (selectedDate.dayOfWeek.value - 1)
                                }
                                repeatExpanded = false
                            }
                        )
                    }
                }
            }

            // "Every week on…" -> 7 weekday FilterChips Mon-Sun
            if (repeatOption == RepeatOption.WEEKLY) {
                WeekdayChipsSelector(
                    weekdaysMask = weekdaysMask,
                    onMaskChange = { weekdaysMask = it }
                )
            }

            // "Custom…" -> number field (1-99) + days/weeks segmented button
            if (repeatOption == RepeatOption.CUSTOM) {
                val customIntervalInt = customIntervalStr.toIntOrNull()
                val isCustomIntervalError = customIntervalInt == null || customIntervalInt !in 1..99

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Tokens.SpaceSm)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Tokens.SpaceMd),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = customIntervalStr,
                            onValueChange = { input ->
                                if (input.length <= 2 && input.all { it.isDigit() }) {
                                    customIntervalStr = input
                                }
                            },
                            label = { Text("Every", fontFamily = SansFamily) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            isError = isCustomIntervalError,
                            modifier = Modifier.width(100.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colors.ink,
                                unfocusedBorderColor = colors.border,
                                focusedTextColor = colors.textPrimary,
                                unfocusedTextColor = colors.textPrimary,
                                focusedLabelColor = colors.ink,
                                cursorColor = colors.ink,
                                errorBorderColor = colors.danger
                            )
                        )

                        val units = listOf("days" to "days", "weeks" to "weeks")
                        SingleChoiceSegmentedButtonRow(
                            modifier = Modifier.weight(1f)
                        ) {
                            units.forEachIndexed { index, (key, label) ->
                                SegmentedButton(
                                    selected = customUnit == key,
                                    onClick = {
                                        customUnit = key
                                        if (key == "weeks" && weekdaysMask == 0) {
                                            weekdaysMask = 1 shl (selectedDate.dayOfWeek.value - 1)
                                        }
                                    },
                                    shape = SegmentedButtonDefaults.itemShape(index = index, count = units.size),
                                    colors = SegmentedButtonDefaults.colors(
                                        activeContainerColor = colors.ink,
                                        activeContentColor = colors.onInk,
                                        inactiveContainerColor = colors.card,
                                        inactiveContentColor = colors.textPrimary,
                                        activeBorderColor = colors.border,
                                        inactiveBorderColor = colors.border
                                    )
                                ) {
                                    Text(label, fontSize = Tokens.BodySmSize, fontFamily = SansFamily)
                                }
                            }
                        }
                    }

                    if (isCustomIntervalError) {
                        Text(
                            text = "Enter 1 to 99",
                            color = colors.danger,
                            fontSize = Tokens.CaptionSize,
                            fontFamily = SansFamily,
                            modifier = Modifier.padding(top = Tokens.SpaceXs)
                        )
                    }

                    if (customUnit == "weeks") {
                        WeekdayChipsSelector(
                            weekdaysMask = weekdaysMask,
                            onMaskChange = { weekdaysMask = it }
                        )
                    }
                }
            }

            // Ends section (shown only when repeating)
            if (repeatOption != RepeatOption.NONE) {
                Text(
                    text = "Ends",
                    fontSize = Tokens.CaptionSize,
                    fontFamily = SansFamily,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(top = Tokens.SpaceMd, bottom = Tokens.SpaceXs)
                )

                // 1. Never
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .clickable { endType = "NEVER" },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = endType == "NEVER",
                        onClick = { endType = "NEVER" },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = colors.ink,
                            unselectedColor = colors.textTertiary
                        )
                    )
                    Spacer(modifier = Modifier.width(Tokens.SpaceSm))
                    Text(
                        text = "Never",
                        fontSize = Tokens.BodySize,
                        fontFamily = SansFamily,
                        color = colors.textPrimary
                    )
                }

                // 2. On [date]
                val isUntilDateInvalid = endType == "UNTIL" && untilDate.isBefore(selectedDate)

                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .clickable {
                                endType = "UNTIL"
                                showUntilDatePicker = true
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = endType == "UNTIL",
                            onClick = {
                                endType = "UNTIL"
                                showUntilDatePicker = true
                            },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = colors.ink,
                                unselectedColor = colors.textTertiary
                            )
                        )
                        Spacer(modifier = Modifier.width(Tokens.SpaceSm))
                        Text(
                            text = "On ${untilDate.format(eventDateFormat)}",
                            fontSize = Tokens.BodySize,
                            fontFamily = SansFamily,
                            color = colors.textPrimary
                        )
                    }
                    if (isUntilDateInvalid) {
                        Text(
                            text = "End date must be on or after the start date",
                            color = colors.danger,
                            fontSize = Tokens.CaptionSize,
                            fontFamily = SansFamily,
                            modifier = Modifier.padding(start = 40.dp, bottom = Tokens.SpaceXs)
                        )
                    }
                }

                // 3. After [N] times
                val countInt = countStr.toIntOrNull()
                val isCountInvalid = endType == "COUNT" && (countInt == null || countInt !in 1..999)

                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .clickable { endType = "COUNT" },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = endType == "COUNT",
                            onClick = { endType = "COUNT" },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = colors.ink,
                                unselectedColor = colors.textTertiary
                            )
                        )
                        Spacer(modifier = Modifier.width(Tokens.SpaceSm))
                        Text(
                            text = "After",
                            fontSize = Tokens.BodySize,
                            fontFamily = SansFamily,
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.width(Tokens.SpaceSm))
                        OutlinedTextField(
                            value = countStr,
                            onValueChange = {
                                if (it.length <= 3 && it.all { c -> c.isDigit() }) {
                                    countStr = it
                                    endType = "COUNT"
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            isError = isCountInvalid,
                            modifier = Modifier.width(96.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colors.ink,
                                unfocusedBorderColor = colors.border,
                                focusedTextColor = colors.textPrimary,
                                unfocusedTextColor = colors.textPrimary,
                                errorBorderColor = colors.danger
                            )
                        )
                        Spacer(modifier = Modifier.width(Tokens.SpaceSm))
                        Text(
                            text = "times",
                            fontSize = Tokens.BodySize,
                            fontFamily = SansFamily,
                            color = colors.textPrimary
                        )
                    }
                    if (isCountInvalid) {
                        Text(
                            text = "Enter 1 to 999",
                            color = colors.danger,
                            fontSize = Tokens.CaptionSize,
                            fontFamily = SansFamily,
                            modifier = Modifier.padding(start = 40.dp, top = Tokens.SpaceXs)
                        )
                    }
                }
            }

            // Live event summary preview line (13sp textSecondary)
            val liveSummary = remember(
                kind, title, selectedDate, startMold, endMold,
                repeatOption, customUnit, customIntervalStr, weekdaysMask,
                endType, untilDate, countStr
            ) {
                val repType = when (repeatOption) {
                    RepeatOption.NONE -> "NONE"
                    RepeatOption.DAILY -> "DAILY"
                    RepeatOption.WEEKLY -> "WEEKLY"
                    RepeatOption.MONTHLY -> "MONTHLY"
                    RepeatOption.CUSTOM -> if (customUnit == "days") "DAILY" else "WEEKLY"
                }
                val repInterval = when (repeatOption) {
                    RepeatOption.NONE -> 1
                    RepeatOption.DAILY -> 1
                    RepeatOption.WEEKLY -> 1
                    RepeatOption.MONTHLY -> 1
                    RepeatOption.CUSTOM -> customIntervalStr.toIntOrNull()?.coerceIn(1, 99) ?: 1
                }
                val mask = when (repeatOption) {
                    RepeatOption.WEEKLY -> weekdaysMask
                    RepeatOption.CUSTOM -> if (customUnit == "weeks") weekdaysMask else 0
                    else -> 0
                }
                val (uDay, cnt) = when {
                    repType == "NONE" -> Pair(null, null)
                    endType == "UNTIL" -> Pair(untilDate.toEpochDay(), null)
                    endType == "COUNT" -> Pair(null, countStr.toIntOrNull()?.coerceIn(1, 999))
                    else -> Pair(null, null)
                }
                val previewEntity = EventEntity(
                    id = event?.id ?: 0L,
                    kind = kind,
                    title = title.ifBlank { "Event" },
                    startDay = selectedDate.toEpochDay(),
                    startMold = startMold,
                    endMold = endMold,
                    repeatType = repType,
                    repeatInterval = repInterval,
                    weekdaysMask = mask,
                    endType = if (repType == "NONE") "NEVER" else endType,
                    untilDay = uDay,
                    count = cnt,
                    note = event?.note ?: ""
                )
                eventSummary(previewEntity)
            }

            Spacer(modifier = Modifier.height(Tokens.SpaceSm))
            Text(
                text = liveSummary,
                fontSize = 13.sp,
                fontFamily = SansFamily,
                color = colors.textSecondary,
                modifier = Modifier.padding(bottom = Tokens.SpaceMd)
            )

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isEditMode && onDelete != null) {
                    TextButton(
                        onClick = { showDeleteConfirm = true },
                        colors = ButtonDefaults.textButtonColors(contentColor = colors.danger)
                    ) {
                        Text("Delete", fontFamily = SansFamily)
                    }
                    Spacer(modifier = Modifier.weight(1f))
                }

                TextButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.textButtonColors(contentColor = colors.textSecondary)
                ) {
                    Text("Cancel", fontFamily = SansFamily)
                }

                Spacer(modifier = Modifier.width(Tokens.SpaceSm))

                Button(
                    onClick = {
                        if (isFormValid) {
                            val finalRepeatType = when (repeatOption) {
                                RepeatOption.NONE -> "NONE"
                                RepeatOption.DAILY -> "DAILY"
                                RepeatOption.WEEKLY -> "WEEKLY"
                                RepeatOption.MONTHLY -> "MONTHLY"
                                RepeatOption.CUSTOM -> if (customUnit == "days") "DAILY" else "WEEKLY"
                            }
                            val finalRepeatInterval = when (repeatOption) {
                                RepeatOption.NONE -> 1
                                RepeatOption.DAILY -> 1
                                RepeatOption.WEEKLY -> 1
                                RepeatOption.MONTHLY -> 1
                                RepeatOption.CUSTOM -> customIntervalStr.toIntOrNull() ?: 1
                            }
                            val finalWeekdaysMask = when (repeatOption) {
                                RepeatOption.WEEKLY -> weekdaysMask
                                RepeatOption.CUSTOM -> if (customUnit == "weeks") weekdaysMask else 0
                                else -> 0
                            }
                            val finalEndType = if (finalRepeatType == "NONE") "NEVER" else endType
                            val finalUntilDay = if (finalRepeatType != "NONE" && finalEndType == "UNTIL") {
                                untilDate.toEpochDay()
                            } else {
                                null
                            }
                            val finalCount = if (finalRepeatType != "NONE" && finalEndType == "COUNT") {
                                countStr.toIntOrNull()
                            } else {
                                null
                            }

                            val saved = EventEntity(
                                id = event?.id ?: 0L,
                                kind = kind,
                                title = title.trim(),
                                startDay = selectedDate.toEpochDay(),
                                startMold = startMold,
                                endMold = endMold,
                                repeatType = finalRepeatType,
                                repeatInterval = finalRepeatInterval,
                                weekdaysMask = finalWeekdaysMask,
                                endType = finalEndType,
                                untilDay = finalUntilDay,
                                count = finalCount,
                                note = event?.note ?: ""
                            )
                            onSave(saved)
                        }
                    },
                    enabled = isFormValid,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.ink,
                        contentColor = colors.onInk,
                        disabledContainerColor = colors.border,
                        disabledContentColor = colors.textTertiary
                    ),
                    shape = RoundedCornerShape(Tokens.RadiusMd)
                ) {
                    Text("Save", fontFamily = SansFamily)
                }
            }

            Spacer(modifier = Modifier.height(Tokens.SpaceMd))
        }
    }

    // Material 3 DatePicker with UTC rule (Event start date)
    if (showDatePicker) {
        val initialUtcMillis = remember(selectedDate) {
            selectedDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        }
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialUtcMillis
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            selectedDate = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK", color = colors.ink, fontFamily = SansFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = colors.textSecondary, fontFamily = SansFamily)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Material 3 DatePicker with UTC rule (Ends On date)
    if (showUntilDatePicker) {
        val initialUtcMillis = remember(untilDate) {
            untilDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        }
        val untilDatePickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialUtcMillis
        )

        DatePickerDialog(
            onDismissRequest = { showUntilDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        untilDatePickerState.selectedDateMillis?.let { millis ->
                            untilDate = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                        }
                        showUntilDatePicker = false
                    }
                ) {
                    Text("OK", color = colors.ink, fontFamily = SansFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { showUntilDatePicker = false }) {
                    Text("Cancel", color = colors.textSecondary, fontFamily = SansFamily)
                }
            }
        ) {
            DatePicker(state = untilDatePickerState)
        }
    }

    // Start TimePicker (AlertDialog with Cancel/OK)
    if (showStartTimePicker) {
        val h = (startMold % 1440) / 60
        val m = startMold % 60
        TimePickerDialog(
            initialHour = h,
            initialMinute = m,
            dayStartHour = dayStartHour,
            onConfirm = { mold ->
                startMold = mold
                showStartTimePicker = false
            },
            onDismiss = { showStartTimePicker = false }
        )
    }

    // End TimePicker
    if (showEndTimePicker) {
        val h = (endMold % 1440) / 60
        val m = endMold % 60
        TimePickerDialog(
            initialHour = h,
            initialMinute = m,
            dayStartHour = dayStartHour,
            onConfirm = { mold ->
                endMold = mold
                showEndTimePicker = false
            },
            onDismiss = { showEndTimePicker = false }
        )
    }

    // Delete Confirmation
    if (showDeleteConfirm && event != null && onDelete != null) {
        ConfirmDialog(
            title = "Delete event",
            message = "Delete ${event.title}?",
            confirmVerb = "Delete",
            onConfirm = {
                showDeleteConfirm = false
                onDelete(event.id)
            },
            onDismiss = { showDeleteConfirm = false }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WeekdayChipsSelector(
    weekdaysMask: Int,
    onMaskChange: (Int) -> Unit
) {
    val colors = BulkTrackerTheme.colors
    val weekdayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = Tokens.SpaceSm)
    ) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Tokens.SpaceSm),
            verticalArrangement = Arrangement.spacedBy(Tokens.SpaceXs)
        ) {
            weekdayLabels.forEachIndexed { index, label ->
                val bit = 1 shl index
                val isSelected = (weekdaysMask and bit) != 0
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        onMaskChange(weekdaysMask xor bit)
                    },
                    label = { Text(label, fontSize = Tokens.BodySmSize, fontFamily = SansFamily) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = colors.ink,
                        selectedLabelColor = colors.onInk,
                        containerColor = colors.card,
                        labelColor = colors.textPrimary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = colors.border,
                        selectedBorderColor = colors.border
                    )
                )
            }
        }
        if (weekdaysMask == 0) {
            Text(
                text = "Pick at least one day",
                color = colors.danger,
                fontSize = Tokens.CaptionSize,
                fontFamily = SansFamily,
                modifier = Modifier.padding(top = Tokens.SpaceXs)
            )
        }
    }
}
