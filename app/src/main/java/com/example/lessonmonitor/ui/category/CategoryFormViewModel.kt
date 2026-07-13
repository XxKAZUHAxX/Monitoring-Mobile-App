package com.example.lessonmonitor.ui.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lessonmonitor.data.local.entity.CategoryEntity
import com.example.lessonmonitor.domain.repository.CategoryRepository
import com.example.lessonmonitor.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryFormViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    data class UiState(
        val categoryId: Long = Routes.NEW_ID,
        val name: String = "",
        val description: String = "",
        val color: Int? = null,
        val icon: String = "",
        val errorMessage: String? = null,
        val isSubmitting: Boolean = false,
        val isLoading: Boolean = false
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var loadedCategoryId: Long? = null
    private var loadedCategory: CategoryEntity? = null

    /** Idempotent per [categoryId] so re-entering the same edit screen (e.g. on recomposition) doesn't re-fetch. */
    fun load(categoryId: Long) {
        if (categoryId == loadedCategoryId) return
        loadedCategoryId = categoryId
        if (categoryId == Routes.NEW_ID) {
            loadedCategory = null
            _uiState.value = UiState(categoryId = Routes.NEW_ID)
            return
        }
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val category = categoryRepository.getById(categoryId).first()
            loadedCategory = category
            _uiState.value = UiState(
                categoryId = categoryId,
                name = category?.name.orEmpty(),
                description = category?.description.orEmpty(),
                color = category?.color,
                icon = category?.icon.orEmpty(),
                isLoading = false
            )
        }
    }

    fun onNameChange(value: String) {
        _uiState.update { it.copy(name = value, errorMessage = null) }
    }

    fun onDescriptionChange(value: String) {
        _uiState.update { it.copy(description = value) }
    }

    fun onColorSelected(color: Int) {
        _uiState.update { it.copy(color = color) }
    }

    fun onIconChange(value: String) {
        _uiState.update { it.copy(icon = value.take(2)) }
    }

    fun submit(onSaved: () -> Unit) {
        val state = _uiState.value
        if (state.name.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Name is required") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            val description = state.description.ifBlank { null }
            val icon = state.icon.ifBlank { null }
            if (state.categoryId == Routes.NEW_ID) {
                categoryRepository.create(state.name.trim(), description, state.color, icon)
            } else {
                val base = loadedCategory
                if (base != null) {
                    categoryRepository.update(
                        base.copy(
                            name = state.name.trim(),
                            description = description,
                            color = state.color,
                            icon = icon
                        )
                    )
                }
            }
            _uiState.update { it.copy(isSubmitting = false) }
            onSaved()
        }
    }
}
