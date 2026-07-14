package com.example.lessonmonitor.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lessonmonitor.domain.repository.AttendanceRepository
import com.example.lessonmonitor.domain.repository.CalendarSessionEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DayAgendaViewModel @Inject constructor(
    private val attendanceRepository: AttendanceRepository
) : ViewModel() {

    data class UiState(
        val epochDay: Long = 0L,
        val sessions: List<CalendarSessionEntry> = emptyList(),
        val isLoading: Boolean = true
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var loadedEpochDay: Long? = null

    /** Idempotent per [epochDay] so re-entering the same agenda (e.g. on recomposition) doesn't re-subscribe. */
    fun load(epochDay: Long) {
        if (epochDay == loadedEpochDay) return
        loadedEpochDay = epochDay
        _uiState.update { it.copy(epochDay = epochDay, isLoading = true) }
        viewModelScope.launch {
            attendanceRepository.getSessionsInRange(epochDay, epochDay).collect { sessions ->
                _uiState.update {
                    it.copy(sessions = sessions.sortedBy { entry -> entry.lessonTitle }, isLoading = false)
                }
            }
        }
    }
}
