package com.example.lessonmonitor.ui.lesson

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lessonmonitor.data.local.entity.AttendanceSessionEntity
import com.example.lessonmonitor.data.local.entity.LessonEntity
import com.example.lessonmonitor.domain.repository.AttendanceRepository
import com.example.lessonmonitor.domain.repository.EnrollmentRepository
import com.example.lessonmonitor.domain.repository.LessonRepository
import com.example.lessonmonitor.domain.repository.RosterEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeParseException
import javax.inject.Inject

@HiltViewModel
class LessonDetailViewModel @Inject constructor(
    private val lessonRepository: LessonRepository,
    private val enrollmentRepository: EnrollmentRepository,
    private val attendanceRepository: AttendanceRepository
) : ViewModel() {

    data class UiState(
        val lesson: LessonEntity? = null,
        val roster: List<RosterEntry> = emptyList(),
        val sessions: List<AttendanceSessionEntity> = emptyList(),
        val newSessionDateText: String = LocalDate.now().toString(),
        val errorMessage: String? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var loadedLessonId: Long? = null

    /** Idempotent per [lessonId] — (re)subscribes to that lesson's details, roster, and sessions only when it changes. */
    fun load(lessonId: Long) {
        if (lessonId == loadedLessonId) return
        loadedLessonId = lessonId

        viewModelScope.launch {
            lessonRepository.getById(lessonId).collect { lesson ->
                _uiState.update { it.copy(lesson = lesson) }
            }
        }
        viewModelScope.launch {
            enrollmentRepository.getRosterForLesson(lessonId).collect { roster ->
                _uiState.update { it.copy(roster = roster) }
            }
        }
        viewModelScope.launch {
            attendanceRepository.getSessionsForLesson(lessonId).collect { sessions ->
                _uiState.update { it.copy(sessions = sessions) }
            }
        }
    }

    fun onNewSessionDateTextChange(value: String) {
        _uiState.update { it.copy(newSessionDateText = value, errorMessage = null) }
    }

    fun addSession() {
        val lessonId = loadedLessonId ?: return
        val dateText = _uiState.value.newSessionDateText.trim()
        val sessionDate = try {
            LocalDate.parse(dateText).toEpochDay()
        } catch (e: DateTimeParseException) {
            _uiState.update { it.copy(errorMessage = "Date must be in yyyy-MM-dd format") }
            return
        }
        viewModelScope.launch {
            attendanceRepository.createSession(lessonId, sessionDate)
        }
    }

    fun unenroll(entry: RosterEntry) {
        viewModelScope.launch {
            enrollmentRepository.unenroll(entry.enrollment)
        }
    }
}
