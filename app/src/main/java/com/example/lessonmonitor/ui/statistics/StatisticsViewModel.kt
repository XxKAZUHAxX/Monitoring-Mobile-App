package com.example.lessonmonitor.ui.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lessonmonitor.domain.repository.AttendanceRepository
import com.example.lessonmonitor.domain.repository.AttendanceStats
import com.example.lessonmonitor.domain.repository.LessonRepository
import com.example.lessonmonitor.domain.repository.StudentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Attendance Statistics Dashboard (PLAN.md §7 milestone 12, prompt §H):
 * per-student and per-lesson attendance percentages. Stats are computed
 * once per [refresh] call (load-once pattern, like [com.example.lessonmonitor.domain.repository.LessonDeleteImpact])
 * rather than as a live-recomputing `Flow`, since [com.example.lessonmonitor.data.local.dao.AttendanceRecordDao]'s
 * count queries are plain suspend functions, not reactive ones.
 */
@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val studentRepository: StudentRepository,
    private val lessonRepository: LessonRepository,
    private val attendanceRepository: AttendanceRepository
) : ViewModel() {

    enum class Tab { STUDENTS, LESSONS }

    data class StudentStatRow(val studentId: Long, val name: String, val stats: AttendanceStats)
    data class LessonStatRow(val lessonId: Long, val title: String, val stats: AttendanceStats)

    data class UiState(
        val isLoading: Boolean = true,
        val selectedTab: Tab = Tab.STUDENTS,
        val studentRows: List<StudentStatRow> = emptyList(),
        val lessonRows: List<LessonStatRow> = emptyList()
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun onTabSelected(tab: Tab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val students = studentRepository.getAll().first()
            val studentRows = students.map { student ->
                StudentStatRow(
                    studentId = student.id,
                    name = student.name,
                    stats = attendanceRepository.getStudentAttendanceStats(student.id)
                )
            }

            val lessons = lessonRepository.getAll().first()
            val lessonRows = lessons.map { lesson ->
                LessonStatRow(
                    lessonId = lesson.id,
                    title = lesson.title,
                    stats = attendanceRepository.getLessonAttendanceStats(lesson.id)
                )
            }

            _uiState.update {
                it.copy(isLoading = false, studentRows = studentRows, lessonRows = lessonRows)
            }
        }
    }
}
