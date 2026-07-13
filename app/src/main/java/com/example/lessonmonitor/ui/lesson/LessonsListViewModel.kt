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

    data class UiState(
        val lessons: List<LessonEntity> = emptyList(),
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

    /** Idempotent per [categoryId] — (re)subscribes to that category's lessons only when it changes. */
    fun load(categoryId: Long) {
        if (categoryId == loadedCategoryId) return
        loadedCategoryId = categoryId
        lessonsJob?.cancel()
        lessonsJob = viewModelScope.launch {
            lessonRepository.getAllByCategory(categoryId).collect { lessons ->
                _uiState.update { it.copy(lessons = lessons) }
            }
        }
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
