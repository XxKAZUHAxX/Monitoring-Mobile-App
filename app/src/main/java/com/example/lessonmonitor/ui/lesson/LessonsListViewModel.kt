package com.example.lessonmonitor.ui.lesson

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lessonmonitor.data.local.entity.LessonEntity
import com.example.lessonmonitor.domain.repository.LessonDeleteImpact
import com.example.lessonmonitor.domain.repository.LessonRepository
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
    private val lessonRepository: LessonRepository
) : ViewModel() {

    enum class LessonFilter { ALL, RECURRING, ONE_OFF }

    data class UiState(
        val lessons: List<LessonEntity> = emptyList(),
        val filter: LessonFilter = LessonFilter.ALL,
        val pendingDelete: PendingDelete? = null
    )

    /** Populated once the exact cascade-delete impact counts have been fetched (PLAN.md §1 assumption #3). */
    data class PendingDelete(
        val lesson: LessonEntity,
        val impact: LessonDeleteImpact
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var loadedCategoryId: Long? = null
    private var lessonsJob: Job? = null

    /** All lessons for the loaded category, unfiltered — the source [onFilterChange] re-filters from. */
    private var allLessons: List<LessonEntity> = emptyList()

    /** Idempotent per [categoryId] — (re)subscribes to that category's lessons only when it changes. */
    fun load(categoryId: Long) {
        if (categoryId == loadedCategoryId) return
        loadedCategoryId = categoryId
        lessonsJob?.cancel()
        lessonsJob = viewModelScope.launch {
            lessonRepository.getAllByCategory(categoryId).collect { lessons ->
                allLessons = lessons
                _uiState.update { it.copy(lessons = applyFilter(lessons, it.filter)) }
            }
        }
    }

    /** Client-side only (PLAN.md §1 assumption #7 "inline filter chips on Lessons/Students lists") — no new query needed. */
    fun onFilterChange(filter: LessonFilter) {
        _uiState.update { it.copy(filter = filter, lessons = applyFilter(allLessons, filter)) }
    }

    private fun applyFilter(lessons: List<LessonEntity>, filter: LessonFilter): List<LessonEntity> = when (filter) {
        LessonFilter.ALL -> lessons
        LessonFilter.RECURRING -> lessons.filter { it.isRecurring }
        LessonFilter.ONE_OFF -> lessons.filter { !it.isRecurring }
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
}
