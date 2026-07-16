package com.example.lessonmonitor.ui.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lessonmonitor.domain.repository.AttendanceRepository
import com.example.lessonmonitor.domain.repository.AttendanceStats
import com.example.lessonmonitor.domain.repository.CategoryRepository
import com.example.lessonmonitor.domain.repository.EnrollmentRepository
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

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val studentRepository: StudentRepository,
    private val lessonRepository: LessonRepository,
    private val enrollmentRepository: EnrollmentRepository,
    private val attendanceRepository: AttendanceRepository
) : ViewModel() {

    data class CategoryOverview(
        val categoryId: Long,
        val categoryName: String,
        val studentCount: Int
    )

    data class StudentStatRow(
        val studentId: Long,
        val name: String,
        val stats: AttendanceStats,
        val completedCount: Int,
        val totalLessons: Int
    )

    data class StudentLessonRow(
        val lessonId: Long,
        val lessonTitle: String,
        val status: String,
        val completed: Boolean
    )

    data class UiState(
        val isLoading: Boolean = true,
        val categories: List<CategoryOverview> = emptyList(),
        val selectedCategoryId: Long? = null,
        val selectedCategoryName: String = "",
        val studentRows: List<StudentStatRow> = emptyList(),
        val selectedStudentId: Long? = null,
        val selectedStudentName: String = "",
        val studentLessonRows: List<StudentLessonRow> = emptyList()
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        loadCategories()
    }

    fun loadCategories() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val categories = categoryRepository.getAll().first()
            val overviews = categories.map { cat ->
                val roster = enrollmentRepository.getRosterForCategory(cat.id).first()
                CategoryOverview(cat.id, cat.name, roster.size)
            }
            _uiState.update { it.copy(isLoading = false, categories = overviews, selectedCategoryId = null) }
        }
    }

    fun selectCategory(categoryId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val category = categoryRepository.getById(categoryId).first() ?: return@launch
            val lessons = lessonRepository.getAllByCategory(categoryId).first()
            val roster = enrollmentRepository.getRosterForCategory(categoryId).first()
            val totalLessons = lessons.size

            val rows = roster.map { entry ->
                val stats = attendanceRepository.getStudentAttendanceStats(entry.student.id)
                val completedCount = lessons.count { lesson ->
                    val records = attendanceRepository.getRecordsForLesson(lesson.id).first()
                    records.any { it.studentId == entry.student.id && it.completed }
                }
                StudentStatRow(
                    studentId = entry.student.id,
                    name = entry.student.name,
                    stats = stats,
                    completedCount = completedCount,
                    totalLessons = totalLessons
                )
            }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    selectedCategoryId = categoryId,
                    selectedCategoryName = category.name,
                    studentRows = rows
                )
            }
        }
    }

    fun selectStudent(studentId: Long) {
        viewModelScope.launch {
            val categoryId = _uiState.value.selectedCategoryId ?: return@launch
            val student = studentRepository.getById(studentId).first() ?: return@launch
            val lessons = lessonRepository.getAllByCategory(categoryId).first()

            val lessonRows = lessons.map { lesson ->
                val records = attendanceRepository.getRecordsForLesson(lesson.id).first()
                val record = records.find { it.studentId == studentId }
                StudentLessonRow(
                    lessonId = lesson.id,
                    lessonTitle = lesson.title,
                    status = record?.status?.name ?: "(no record)",
                    completed = record?.completed ?: false
                )
            }

            _uiState.update {
                it.copy(selectedStudentId = studentId, selectedStudentName = student.name, studentLessonRows = lessonRows)
            }
        }
    }

    fun backToStudentStats() {
        _uiState.update { it.copy(selectedStudentId = null, studentLessonRows = emptyList()) }
    }

    fun backToCategories() {
        _uiState.update {
            it.copy(selectedCategoryId = null, selectedStudentId = null, studentRows = emptyList(), studentLessonRows = emptyList())
        }
    }
}
