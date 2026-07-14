package com.example.lessonmonitor.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lessonmonitor.domain.repository.AttendanceRepository
import com.example.lessonmonitor.domain.repository.CalendarSessionEntry
import com.example.lessonmonitor.domain.schedule.RecurringSessionGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.YearMonth
import javax.inject.Inject

/**
 * Feature: Calendar/schedule view (PLAN.md §7 milestone 10).
 *
 * Also re-runs [RecurringSessionGenerator] once on creation — PLAN.md §1
 * assumption #4 lists "the Calendar screen is opened" as one of the rolling
 * window's regeneration triggers (alongside app-open, already wired in
 * [com.example.lessonmonitor.navigation.MainScreenViewModel]). Re-running it
 * here is always safe since `createSession` is idempotent.
 */
@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val attendanceRepository: AttendanceRepository,
    recurringSessionGenerator: RecurringSessionGenerator
) : ViewModel() {

    data class UiState(
        val yearMonth: YearMonth = YearMonth.now(),
        val sessionsByDay: Map<Long, List<CalendarSessionEntry>> = emptyMap(),
        val isLoading: Boolean = true
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var sessionsJob: Job? = null

    init {
        viewModelScope.launch { recurringSessionGenerator.generateUpcomingSessions() }
        loadMonth(YearMonth.now())
    }

    fun goToPreviousMonth() = loadMonth(_uiState.value.yearMonth.minusMonths(1))

    fun goToNextMonth() = loadMonth(_uiState.value.yearMonth.plusMonths(1))

    fun goToToday() = loadMonth(YearMonth.now())

    private fun loadMonth(yearMonth: YearMonth) {
        sessionsJob?.cancel()
        _uiState.update { it.copy(yearMonth = yearMonth, isLoading = true) }
        val startEpochDay = yearMonth.atDay(1).toEpochDay()
        val endEpochDay = yearMonth.atEndOfMonth().toEpochDay()
        sessionsJob = viewModelScope.launch {
            attendanceRepository.getSessionsInRange(startEpochDay, endEpochDay).collect { entries ->
                _uiState.update {
                    it.copy(
                        sessionsByDay = entries.groupBy { entry -> entry.session.sessionDate },
                        isLoading = false
                    )
                }
            }
        }
    }
}
