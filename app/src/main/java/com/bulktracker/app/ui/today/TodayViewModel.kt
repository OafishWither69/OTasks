package com.bulktracker.app.ui.today

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.bulktracker.app.AppContainer
import com.bulktracker.app.data.db.DailyHabitEntity
import com.bulktracker.app.data.db.FoodItemEntity
import com.bulktracker.app.data.db.LogEntryEntity
import com.bulktracker.app.data.db.SectionEntity
import com.bulktracker.app.data.db.SettingsEntity
import com.bulktracker.app.data.repo.ScopeSelection
import com.bulktracker.app.data.repo.TrackerRepository
import com.bulktracker.app.domain.DayTotals
import com.bulktracker.app.domain.ResolvedDay
import com.bulktracker.app.util.DateUtils
import com.bulktracker.app.util.Format
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Immutable
data class ResolvedItemDisplayModel(
    val item: FoodItemEntity,
    val isChecked: Boolean,
    val isMergedFromOtherSection: Boolean,
    val originalSectionName: String?,
    val isPriority: Boolean,
    val isOptional: Boolean,
    val portionLabel: String,
    val kcalFormatted: String
)

@Immutable
data class CustomEntryDisplayModel(
    val entry: LogEntryEntity,
    val name: String,
    val portion: String,
    val kcalFormatted: String
)

@Immutable
data class ResolvedSectionDisplayModel(
    val section: SectionEntity,
    val isSkipped: Boolean,
    val isAllDone: Boolean,
    val pinnedMold: Int?,
    val effectiveTimeMold: Int,
    val slotStatus: String,
    val color: Color,
    val timeFormatted: String,
    val subtext: String,
    val items: List<ResolvedItemDisplayModel>,
    val customEntries: List<CustomEntryDisplayModel>,
    val totalKcal: Int
)

@Immutable
data class NextMealCardDisplayModel(
    val isAllDone: Boolean,
    val sectionId: Long? = null,
    val sectionName: String = "",
    val color: Color = Color(0xFF262626),
    val timeFormatted: String = "",
    val subtext: String = ""
)

@Immutable
data class SummaryCardDisplayModel(
    val calLabel: String,
    val calCurrentText: String,
    val calTargetText: String,
    val calCurrent: Double,
    val calTarget: Double,
    val calExtraText: String,
    val proLabel: String,
    val proCurrentText: String,
    val proTargetText: String,
    val proCurrent: Double,
    val proTarget: Double,
    val checklistPercentText: String,
    val showPriorityWarning: Boolean,
    val priorityWarningText: String
)

@Immutable
data class TodayUiState(
    val selectedDate: LocalDate,
    val logicalToday: LocalDate,
    val resolvedDay: ResolvedDay? = null,
    val dayStartHour: Int = 4,
    val isAfter6Pm: Boolean = false,
    val titleText: String = "",
    val isRestDay: Boolean = false,
    val nearestSectionId: Long? = null,
    val hasNextMealCard: Boolean = false,
    val nextMealCard: NextMealCardDisplayModel? = null,
    val summaryCard: SummaryCardDisplayModel? = null,
    val sections: List<ResolvedSectionDisplayModel> = emptyList()
)

