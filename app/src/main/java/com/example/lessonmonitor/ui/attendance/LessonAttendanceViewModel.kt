package com.example.lessonmonitor.ui.attendance

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lessonmonitor.data.local.entity.AttendanceRecordEntity
import com.example.lessonmonitor.data.local.entity.AttendanceStatus
import com.example.lessonmonitor.domain.repository.AttendanceRepository
import com.example.lessonmonitor.domain.repository.EnrollmentRepository
import com.example.lessonmonitor.domain.repository.LessonRepository
import com.example.lessonmonitor.domain.repository.RosterEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LessonAttendanceViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val lessonRepository: LessonRepository,
    private val enrollmentRepository: EnrollmentRepository,
    private val attendanceRepository: AttendanceRepository
) : ViewModel() {

    private val lessonId: Long = savedStateHandle.get<Long>("lessonId") ?: 0L

    data class StudentRow(
        val studentId: Long,
        val studentName: String,
        val status: AttendanceStatus = AttendanceStatus.PRESENT,
        val absenceReason: String = "",
        val completed: Boolean = false,
        val isExistingRecord: Boolean = false
    )

    data class UiState(
        val lessonTitle: String = "",
        val lessonDate: String = "",
        val categoryId: Long = 0L,
        val students: List<StudentRow> = emptyList(),
        val isSaving: Boolean = false,
        val saved: Boolean = false
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var loadedLessonId: Long? = null

    fun load() {
        if (lessonId == loadedLessonId) return
        loadedLessonId = lessonId

        viewModelScope.launch {
            val lesson = lessonRepository.getById(lessonId).first() ?: return@launch
            val roster = enrollmentRepository.getRosterForCategory(lesson.categoryId).first()
            val existingRecords = attendanceRepository.getRecordsForLesson(lessonId).first()
            val recordByStudentId = existingRecords.associateBy { it.studentId }

            val students = roster.map { entry ->
                val record = recordByStudentId[entry.student.id]
                if (record != null) {
                    StudentRow(
                        studentId = entry.student.id,
                        studentName = entry.student.name,
                        status = record.status,
                        absenceReason = record.absenceReason.orEmpty(),
                        completed = record.completed,
                        isExistingRecord = true
                    )
                } else {
                    StudentRow(
                        studentId = entry.student.id,
                        studentName = entry.student.name
                    )
                }
            }

            _uiState.update {
                it.copy(
                    lessonTitle = lesson.title,
                    lessonDate = java.time.LocalDate.ofEpochDay(lesson.startDate).toString(),
                    categoryId = lesson.categoryId,
                    students = students
                )
            }
        }
    }

    fun onStatusChange(studentId: Long, status: AttendanceStatus) {
        _uiState.update { state ->
            state.copy(
                students = state.students.map { row ->
                    if (row.studentId == studentId) {
                        row.copy(
                            status = status,
                            completed = if (status != AttendanceStatus.PRESENT) false else row.completed,
                            absenceReason = if (status == AttendanceStatus.PRESENT || status == AttendanceStatus.LATE) "" else row.absenceReason
                        )
                    } else row
                }
            )
        }
    }

    fun onReasonChange(studentId: Long, reason: String) {
        _uiState.update { state ->
            state.copy(
                students = state.students.map { row ->
                    if (row.studentId == studentId) row.copy(absenceReason = reason)
                    else row
                }
            )
        }
    }

    fun onCompletedChange(studentId: Long, completed: Boolean) {
        _uiState.update { state ->
            state.copy(
                students = state.students.map { row ->
                    if (row.studentId == studentId) {
                        if (row.status == AttendanceStatus.PRESENT) row.copy(completed = completed)
                        else row
                    } else row
                }
            )
        }
    }

    fun save() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val state = _uiState.value
            for (row in state.students) {
                attendanceRepository.markAttendance(
                    lessonId = lessonId,
                    studentId = row.studentId,
                    status = row.status,
                    absenceReason = row.absenceReason.ifBlank { null },
                    completed = row.completed
                )
            }
            _uiState.update { it.copy(isSaving = false, saved = true) }
        }
    }
}
