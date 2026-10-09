package com.bulktracker.app.ui.today

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.ViewTimeline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import com.bulktracker.app.ui.theme.SerifFamily
import com.bulktracker.app.ui.theme.TitleLg
import com.bulktracker.app.ui.theme.TitleMd
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bulktracker.app.data.db.FoodItemEntity
import com.bulktracker.app.data.db.LogEntryEntity
import com.bulktracker.app.data.db.SectionEntity
import com.bulktracker.app.data.repo.ScopeSelection
import com.bulktracker.app.domain.ResolvedItem
import com.bulktracker.app.domain.ResolvedSection
import com.bulktracker.app.ui.components.AddMealSheet
import com.bulktracker.app.ui.components.AddSheet
import com.bulktracker.app.ui.components.ConfirmDialog
import com.bulktracker.app.ui.components.EditItemSheet
import com.bulktracker.app.ui.components.EditMealSheet
import com.bulktracker.app.ui.components.ItemSheet
import com.bulktracker.app.ui.components.MealCheckbox
import com.bulktracker.app.ui.components.ProgressRow
import com.bulktracker.app.ui.components.ScopeSheet
import com.bulktracker.app.ui.components.SectionDot
import com.bulktracker.app.ui.components.StatusMarker
import com.bulktracker.app.ui.components.TimePickerDialog
import com.bulktracker.app.ui.theme.BulkTrackerTheme
import com.bulktracker.app.ui.theme.Motion
import com.bulktracker.app.ui.theme.Tokens
import com.bulktracker.app.util.DateUtils
import com.bulktracker.app.util.Format
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private data class PendingItemAdd(
    val sectionId: Long,
    val name: String,
    val portion: String,
    val kcal: Int?,
    val protein: Double?
)

private data class PendingMealAdd(
    val name: String,
    val idealMold: Int,
    val size: String,
    val colorHex: String
)

private data class ItemSheetState(
    val item: FoodItemEntity?,
    val itemId: Long?,
    val customLogEntryId: Long?,
    val title: String,
    val initialMultiplier: Double,
    val initialKcal: Int?,
    val initialProtein: Double?,
    val isCustom: Boolean
)

private data class ScopeSheetState(
    val targetName: String,
    val isRemoveMode: Boolean,
    val onScopeConfirmed: (ScopeSelection, String) -> Unit
)