@OptIn(ExperimentalCoroutinesApi::class)
class TodayViewModel(
    private val appContainer: AppContainer,
    private val trackerRepository: TrackerRepository
) : ViewModel() {

    private val nowFlow = MutableStateFlow(LocalDateTime.now())

    fun updateNow() {
        nowFlow.value = LocalDateTime.now()
    }

    private val settingsFlow = trackerRepository.observeSettings()

    val uiState: StateFlow<TodayUiState> = combine(
        appContainer.selectedDate,
        settingsFlow,
        nowFlow
    ) { selectedDate, settings, now ->
        Triple(selectedDate, settings, now)
    }.flatMapLatest { (selectedDate, settings, now) ->
        val logToday = DateUtils.logicalToday(now, settings.dayStartHour)
        val isAfter6 = now.hour >= 18

        trackerRepository.observeDay(selectedDate).combine(settingsFlow) { resolvedDay, _ ->
            computeUiState(selectedDate, logToday, isAfter6, settings, now, resolvedDay)
        }.flowOn(Dispatchers.Default)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TodayUiState(
            selectedDate = appContainer.selectedDate.value,
            logicalToday = DateUtils.logicalToday(LocalDateTime.now(), 4)
        )
    )

    fun onDateSelected(date: LocalDate) {
        appContainer.selectedDate.value = date
    }

    fun selectToday() {
        val settings = uiState.value.resolvedDay?.settings
        val hour = settings?.dayStartHour ?: 4
        val today = DateUtils.logicalToday(LocalDateTime.now(), hour)
        appContainer.selectedDate.value = today
    }

    fun toggleItem(itemId: Long) {
        viewModelScope.launch {
            trackerRepository.toggleItem(uiState.value.selectedDate, itemId)
        }
    }

    fun toggleBareMinimum() {
        viewModelScope.launch {
            trackerRepository.toggleBareMinimum(uiState.value.selectedDate)
        }
    }

    fun toggleGymRestDay() {
        viewModelScope.launch {
            val habit = uiState.value.resolvedDay?.habit
            val settings = uiState.value.resolvedDay?.settings
            val selectedDate = uiState.value.selectedDate
            val weekdayBit = 1 shl (selectedDate.dayOfWeek.value - 1)
            val isRest = when {
                habit?.gymPlan == "REST" -> true
                habit?.gymPlan == "TRAIN" -> false
                settings != null -> (settings.gymRestDaysMask and weekdayBit) != 0
                else -> false
            }
            val newPlan = if (isRest) "TRAIN" else "REST"
            trackerRepository.setGymPlan(selectedDate, newPlan)
        }
    }

    fun markAllEaten(sectionId: Long) {
        viewModelScope.launch {
            trackerRepository.markAllEaten(uiState.value.selectedDate, sectionId)
        }
    }

    fun pinSectionTime(sectionId: Long, pinnedMold: Int) {
        viewModelScope.launch {
            trackerRepository.pinSectionTime(uiState.value.selectedDate, sectionId, pinnedMold)
        }
    }

    fun skipSection(sectionId: Long) {
        viewModelScope.launch {
            trackerRepository.skipSection(uiState.value.selectedDate, sectionId)
        }
    }

    fun restoreSectionOverride(sectionId: Long) {
        viewModelScope.launch {
            trackerRepository.restoreSectionOverride(uiState.value.selectedDate, sectionId)
        }
    }

    fun addCustomLogEntry(sectionId: Long, name: String, portion: String, kcal: Int?, protein: Double?) {
        viewModelScope.launch {
            trackerRepository.addCustomLogEntry(uiState.value.selectedDate, sectionId, name, portion, kcal, protein)
        }
    }

    fun updateCustomLogEntry(logEntryId: Long, kcal: Int?, protein: Double?, multiplier: Double) {
        viewModelScope.launch {
            trackerRepository.updateCustomLogEntry(logEntryId, kcal, protein, multiplier)
        }
    }

    fun deleteCustomLogEntry(logEntryId: Long, onDeleted: (LogEntryEntity) -> Unit) {
        viewModelScope.launch {
            val entry = trackerRepository.deleteCustomLogEntry(logEntryId)
            if (entry != null) {
                onDeleted(entry)
            }
        }
    }

    fun restoreLogEntry(entry: LogEntryEntity) {
        viewModelScope.launch {
            trackerRepository.restoreLogEntry(entry)
        }
    }

    fun updateItemMultiplierAndNutrition(itemId: Long, multiplier: Double, customKcal: Int?, customProtein: Double?) {
        viewModelScope.launch {
            trackerRepository.updateItemMultiplierAndNutrition(uiState.value.selectedDate, itemId, multiplier, customKcal, customProtein)
        }
    }

    fun removeItemWithScope(itemId: Long, scope: ScopeSelection) {
        viewModelScope.launch {
            trackerRepository.removeItemWithScope(uiState.value.selectedDate, itemId, scope)
        }
    }

    fun removeSectionWithScope(sectionId: Long, scope: ScopeSelection) {
        viewModelScope.launch {
            trackerRepository.removeSectionWithScope(uiState.value.selectedDate, sectionId, scope)
        }
    }

    fun addItemWithScope(
        sectionId: Long,
        name: String,
        portion: String,
        kcal: Int?,
        protein: Double?,
        scope: ScopeSelection
    ) {
        viewModelScope.launch {
            trackerRepository.addItemWithScope(uiState.value.selectedDate, sectionId, name, portion, kcal, protein, scope)
        }
    }

    fun addSectionWithScope(
        name: String,
        idealMold: Int,
        size: String,
        colorHex: String,
        scope: ScopeSelection
    ) {
        viewModelScope.launch {
            trackerRepository.addSectionWithScope(uiState.value.selectedDate, name, idealMold, size, colorHex, scope)
        }
    }

    fun updateFoodItem(
        itemId: Long,
        name: String,
        portion: String,
        kcal: Int?,
        protein: Double?,
        isPriority: Boolean,
        isOptional: Boolean
    ) {
        viewModelScope.launch {
            trackerRepository.updateFoodItem(itemId, name, portion, kcal, protein, isPriority, isOptional)
        }
    }

    fun updateSection(
        sectionId: Long,
        name: String,
        idealMold: Int,
        size: String,
        colorHex: String
    ) {
        viewModelScope.launch {
            trackerRepository.updateSection(sectionId, name, idealMold, size, colorHex)
        }
    }

    companion object {
        fun provideFactory(appContainer: AppContainer): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return TodayViewModel(appContainer, appContainer.trackerRepository) as T
                }
            }
    }
}

