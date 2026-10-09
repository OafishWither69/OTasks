package com.bulktracker.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.bulktracker.app.AppContainer
import com.bulktracker.app.data.db.SectionEntity
import com.bulktracker.app.data.db.SettingsEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val settings: SettingsEntity = SettingsEntity(),
    val sections: List<SectionEntity> = emptyList()
)

class SettingsViewModel(
    private val appContainer: AppContainer
) : ViewModel() {

    private val trackerRepo = appContainer.trackerRepository

    val uiState: StateFlow<SettingsUiState> = combine(
        trackerRepo.observeSettings(),
        trackerRepo.trackerDao.observeAllSections()
    ) { settings, sections ->
        SettingsUiState(
            settings = settings,
            sections = sections.sortedBy { it.sortOrder }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    fun setAlarmsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            trackerRepo.setAlarmsEnabled(enabled)
        }
    }

    fun setSectionAlarmEnabled(sectionId: Long, enabled: Boolean) {
        viewModelScope.launch {
            trackerRepo.setSectionAlarmEnabled(sectionId, enabled)
        }
    }

    companion object {
        fun provideFactory(appContainer: AppContainer): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SettingsViewModel(appContainer) as T
                }
            }
    }
}