private data class ConfirmDialogState(
    val title: String,
    val message: String,
    val confirmVerb: String,
    val onConfirm: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    viewModel: TodayViewModel,
    uiState: TodayUiState,
    snackbarHostState: SnackbarHostState
) {
    val colors = BulkTrackerTheme.colors
    val context = androidx.compose.ui.platform.LocalContext.current
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var showMenu by remember { mutableStateOf(false) }

    // Dialog & Sheet States
    var showAddSheet by remember { mutableStateOf(false) }
    var addSheetPreselectedSectionId by remember { mutableStateOf<Long?>(null) }

    var showAddMealSheet by remember { mutableStateOf(false) }

    var itemSheetState by remember { mutableStateOf<ItemSheetState?>(null) }
    var editItemState by remember { mutableStateOf<FoodItemEntity?>(null) }
    var editingSection by remember { mutableStateOf<SectionEntity?>(null) }
    var scopeSheetState by remember { mutableStateOf<ScopeSheetState?>(null) }
    var confirmDialogState by remember { mutableStateOf<ConfirmDialogState?>(null) }

    var sectionToPin by remember { mutableStateOf<ResolvedSection?>(null) }

    // Track last tap time per item for 300ms debounce (R15)
    var lastTapItemId by remember { mutableStateOf<Long?>(null) }
    var lastTapTimeMillis by remember { mutableLongStateOf(0L) }

    // State for collapsed cards
    val collapsedSections = remember { mutableStateMapOf<Long, Boolean>() }
    val autoCollapsedHistory = remember { mutableStateMapOf<Long, Boolean>() }

    val resolvedDay = uiState.resolvedDay

    // Auto collapse when all non-optional items become checked
    LaunchedEffect(resolvedDay?.sections) {
        resolvedDay?.sections?.forEach { section ->
            if (section.isAllDone && autoCollapsedHistory[section.section.id] != true) {
                collapsedSections[section.section.id] = true
                autoCollapsedHistory[section.section.id] = true
            }
        }
    }

    val dayFormatShort = DateTimeFormatter.ofPattern("EEE, d MMM", Locale.US)

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = uiState.titleText,
                            style = TitleLg,
                            color = colors.textPrimary,
                            modifier = Modifier.semantics { heading() }
                        )
                        if (resolvedDay?.habit?.bareMinimum == true) {
                            Text(
                                text = "Bare-minimum day — priority items only",
                                fontSize = Tokens.CaptionSize,
                                lineHeight = Tokens.CaptionLineHeight,
                                color = colors.textSecondary
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { /* Phase 5: Timeline toggle */ },
                        modifier = Modifier.size(Tokens.TouchMin)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ViewTimeline,
                            contentDescription = "Show timeline",
                            tint = colors.textPrimary
                        )
                    }

                    if (uiState.selectedDate != uiState.logicalToday) {
                        TextButton(
                            onClick = { viewModel.selectToday() },
                            modifier = Modifier.height(Tokens.TouchMin)
                        ) {
                            Text(
                                text = "Today",
                                fontSize = Tokens.BodySmSize,
                                color = colors.textPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.size(Tokens.TouchMin)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.MoreVert,
                                contentDescription = "More options",
                                tint = colors.textPrimary
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            val isBareMin = resolvedDay?.habit?.bareMinimum == true
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = if (isBareMin) "✓ Bare-minimum day" else "Bare-minimum day",
                                        color = colors.textPrimary
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    viewModel.toggleBareMinimum()
                                }
                            )

                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = if (uiState.isRestDay) "✓ Rest day (no gym)" else "Rest day (no gym)",
                                        color = colors.textPrimary
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    viewModel.toggleGymRestDay()
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.background
                ),
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    addSheetPreselectedSectionId = uiState.nearestSectionId
                    showAddSheet = true
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
                    contentDescription = "Add",
                    tint = colors.onAccentIcon,
                    modifier = Modifier.size(Tokens.IconMd)
                )
            }
        },
        containerColor = colors.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Week Strip
            WeekStrip(
                selectedDate = uiState.selectedDate,
                logicalToday = uiState.logicalToday,
                onDateSelected = { viewModel.onDateSelected(it) }
            )

            Spacer(modifier = Modifier.height(Tokens.SpaceSm))

            if (resolvedDay == null) {
                Box(modifier = Modifier.fillMaxSize())
            } else {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(
                        start = Tokens.SpaceLg,
                        end = Tokens.SpaceLg,
                        top = Tokens.SpaceSm,
                        bottom = 96.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(Tokens.SpaceMd),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Permissions warning card (D3 line 730)
                    val permissionsState = com.bulktracker.app.ui.components.checkPermissionsState(context)
                    if (resolvedDay.settings.alarmsEnabled && permissionsState.hasProblem) {
                        item(
                            key = "permissions_warning_card",
                            contentType = "warning-0"
                        ) {
                            com.bulktracker.app.ui.components.PermissionsCard()
                        }
                    }

                    // Next Meal Card (Block C 5b)
                    if (uiState.hasNextMealCard && uiState.nextMealCard != null) {
                        item(
                            key = "next_meal_card",
                            contentType = "next-card"
                        ) {
                            NextMealCard(
                                model = uiState.nextMealCard,
                                onMarkAllEaten = { secId ->
                                    viewModel.markAllEaten(secId)
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = "Marked all eaten",
                                            actionLabel = "Undo",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                },
                                onScrollToShow = { secId ->
                                    val idx = uiState.sections.indexOfFirst { it.section.id == secId }
                                    if (idx != -1) {
                                        coroutineScope.launch {
                                            listState.animateScrollToItem(idx + 2)
                                        }
                                    }
                                }
                            )
                        }
                    }

                    // Summary Card (Block C item 6)
                    uiState.summaryCard?.let { summaryModel ->
                        item(
                            key = "summary_card",
                            contentType = "summary"
                        ) {
                            SummaryCard(model = summaryModel)
                        }
                    }

                    // Section Cards
                    items(
                        items = uiState.sections,
                        key = { sec -> "section-${sec.section.id}" },
                        contentType = { "section" }
                    ) { resolvedSection ->
                        val isCollapsed = collapsedSections[resolvedSection.section.id] == true

                        SectionCard(
                            model = resolvedSection,
                            isCollapsed = isCollapsed,
                            onToggleCollapse = {
                                collapsedSections[resolvedSection.section.id] = !isCollapsed
                            },
                            onItemToggle = { itemId ->
                                val now = System.currentTimeMillis()
                                if (lastTapItemId == itemId && (now - lastTapTimeMillis) < 300) {
                                    return@SectionCard
                                }
                                lastTapItemId = itemId
                                lastTapTimeMillis = now

                                val resolvedItem = resolvedDay.sections.flatMap { it.items }.firstOrNull { it.item.id == itemId }
                                val wasChecked = resolvedItem?.isChecked == true

                                if (!wasChecked) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }

                                viewModel.toggleItem(itemId)

                                if (wasChecked && resolvedItem != null) {
                                    coroutineScope.launch {
                                        val result = snackbarHostState.showSnackbar(
                                            message = "Unchecked ${resolvedItem.item.name}",
                                            actionLabel = "Undo",
                                            duration = SnackbarDuration.Short
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            viewModel.toggleItem(itemId)
                                        }
                                    }
                                }
                            },
                            onItemLongPress = { itemModel ->
                                val log = resolvedDay.logEntries.firstOrNull { it.foodItemId == itemModel.item.id }
                                itemSheetState = ItemSheetState(
                                    item = itemModel.item,
                                    itemId = itemModel.item.id,
                                    customLogEntryId = null,
                                    title = itemModel.item.name,
                                    initialMultiplier = log?.multiplier ?: 1.0,
                                    initialKcal = log?.kcalSnapshot ?: itemModel.item.kcal,
                                    initialProtein = log?.proteinSnapshot ?: itemModel.item.proteinG,
                                    isCustom = false
                                )
                            },
                            onCustomEntryLongPress = { customEntryModel ->
                                val entry = customEntryModel.entry
                                itemSheetState = ItemSheetState(
                                    item = null,
                                    itemId = null,
                                    customLogEntryId = entry.id,
                                    title = entry.nameSnapshot,
                                    initialMultiplier = entry.multiplier,
                                    initialKcal = entry.kcalSnapshot,
                                    initialProtein = entry.proteinSnapshot,
                                    isCustom = true
                                )
                            },
                            onAddWhatIAte = {
                                addSheetPreselectedSectionId = resolvedSection.section.id
                                showAddSheet = true
                            },
                            onAddItem = {
                                addSheetPreselectedSectionId = resolvedSection.section.id
                                showAddSheet = true
                            },
                            onEditMeal = {
                                editingSection = resolvedSection.section
                            },
                            onPinTime = {
                                sectionToPin = resolvedDay.sections.firstOrNull { it.section.id == resolvedSection.section.id }
                            },
                            onSkipMeal = {
                                val secName = resolvedSection.section.name
                                confirmDialogState = ConfirmDialogState(
                                    title = "Skip meal",
                                    message = "Skip $secName for ${uiState.titleText}?",
                                    confirmVerb = "Skip",
                                    onConfirm = {
                                        viewModel.skipSection(resolvedSection.section.id)
                                        collapsedSections[resolvedSection.section.id] = true
                                    }
                                )
                            },
                            onRestoreMeal = {
                                val secName = resolvedSection.section.name
                                confirmDialogState = ConfirmDialogState(
                                    title = "Restore meal",
                                    message = "Restore $secName for ${uiState.titleText}?",
                                    confirmVerb = "Restore",
                                    onConfirm = {
                                        viewModel.restoreSectionOverride(resolvedSection.section.id)
                                        collapsedSections[resolvedSection.section.id] = false
                                    }
                                )
                            },
                            onRemoveMeal = {
                                val secName = resolvedSection.section.name
                                scopeSheetState = ScopeSheetState(
                                    targetName = secName,
                                    isRemoveMode = true,
                                    onScopeConfirmed = { scopeSelection, sentence ->
                                        confirmDialogState = ConfirmDialogState(
                                            title = "Remove $secName",
                                            message = sentence,
                                            confirmVerb = "Remove",
                                            onConfirm = {
                                                viewModel.removeSectionWithScope(resolvedSection.section.id, scopeSelection)
                                            }
                                        )
                                    }
                                )
                            }
                        )
                    }

                    // "+ Add meal" text button at the end of the card list (Phase 2 item 6)
                    item(
                        key = "add_meal_button",
                        contentType = "add-meal"
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = Tokens.SpaceSm),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            TextButton(
                                onClick = { showAddMealSheet = true },
                                modifier = Modifier.height(Tokens.TouchMin)
                            ) {
                                Text(
                                    text = "+ Add meal",
                                    fontSize = Tokens.BodySize,
                                    color = colors.textPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Sheet
    if (showAddSheet && resolvedDay != null) {
        AddSheet(
            sections = resolvedDay.sections,
            preselectedSectionId = addSheetPreselectedSectionId,
            onSaveCustomLog = { secId, name, portion, kcal, protein ->
                viewModel.addCustomLogEntry(secId, name, portion, kcal, protein)
                showAddSheet = false
            },
            onSaveWithScope = { secId, name, portion, kcal, protein ->
                showAddSheet = false
                scopeSheetState = ScopeSheetState(
                    targetName = name,
                    isRemoveMode = false,
                    onScopeConfirmed = { scopeSelection, sentence ->
                        confirmDialogState = ConfirmDialogState(
                            title = "Add $name",
                            message = sentence,
                            confirmVerb = "Add",
                            onConfirm = {
                                viewModel.addItemWithScope(secId, name, portion, kcal, protein, scopeSelection)
                            }
                        )
                    }
                )
            },
            onDismiss = { showAddSheet = false }
        )
    }

    // Add Meal Sheet
    if (showAddMealSheet) {
        AddMealSheet(
            onContinueToScope = { mealName, idealMold, size, colorHex ->
                showAddMealSheet = false
                scopeSheetState = ScopeSheetState(
                    targetName = mealName,
                    isRemoveMode = false,
                    onScopeConfirmed = { scopeSelection, sentence ->
                        confirmDialogState = ConfirmDialogState(
                            title = "Add $mealName",
                            message = sentence,
                            confirmVerb = "Add",
                            onConfirm = {
                                viewModel.addSectionWithScope(mealName, idealMold, size, colorHex, scopeSelection)
                            }
                        )
                    }
                )
            },
            onDismiss = { showAddMealSheet = false }
        )
    }

    // Item Sheet
    itemSheetState?.let { sheetData ->
        ItemSheet(
            title = sheetData.title,
            initialMultiplier = sheetData.initialMultiplier,
            initialKcal = sheetData.initialKcal,
            initialProtein = sheetData.initialProtein,
            isCustomEntry = sheetData.isCustom,
            onSaveNutrition = { multiplier, kcal, protein ->
                if (sheetData.isCustom && sheetData.customLogEntryId != null) {
                    viewModel.updateCustomLogEntry(sheetData.customLogEntryId, kcal, protein, multiplier)
                } else if (sheetData.itemId != null) {
                    viewModel.updateItemMultiplierAndNutrition(sheetData.itemId, multiplier, kcal, protein)
                }
            },
            onEditItem = if (!sheetData.isCustom && sheetData.item != null) {
                {
                    val itemToEdit = sheetData.item
                    itemSheetState = null
                    editItemState = itemToEdit
                }
            } else null,
            onRemoveFromChecklist = {
                val itId = sheetData.itemId
                val itTitle = sheetData.title
                itemSheetState = null
                if (itId != null) {
                    scopeSheetState = ScopeSheetState(
                        targetName = itTitle,
                        isRemoveMode = true,
                        onScopeConfirmed = { scopeSelection, sentence ->
                            confirmDialogState = ConfirmDialogState(
                                title = "Remove $itTitle",
                                message = sentence,
                                confirmVerb = "Remove",
                                onConfirm = {
                                    viewModel.removeItemWithScope(itId, scopeSelection)
                                }
                            )
                        }
                    )
                }
            },
            onDeleteCustomEntry = {
                val entryId = sheetData.customLogEntryId
                val entryTitle = sheetData.title
                itemSheetState = null
                if (entryId != null) {
                    confirmDialogState = ConfirmDialogState(
                        title = "Delete $entryTitle",
                        message = "Delete $entryTitle for ${uiState.selectedDate.format(dayFormatShort)}?",
                        confirmVerb = "Delete",
                        onConfirm = {
                            viewModel.deleteCustomLogEntry(entryId) { deleted ->
                                coroutineScope.launch {
                                    val result = snackbarHostState.showSnackbar(
                                        message = "Deleted ${deleted.nameSnapshot}",
                                        actionLabel = "Undo",
                                        duration = SnackbarDuration.Short
                                    )
                                    if (result == SnackbarResult.ActionPerformed) {
                                        viewModel.restoreLogEntry(deleted)
                                    }
                                }
                            }
                        }
                    )
                }
            },
            onDismiss = { itemSheetState = null }
        )
    }

    // Edit Item Sheet
    editItemState?.let { itemToEdit ->
        EditItemSheet(
            item = itemToEdit,
            onSave = { name, portion, kcal, protein, isPriority, isOptional ->
                viewModel.updateFoodItem(
                    itemId = itemToEdit.id,
                    name = name,
                    portion = portion,
                    kcal = kcal,
                    protein = protein,
                    isPriority = isPriority,
                    isOptional = isOptional
                )
                editItemState = null
            },
            onDismiss = {
                editItemState = null
            }
        )
    }

    // Edit Meal Sheet
    editingSection?.let { secToEdit ->
        EditMealSheet(
            section = secToEdit,
            onSave = { name, idealMold, size, colorHex ->
                viewModel.updateSection(
                    sectionId = secToEdit.id,
                    name = name,
                    idealMold = idealMold,
                    size = size,
                    colorHex = colorHex
                )
                editingSection = null
            },
            onDismiss = {
                editingSection = null
            }
        )
    }

    // Scope Sheet
    scopeSheetState?.let { sheetState ->
        ScopeSheet(
            targetName = sheetState.targetName,
            isRemoveMode = sheetState.isRemoveMode,
            currentDate = uiState.selectedDate,
            onContinue = { scopeSelection, sentence ->
                scopeSheetState = null
                sheetState.onScopeConfirmed(scopeSelection, sentence)
            },
            onDismiss = { scopeSheetState = null }
        )
    }

    // Confirm Dialog
    confirmDialogState?.let { dialog ->
        ConfirmDialog(
            title = dialog.title,
            message = dialog.message,
            confirmVerb = dialog.confirmVerb,
            onConfirm = {
                confirmDialogState = null
                dialog.onConfirm()
            },
            onDismiss = { confirmDialogState = null }
        )
    }

    // Pin Time Dialog
    sectionToPin?.let { sec ->
        val initialHour = (sec.effectiveTimeMold % 1440) / 60
        val initialMinute = (sec.effectiveTimeMold % 1440) % 60

        TimePickerDialog(
            initialHour = initialHour,
            initialMinute = initialMinute,
            onConfirm = { mold ->
                sectionToPin = null
                confirmDialogState = ConfirmDialogState(
                    title = "Pin time",
                    message = "Pin ${sec.section.name} to ${DateUtils.formatMold(mold)} for ${uiState.selectedDate.format(dayFormatShort)}?",
                    confirmVerb = "Pin",
                    onConfirm = {
                        viewModel.pinSectionTime(sec.section.id, mold)
                    }
                )
            },
            onDismiss = { sectionToPin = null }
        )
    }
}

@Composable
fun WeekStrip(
    selectedDate: LocalDate,
    logicalToday: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    val colors = BulkTrackerTheme.colors
    val mondayOfToday = DateUtils.mondayOf(logicalToday)
    val pagerState = rememberPagerState(initialPage = 1000, pageCount = { 2001 })

    Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            val weekStart = mondayOfToday.plusWeeks((page - 1000).toLong())
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Tokens.SpaceSm),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0..6) {
                    val date = weekStart.plusDays(i.toLong())
                    val isSelected = date == selectedDate
                    val isToday = date == logicalToday
                    val isFuture = date.isAfter(logicalToday)
                    val weekdayLetter = date.dayOfWeek.name.take(1)

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(Tokens.RadiusMd))
                            .clickable { onDateSelected(date) }
                            .padding(vertical = Tokens.SpaceXs, horizontal = Tokens.SpaceXxs)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(Tokens.SpaceXs)
                                .then(
                                    if (isToday && !isSelected) {
                                        Modifier.background(colors.ink, CircleShape)
                                    } else {
                                        Modifier
                                    }
                                )
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = weekdayLetter,
                            fontSize = Tokens.LabelSize,
                            lineHeight = Tokens.LabelLineHeight,
                            color = colors.textTertiary
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .then(
                                    if (isSelected) {
                                        Modifier.background(colors.accent, CircleShape)
                                    } else {
                                        Modifier
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = date.dayOfMonth.toString(),
                                fontSize = Tokens.BodySize,
                                fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) colors.onAccentText else colors.textPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        StatusMarker(
                            completionPercent = 0,
                            isFutureDay = isFuture
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(Tokens.IconMd),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.KeyboardArrowDown,
                contentDescription = null,
                tint = colors.textTertiary,
                modifier = Modifier.size(Tokens.IconSm)
            )
        }
    }
}

