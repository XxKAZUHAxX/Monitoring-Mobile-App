package com.example.lessonmonitor.ui.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lessonmonitor.data.local.entity.StudentEntity
import com.example.lessonmonitor.domain.repository.AttendanceRepository
import com.example.lessonmonitor.domain.repository.StudentAttendanceHistoryEntry
import com.example.lessonmonitor.domain.repository.StudentDeleteImpact
import com.example.lessonmonitor.domain.repository.StudentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StudentDetailViewModel @Inject constructor(
    private val studentRepository: StudentRepository,
    private val attendanceRepository: AttendanceRepository
) : ViewModel() {

    data class UiState(
        val student: StudentEntity? = null,
        val history: List<StudentAttendanceHistoryEntry> = emptyList(),
        val pendingDelete: StudentDeleteImpact? = null,
        val deleted: Boolean = false
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var loadedStudentId: Long? = null

    /** Idempotent per [studentId] — (re)subscribes to that student's profile + attendance history only when it changes. */
    fun load(studentId: Long) {
        if (studentId == loadedStudentId) return
        loadedStudentId = studentId

        viewModelScope.launch {
            studentRepository.getById(studentId).collect { student ->
                _uiState.update { it.copy(student = student) }
            }
        }
        viewModelScope.launch {
            attendanceRepository.getHistoryForStudent(studentId).collect { history ->
                _uiState.update { it.copy(history = history) }
            }
        }
    }

    fun requestDelete() {
        val studentId = loadedStudentId ?: return
        viewModelScope.launch {
            val impact = studentRepository.getDeleteImpact(studentId)
            _uiState.update { it.copy(pendingDelete = impact) }
        }
    }

    fun confirmDelete() {
        val student = _uiState.value.student ?: return
        viewModelScope.launch {
            studentRepository.delete(student)
            _uiState.update { it.copy(pendingDelete = null, deleted = true) }
        }
    }

    fun cancelDelete() {
        _uiState.update { it.copy(pendingDelete = null) }
    }
}
