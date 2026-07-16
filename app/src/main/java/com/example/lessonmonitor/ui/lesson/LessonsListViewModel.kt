package com.example.lessonmonitor.ui.lesson

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lessonmonitor.data.local.entity.LessonEntity
import com.example.lessonmonitor.domain.repository.EnrollmentRepository
import com.example.lessonmonitor.domain.repository.LessonDeleteImpact
import com.example.lessonmonitor.domain.repository.LessonRepository
import com.example.lessonmonitor.domain.repository.RosterEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LessonsListViewModel @Inject constructor(
    private val lessonRepository: LessonRepository,
    private val enrollmentRepository: EnrollmentRepository
) : ViewModel() {

    enum class Tab { LESSONS, STUDENTS }

    data class UiState(
        val selectedTab: Tab = Tab.LESSONS,
        val lessons: List<LessonEntity> = emptyList(),
        val students: List<RosterEntry> = emptyList(),
        val pendingDelete: PendingDelete? = null,
        val pendingUnenroll: RosterEntry? = null
    )

    data class PendingDelete(
        val lesson: LessonEntity,
        val impact: LessonDeleteImpact
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var loadedCategoryId: Long? = null
    private var lessonsJob: Job? = null
    private var studentsJob: Job? = null

    fun load(categoryId: Long) {
        if (categoryId == loadedCategoryId) return
        loadedCategoryId = categoryId

        lessonsJob?.cancel()
        lessonsJob = viewModelScope.launch {
            lessonRepository.getAllByCategory(categoryId).collect { lessons ->
                _uiState.update { it.copy(lessons = lessons) }
            }
        }

        studentsJob?.cancel()
        studentsJob = viewModelScope.launch {
            enrollmentRepository.getRosterForCategory(categoryId).collect { roster ->
                _uiState.update { it.copy(students = roster) }
            }
        }
    }

    fun onTabSelected(tab: Tab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun requestDelete(lesson: LessonEntity) {
        viewModelScope.launch {
            val impact = lessonRepository.getDeleteImpact(lesson.id)
            _uiState.update { it.copy(pendingDelete = PendingDelete(lesson, impact)) }
        }
    }

    fun confirmDelete() {
        val target = _uiState.value.pendingDelete ?: return
        viewModelScope.launch {
            lessonRepository.delete(target.lesson)
            _uiState.update { it.copy(pendingDelete = null) }
        }
    }

    fun cancelDelete() {
        _uiState.update { it.copy(pendingDelete = null) }
    }

    fun requestUnenroll(entry: RosterEntry) {
        _uiState.update { it.copy(pendingUnenroll = entry) }
    }

    fun confirmUnenroll() {
        val target = _uiState.value.pendingUnenroll ?: return
        viewModelScope.launch {
            enrollmentRepository.unenroll(target.enrollment)
            _uiState.update { it.copy(pendingUnenroll = null) }
        }
    }

    fun cancelUnenroll() {
        _uiState.update { it.copy(pendingUnenroll = null) }
    }

    fun reorderLessons(orderedIds: List<Long>) {
        val categoryId = loadedCategoryId ?: return
        viewModelScope.launch {
            lessonRepository.reorderLessons(categoryId, orderedIds)
        }
    }
}