private fun computeUiState(
    selectedDate: LocalDate,
    logicalToday: LocalDate,
    isAfter6Pm: Boolean,
    settings: SettingsEntity,
    now: LocalDateTime,
    resolvedDay: ResolvedDay?
): TodayUiState {
    val dayFormatShort = DateTimeFormatter.ofPattern("EEE, d MMM", Locale.US)
    val titleText = when (selectedDate) {
        logicalToday -> "Today"
        logicalToday.minusDays(1) -> "Yesterday"
        logicalToday.plusDays(1) -> "Tomorrow"
        else -> selectedDate.format(dayFormatShort)
    }

    val weekdayBit = 1 shl (selectedDate.dayOfWeek.value - 1)
    val habit = resolvedDay?.habit
    val isRestDay = when {
        habit?.gymPlan == "REST" -> true
        habit?.gymPlan == "TRAIN" -> false
        else -> (settings.gymRestDaysMask and weekdayBit) != 0
    }

    val nowMold = now.hour * 60 + now.minute
    val nearestSectionId = resolvedDay?.sections?.minByOrNull {
        kotlin.math.abs(it.effectiveTimeMold - nowMold)
    }?.section?.id

    val hasNextMealCard = (selectedDate == logicalToday) && (resolvedDay != null)
    val nextMealCard = if (hasNextMealCard && resolvedDay != null) {
        val nowMoldDay = DateUtils.nowToMold(now, selectedDate)
        val nextSlot = resolvedDay.dayPlan.slots.firstOrNull { slot ->
            val sec = resolvedDay.sections.firstOrNull { it.section.id == slot.sectionId }
            slot.mold >= nowMoldDay - 15 && sec != null && !sec.isAllDone && !sec.isSkipped && slot.status != "SKIPPED" && slot.status != "MERGED"
        }
        val nextSection = nextSlot?.let { slot ->
            resolvedDay.sections.firstOrNull { it.section.id == slot.sectionId }
        }

        if (nextSection == null) {
            NextMealCardDisplayModel(isAllDone = true)
        } else {
            val secColor = try {
                Color(android.graphics.Color.parseColor(nextSection.section.colorHex))
            } catch (_: Exception) {
                Color(0xFF262626)
            }
            val itemsLeft = nextSection.items.count { !it.isChecked && !it.item.isOptional }
            NextMealCardDisplayModel(
                isAllDone = false,
                sectionId = nextSection.section.id,
                sectionName = nextSection.section.name,
                color = secColor,
                timeFormatted = DateUtils.formatMold(nextSection.effectiveTimeMold),
                subtext = "$itemsLeft items left · ${Format.formatKcal(nextSection.totalKcal)} kcal"
            )
        }
    } else null

    val totals = resolvedDay?.totals
    val summaryCard = totals?.let { tot ->
        val unknownSuffix = if (tot.unknownCount > 0) " · ${tot.unknownCount} unknown" else ""
        val calLabel = if (tot.isBareMinimum) "Bare-minimum calories" else "Calories"
        val proLabel = if (tot.isBareMinimum) "Bare-minimum protein" else "Protein"
        val showWarning = (selectedDate == logicalToday) && isAfter6Pm && (tot.priorityItemsLeftCount > 0)
        SummaryCardDisplayModel(
            calLabel = calLabel,
            calCurrentText = Format.formatKcal(tot.caloriesEaten),
            calTargetText = Format.formatKcal(tot.caloriesTarget),
            calCurrent = tot.caloriesEaten.toDouble(),
            calTarget = tot.caloriesTarget.toDouble(),
            calExtraText = unknownSuffix,
            proLabel = proLabel,
            proCurrentText = Format.formatProtein(tot.proteinEatenG),
            proTargetText = Format.formatProtein(tot.proteinTargetG),
            proCurrent = tot.proteinEatenG,
            proTarget = tot.proteinTargetG,
            checklistPercentText = "Checklist ${tot.completionPercent}%",
            showPriorityWarning = showWarning,
            priorityWarningText = "★ ${tot.priorityItemsLeftCount} priority items left"
        )
    }

    val sectionUiModels = resolvedDay?.sections?.map { sec ->
        val secColor = try {
            Color(android.graphics.Color.parseColor(sec.section.colorHex))
        } catch (_: Exception) {
            Color(0xFF262626)
        }
        val subtext = if (sec.isSkipped) {
            "Skipped"
        } else {
            "${sec.eatenCount} of ${sec.items.size} · ${Format.formatKcal(sec.totalKcal)} kcal"
        }
        val itemsList = sec.items.map { ri ->
            ResolvedItemDisplayModel(
                item = ri.item,
                isChecked = ri.isChecked,
                isMergedFromOtherSection = ri.isMergedFromOtherSection,
                originalSectionName = ri.originalSectionName,
                isPriority = ri.item.isPriority,
                isOptional = ri.item.isOptional,
                portionLabel = ri.item.portionLabel,
                kcalFormatted = Format.formatKcal(ri.item.kcal)
            )
        }.toList()
        val customList = sec.customEntries.map { entry ->
            val effKcal = entry.kcalSnapshot?.let { (it * entry.multiplier).toInt() }
            CustomEntryDisplayModel(
                entry = entry,
                name = entry.nameSnapshot,
                portion = entry.portionSnapshot,
                kcalFormatted = Format.formatKcal(effKcal)
            )
        }.toList()
        ResolvedSectionDisplayModel(
            section = sec.section,
            isSkipped = sec.isSkipped,
            isAllDone = sec.isAllDone,
            pinnedMold = sec.pinnedMold,
            effectiveTimeMold = sec.effectiveTimeMold,
            slotStatus = sec.slotStatus,
            color = secColor,
            timeFormatted = DateUtils.formatMold(sec.effectiveTimeMold),
            subtext = subtext,
            items = itemsList,
            customEntries = customList,
            totalKcal = sec.totalKcal
        )
    }?.toList() ?: emptyList()

    return TodayUiState(
        selectedDate = selectedDate,
        logicalToday = logicalToday,
        resolvedDay = resolvedDay,
        dayStartHour = settings.dayStartHour,
        isAfter6Pm = isAfter6Pm,
        titleText = titleText,
        isRestDay = isRestDay,
        nearestSectionId = nearestSectionId,
        hasNextMealCard = hasNextMealCard,
        nextMealCard = nextMealCard,
        summaryCard = summaryCard,
        sections = sectionUiModels
    )
}
