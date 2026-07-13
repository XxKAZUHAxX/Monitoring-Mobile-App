package com.example.lessonmonitor.ui.attendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lessonmonitor.data.local.entity.AttendanceStatus
import com.example.lessonmonitor.domain.repository.AttendanceRepository
import com.example.lessonmonitor.domain.repository.EnrollmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/**
 * Loads the roster + any existing records **once** per (lessonId, sessionId)
 * — edits are purely local until [submit], matching the load-once/edit/submit
 * pattern used by the other feature forms in this app (e.g.
 * `CategoryFormViewModel`), rather than continuously re-collecting Flows that
 * could otherwise clobber in-progress edits on an unrelated DB write.
 */
@HiltViewModel
class AttendanceSessionViewModel @Inject constructor(
    private val attendanceRepository: AttendanceRepository,
    private val enrollmentRepository: EnrollmentRepository
) : ViewModel() {

    data class RosterRowState(
        val studentId: Long,
        val studentName: String,
        val status: AttendanceStatus,
        val reason: String
    )

    data class UiState(
        val sessionDateText: String = "",
        val rows: List<RosterRowState> = emptyList(),
        val isLoading: Boolean = true,
        val isSubmitting: Boolean = false
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var sessionId: Long = 0L
    private var loadedKey: Pair<Long, Long>? = null

    /** Idempotent per (lessonId, sessionId) pair. */
    fun load(lessonId: Long, sessionId: Long) {
        val key = lessonId to sessionId
        if (key == loadedKey) return
        loadedKey = key
        this.sessionId = sessionId

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val session = attendanceRepository.getSession(sessionId).first()
            val roster = enrollmentRepository.getRosterForLesson(lessonId).first()
            val records = attendanceRepository.getRecordsForSession(sessionId).first()
            val rows = roster.map { entry ->
                val record = records.find { it.studentId == entry.student.id }
                RosterRowState(
                    studentId = entry.student.id,
                    studentName = entry.student.name,
                    // Default to PRESENT for a student with no record yet (assumption: attendance is opt-out, not opt-in).
                    status = record?.status ?: AttendanceStatus.PRESENT,
                    reason = record?.absenceReason.orEmpty()
                )
            }
            _uiState.value = UiState(
                sessionDateText = session?.let { LocalDate.ofEpochDay(it.sessionDate).toString() }.orEmpty(),
                rows = rows,
                isLoading = false
            )
        }
    }

    fun onStatusChange(studentId: Long, status: AttendanceStatus) {
        _uiState.update { state ->
            state.copy(
                rows = state.rows.map { row ->
                    if (row.studentId != studentId) {
                        row
                    } else {
                        val keepsReason = status == AttendanceStatus.ABSENT || status == AttendanceStatus.EXCUSED
                        row.copy(status = status, reason = if (keepsReason) row.reason else "")
                    }
                }
            )
        }
    }

    fun onReasonChange(studentId: Long, reason: String) {
        _uiState.update { state ->
            state.copy(rows = state.rows.map { row -> if (row.studentId == studentId) row.copy(reason = reason) else row })
        }
    }

    fun submit(onSaved: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            _uiState.value.rows.forEach { row ->
                val requiresReason = row.status == AttendanceStatus.ABSENT || row.status == AttendanceStatus.EXCUSED
                val reason = row.reason.ifBlank { null }.takeIf { requiresReason }
                attendanceRepository.markAttendance(sessionId, row.studentId, row.status, reason)
            }
            _uiState.update { it.copy(isSubmitting = false) }
            onSaved()
        }
    }
}
