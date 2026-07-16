package com.example.lessonmonitor.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lessonmonitor.data.local.entity.CategoryEntity
import com.example.lessonmonitor.domain.repository.CategoryDeleteImpact
import com.example.lessonmonitor.domain.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    data class UiState(
        val categories: List<CategoryEntity> = emptyList(),
        val pendingDelete: PendingDelete? = null
    )

    data class PendingDelete(
        val category: CategoryEntity,
        val impact: CategoryDeleteImpact
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            categoryRepository.getAll().collect { categories ->
                _uiState.update { it.copy(categories = categories) }
            }
        }
    }

    fun requestDelete(category: CategoryEntity) {
        viewModelScope.launch {
            val impact = categoryRepository.getDeleteImpact(category.id)
            _uiState.update { it.copy(pendingDelete = PendingDelete(category, impact)) }
        }
    }

    fun confirmDelete() {
        val target = _uiState.value.pendingDelete ?: return
        viewModelScope.launch {
            categoryRepository.delete(target.category)
            _uiState.update { it.copy(pendingDelete = null) }
        }
    }

    fun cancelDelete() {
        _uiState.update { it.copy(pendingDelete = null) }
    }

    fun reorderCategories(orderedIds: List<Long>) {
        viewModelScope.launch {
            categoryRepository.reorderCategories(orderedIds)
        }
    }
}
