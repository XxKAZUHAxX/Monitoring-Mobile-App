package com.example.lessonmonitor.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lessonmonitor.data.local.entity.CategoryEntity
import com.example.lessonmonitor.data.local.entity.LessonEntity
import com.example.lessonmonitor.data.local.entity.StudentEntity
import com.example.lessonmonitor.domain.repository.CategoryRepository
import com.example.lessonmonitor.domain.repository.LessonRepository
import com.example.lessonmonitor.domain.repository.StudentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Feature: Search & filter (PLAN.md §7 milestone 11), global search across categories/lessons/students by name. */
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val lessonRepository: LessonRepository,
    private val studentRepository: StudentRepository
) : ViewModel() {

    data class UiState(
        val query: String = "",
        val categoryResults: List<CategoryEntity> = emptyList(),
        val lessonResults: List<LessonEntity> = emptyList(),
        val studentResults: List<StudentEntity> = emptyList()
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun onQueryChange(query: String) {
        searchJob?.cancel()
        if (query.isBlank()) {
            _uiState.update {
                it.copy(query = query, categoryResults = emptyList(), lessonResults = emptyList(), studentResults = emptyList())
            }
            return
        }
        _uiState.update { it.copy(query = query) }
        searchJob = viewModelScope.launch {
            combine(
                categoryRepository.search(query),
                lessonRepository.search(query),
                studentRepository.search(query)
            ) { categories, lessons, students -> Triple(categories, lessons, students) }
                .collect { (categories, lessons, students) ->
                    _uiState.update {
                        it.copy(categoryResults = categories, lessonResults = lessons, studentResults = students)
                    }
                }
        }
    }
}
