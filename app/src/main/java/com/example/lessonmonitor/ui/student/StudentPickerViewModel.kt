package com.example.lessonmonitor.ui.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lessonmonitor.data.local.entity.StudentEntity
import com.example.lessonmonitor.domain.repository.EnrollmentRepository
import com.example.lessonmonitor.domain.repository.RosterEntry
import com.example.lessonmonitor.domain.repository.StudentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StudentPickerViewModel @Inject constructor(
    private val studentRepository: StudentRepository,
    private val enrollmentRepository: EnrollmentRepository
) : ViewModel() {

    data class StudentRow(val student: StudentEntity, val enrolled: Boolean)

    enum class RosterFilter { ALL, ENROLLED, NOT_ENROLLED }

    data class UiState(
        val searchQuery: String = "",
        val students: List<StudentRow> = emptyList(),
        val filter: RosterFilter = RosterFilter.ALL,
        val quickAddName: String = "",
        val errorMessage: String? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val searchQuery = MutableStateFlow("")
    private var loadedLessonId: Long? = null

    /** All rows for the current search query, unfiltered — the source [onFilterChange] re-filters from. */
    private var allRows: List<StudentRow> = emptyList()

    /** The roster entries backing the current `enrolled` flags — needed to unenroll by [com.example.lessonmonitor.data.local.entity.EnrollmentEntity], not just studentId. */
    private var currentRoster: List<RosterEntry> = emptyList()

    /** Idempotent per [lessonId] — (re)subscribes to that lesson's roster + the (searchable) student list only when it changes. */
    fun load(lessonId: Long) {
        if (lessonId == loadedLessonId) return
        loadedLessonId = lessonId

        viewModelScope.launch {
            combine(
                searchQuery.flatMapLatest { query ->
                    if (query.isBlank()) studentRepository.getAll() else studentRepository.search(query)
                },
                enrollmentRepository.getRosterForLesson(lessonId)
            ) { students, roster ->
                currentRoster = roster
                val enrolledIds = roster.map { it.student.id }.toSet()
                students.map { student -> StudentRow(student, enrolledIds.contains(student.id)) }
            }.collect { rows ->
                allRows = rows
                _uiState.update { it.copy(students = applyFilter(rows, it.filter)) }
            }
        }
    }

    /** Client-side only (PLAN.md §1 assumption #7 "inline filter chips on Lessons/Students lists") — no new query needed. */
    fun onFilterChange(filter: RosterFilter) {
        _uiState.update { it.copy(filter = filter, students = applyFilter(allRows, filter)) }
    }

    private fun applyFilter(rows: List<StudentRow>, filter: RosterFilter): List<StudentRow> = when (filter) {
        RosterFilter.ALL -> rows
        RosterFilter.ENROLLED -> rows.filter { it.enrolled }
        RosterFilter.NOT_ENROLLED -> rows.filter { !it.enrolled }
    }

    fun onSearchQueryChange(value: String) {
        searchQuery.value = value
        _uiState.update { it.copy(searchQuery = value) }
    }

    fun onQuickAddNameChange(value: String) {
        _uiState.update { it.copy(quickAddName = value, errorMessage = null) }
    }

    fun toggleEnrollment(row: StudentRow) {
        val lessonId = loadedLessonId ?: return
        viewModelScope.launch {
            if (row.enrolled) {
                val enrollment = currentRoster.find { it.student.id == row.student.id }?.enrollment
                if (enrollment != null) enrollmentRepository.unenroll(enrollment)
            } else {
                enrollmentRepository.enroll(lessonId, row.student.id)
            }
        }
    }

    fun quickAddAndEnroll() {
        val lessonId = loadedLessonId ?: return
        val name = _uiState.value.quickAddName.trim()
        if (name.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Name is required") }
            return
        }
        viewModelScope.launch {
            val studentId = studentRepository.create(name)
            enrollmentRepository.enroll(lessonId, studentId)
            _uiState.update { it.copy(quickAddName = "", errorMessage = null) }
        }
    }
}