@Composable
fun NextMealCard(
    model: NextMealCardDisplayModel,
    onMarkAllEaten: (Long) -> Unit,
    onScrollToShow: (Long) -> Unit
) {
    val colors = BulkTrackerTheme.colors

    Card(
        shape = RoundedCornerShape(Tokens.RadiusLg),
        colors = CardDefaults.cardColors(containerColor = colors.card),
        border = androidx.compose.foundation.BorderStroke(Tokens.BorderStrong, colors.ink),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            if (model.isAllDone) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = colors.success,
                        modifier = Modifier.size(Tokens.IconMd)
                    )
                    Spacer(modifier = Modifier.width(Tokens.SpaceSm))
                    Text(
                        text = "All meals done today",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionDot(color = model.color)
                    Spacer(modifier = Modifier.width(Tokens.SpaceSm))
                    Text(
                        text = model.sectionName,
                        fontFamily = SerifFamily,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = model.timeFormatted,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Normal,
                        color = colors.textPrimary
                    )
                }

                Spacer(modifier = Modifier.height(Tokens.SpaceXs))

                Text(
                    text = model.subtext,
                    fontSize = Tokens.BodySmSize,
                    lineHeight = Tokens.BodySmLineHeight,
                    color = colors.textSecondary
                )

                Spacer(modifier = Modifier.height(Tokens.SpaceLg))

                val secId = model.sectionId
                if (secId != null) {
                    Button(
                        onClick = { onMarkAllEaten(secId) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.ink,
                            contentColor = colors.onInk
                        ),
                        shape = RoundedCornerShape(Tokens.RadiusMd),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(Tokens.TouchMin)
                    ) {
                        Text(
                            text = "Mark all eaten",
                            fontSize = Tokens.BodySmSize,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(Tokens.SpaceXs))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        TextButton(
                            onClick = { onScrollToShow(secId) },
                            modifier = Modifier.height(Tokens.TouchMin)
                        ) {
                            Text(
                                text = "Show",
                                fontSize = Tokens.BodySmSize,
                                color = colors.textPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryCard(
    model: SummaryCardDisplayModel
) {
    val colors = BulkTrackerTheme.colors

    Card(
        shape = RoundedCornerShape(Tokens.RadiusLg),
        colors = CardDefaults.cardColors(containerColor = colors.card),
        border = androidx.compose.foundation.BorderStroke(Tokens.BorderThin, colors.border),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Tokens.SpaceLg)
        ) {
            ProgressRow(
                label = model.calLabel,
                currentValueText = model.calCurrentText,
                targetValueText = model.calTargetText,
                currentValue = model.calCurrent,
                targetValue = model.calTarget,
                unitText = "kcal",
                extraText = model.calExtraText
            )

            Spacer(modifier = Modifier.height(Tokens.SpaceMd))

            ProgressRow(
                label = model.proLabel,
                currentValueText = model.proCurrentText,
                targetValueText = model.proTargetText,
                currentValue = model.proCurrent,
                targetValue = model.proTarget,
                unitText = "g"
            )

            Spacer(modifier = Modifier.height(Tokens.SpaceSm))

            Text(
                text = model.checklistPercentText,
                fontSize = Tokens.CaptionSize,
                lineHeight = Tokens.CaptionLineHeight,
                color = colors.textSecondary
            )

            if (model.showPriorityWarning) {
                Spacer(modifier = Modifier.height(Tokens.SpaceXxs))
                Text(
                    text = model.priorityWarningText,
                    fontSize = Tokens.CaptionSize,
                    lineHeight = Tokens.CaptionLineHeight,
                    color = colors.warningText
                )
            }
        }
    }
}

@Composable
fun SectionCard(
    model: ResolvedSectionDisplayModel,
    isCollapsed: Boolean,
    onToggleCollapse: () -> Unit,
    onItemToggle: (Long) -> Unit,
    onItemLongPress: (ResolvedItemDisplayModel) -> Unit,
    onCustomEntryLongPress: (CustomEntryDisplayModel) -> Unit,
    onAddWhatIAte: () -> Unit,
    onAddItem: () -> Unit,
    onEditMeal: () -> Unit,
    onPinTime: () -> Unit,
    onSkipMeal: () -> Unit,
    onRestoreMeal: () -> Unit,
    onRemoveMeal: () -> Unit
) {
    val colors = BulkTrackerTheme.colors

    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(Tokens.RadiusLg),
        colors = CardDefaults.cardColors(containerColor = colors.card),
        border = androidx.compose.foundation.BorderStroke(Tokens.BorderThin, colors.border),
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(Motion.fastTween())
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Tokens.SpaceLg)
        ) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleCollapse() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionDot(color = model.color)
                Spacer(modifier = Modifier.width(Tokens.SpaceSm))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = model.section.name,
                        style = TitleMd,
                        color = colors.textPrimary,
                        modifier = Modifier.semantics { heading() }
                    )
                    Text(
                        text = model.subtext,
                        fontSize = Tokens.CaptionSize,
                        lineHeight = Tokens.CaptionLineHeight,
                        color = colors.textSecondary
                    )
                }

                if (model.isAllDone) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Completed",
                        tint = colors.success,
                        modifier = Modifier.size(Tokens.IconSm)
                    )
                    Spacer(modifier = Modifier.width(Tokens.SpaceXs))
                }

                if (model.pinnedMold != null || model.slotStatus == "PINNED") {
                    Icon(
                        imageVector = Icons.Outlined.PushPin,
                        contentDescription = "Pinned time",
                        tint = colors.textPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(Tokens.SpaceXs))
                } else if (model.slotStatus == "MOVED") {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = "Moved time",
                        tint = colors.textSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(Tokens.SpaceXs))
                }

                Text(
                    text = model.timeFormatted,
                    fontSize = Tokens.BodySmSize,
                    lineHeight = Tokens.BodySmLineHeight,
                    color = colors.textPrimary
                )

                Spacer(modifier = Modifier.width(Tokens.SpaceXs))

                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.size(Tokens.IconMd)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.MoreVert,
                            contentDescription = "Meal options",
                            tint = colors.textTertiary
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Add item", color = colors.textPrimary) },
                            onClick = {
                                menuExpanded = false
                                onAddItem()
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Edit meal…", color = colors.textPrimary) },
                            onClick = {
                                menuExpanded = false
                                onEditMeal()
                            }
                        )

                        if (!model.isSkipped && model.pinnedMold == null) {
                            DropdownMenuItem(
                                text = { Text("Pin time for this day", color = colors.textPrimary) },
                                onClick = {
                                    menuExpanded = false
                                    onPinTime()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Skip this meal this day", color = colors.textPrimary) },
                                onClick = {
                                    menuExpanded = false
                                    onSkipMeal()
                                }
                            )
                        } else if (model.isSkipped) {
                            DropdownMenuItem(
                                text = { Text("Restore", color = colors.textPrimary) },
                                onClick = {
                                    menuExpanded = false
                                    onRestoreMeal()
                                }
                            )
                        } else if (model.pinnedMold != null) {
                            DropdownMenuItem(
                                text = { Text("Unpin time", color = colors.textPrimary) },
                                onClick = {
                                    menuExpanded = false
                                    onRestoreMeal()
                                }
                            )
                        }

                        DropdownMenuItem(
                            text = { Text("Remove meal…", color = colors.danger) },
                            onClick = {
                                menuExpanded = false
                                onRemoveMeal()
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.width(Tokens.SpaceXs))

                Icon(
                    imageVector = if (isCollapsed) Icons.Filled.KeyboardArrowDown else Icons.Filled.KeyboardArrowUp,
                    contentDescription = if (isCollapsed) "Expand" else "Collapse",
                    tint = colors.textTertiary,
                    modifier = Modifier.size(Tokens.IconMd)
                )
            }

            AnimatedVisibility(visible = !isCollapsed) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.height(Tokens.SpaceSm))

                    // Items list
                    model.items.forEach { resolvedItem ->
                        key(resolvedItem.item.id) {
                            ChecklistItemRow(
                                resolvedItem = resolvedItem,
                                sectionColor = model.color,
                                onToggle = { onItemToggle(resolvedItem.item.id) },
                                onLongPress = { onItemLongPress(resolvedItem) }
                            )
                        }
                    }

                    // Custom entries (What I ate)
                    if (model.customEntries.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(Tokens.SpaceSm))
                        Text(
                            text = "What I ate",
                            fontSize = Tokens.LabelSize,
                            fontWeight = FontWeight.Medium,
                            color = colors.textTertiary
                        )
                        Spacer(modifier = Modifier.height(Tokens.SpaceXs))

                        model.customEntries.forEach { customEntry ->
                            key(customEntry.entry.id) {
                                CustomEntryRow(
                                    model = customEntry,
                                    sectionColor = model.color,
                                    onLongPress = { onCustomEntryLongPress(customEntry) }
                                )
                            }
                        }
                    }

                    // Buttons
                    if (model.section.isMealSection) {
                        Spacer(modifier = Modifier.height(Tokens.SpaceXs))
                        TextButton(
                            onClick = onAddWhatIAte,
                            modifier = Modifier.height(Tokens.TouchMin)
                        ) {
                            Text(
                                text = "+ Add what I ate",
                                fontSize = Tokens.BodySmSize,
                                color = colors.textPrimary
                            )
                        }
                    }

                    TextButton(
                        onClick = onAddItem,
                        modifier = Modifier.height(Tokens.TouchMin)
                    ) {
                        Text(
                            text = "+ Add item",
                            fontSize = Tokens.BodySmSize,
                            color = colors.textPrimary
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChecklistItemRow(
    resolvedItem: ResolvedItemDisplayModel,
    sectionColor: Color,
    onToggle: () -> Unit,
    onLongPress: () -> Unit
) {
    val colors = BulkTrackerTheme.colors
    val item = resolvedItem.item
    val checked = resolvedItem.isChecked

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = Tokens.RowMin)
            .combinedClickable(
                onClick = onToggle,
                onLongClick = onLongPress,
                role = Role.Checkbox
            )
            .padding(vertical = Tokens.SpaceSm)
            .semantics(mergeDescendants = true) {},
        verticalAlignment = Alignment.CenterVertically
    ) {
        MealCheckbox(
            checked = checked,
            sectionColor = sectionColor
        )

        Spacer(modifier = Modifier.width(Tokens.SpaceMd))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = item.name,
                    fontSize = Tokens.BodySize,
                    lineHeight = Tokens.BodyLineHeight,
                    color = if (checked) colors.textTertiary else colors.textPrimary,
                    textDecoration = if (checked) TextDecoration.LineThrough else TextDecoration.None,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (resolvedItem.isPriority) {
                    Spacer(modifier = Modifier.width(Tokens.SpaceXs))
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = "Priority",
                        tint = colors.priorityStar,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            if (resolvedItem.portionLabel.isNotEmpty()) {
                Text(
                    text = resolvedItem.portionLabel,
                    fontSize = Tokens.CaptionSize,
                    lineHeight = Tokens.CaptionLineHeight,
                    color = colors.textSecondary
                )
            }

            if (resolvedItem.isOptional) {
                Text(
                    text = "optional",
                    fontSize = Tokens.LabelSize,
                    lineHeight = Tokens.LabelLineHeight,
                    color = colors.textTertiary
                )
            }

            if (resolvedItem.isMergedFromOtherSection && resolvedItem.originalSectionName != null) {
                Text(
                    text = "From ${resolvedItem.originalSectionName}",
                    fontSize = Tokens.LabelSize,
                    lineHeight = Tokens.LabelLineHeight,
                    color = colors.textTertiary
                )
            }
        }

        Spacer(modifier = Modifier.width(Tokens.SpaceSm))

        Text(
            text = resolvedItem.kcalFormatted,
            fontSize = 14.sp,
            color = if (checked) colors.textTertiary else colors.textPrimary,
            textDecoration = if (checked) TextDecoration.LineThrough else TextDecoration.None
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CustomEntryRow(
    model: CustomEntryDisplayModel,
    sectionColor: Color,
    onLongPress: () -> Unit
) {
    val colors = BulkTrackerTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = Tokens.RowMin)
            .combinedClickable(
                onClick = onLongPress,
                onLongClick = onLongPress
            )
            .padding(vertical = Tokens.SpaceSm)
            .semantics(mergeDescendants = true) {},
        verticalAlignment = Alignment.CenterVertically
    ) {
        MealCheckbox(
            checked = true,
            sectionColor = sectionColor
        )

        Spacer(modifier = Modifier.width(Tokens.SpaceMd))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = model.name,
                fontSize = Tokens.BodySize,
                lineHeight = Tokens.BodyLineHeight,
                color = colors.textTertiary,
                textDecoration = TextDecoration.LineThrough,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (model.portion.isNotEmpty()) {
                Text(
                    text = model.portion,
                    fontSize = Tokens.CaptionSize,
                    lineHeight = Tokens.CaptionLineHeight,
                    color = colors.textSecondary
                )
            }
        }

        Spacer(modifier = Modifier.width(Tokens.SpaceSm))

        Text(
            text = model.kcalFormatted,
            fontSize = 14.sp,
            color = colors.textTertiary,
            textDecoration = TextDecoration.LineThrough
        )
    }
}
