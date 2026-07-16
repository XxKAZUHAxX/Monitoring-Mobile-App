package com.example.lessonmonitor.ui.student

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lessonmonitor.data.local.entity.StudentEntity
import com.example.lessonmonitor.domain.repository.EnrollmentRepository
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
class StudentEnrollmentViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val studentRepository: StudentRepository,
    private val enrollmentRepository: EnrollmentRepository
) : ViewModel() {

    private val categoryId: Long = savedStateHandle.get<Long>("categoryId") ?: 0L

    data class UiState(
        val searchQuery: String = "",
        val searchResults: List<StudentEntity> = emptyList(),
        val enrolledStudentIds: Set<Long> = emptySet(),
        val isSearching: Boolean = false,
        // Create new student form
        val newName: String = "",
        val newPhone: String = "",
        val newEmail: String = "",
        val newNotes: String = "",
        val isCreating: Boolean = false,
        val created: Boolean = false
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            enrollmentRepository.getRosterForCategory(categoryId).first().let { roster ->
                _uiState.update { it.copy(enrolledStudentIds = roster.map { r -> r.student.id }.toSet()) }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        if (query.isBlank()) {
            _uiState.update { it.copy(searchResults = emptyList(), isSearching = false) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true) }
            val results = studentRepository.search(query).first()
            _uiState.update { it.copy(searchResults = results, isSearching = false) }
        }
    }

    fun enrollStudent(studentId: Long) {
        viewModelScope.launch {
            enrollmentRepository.enroll(categoryId, studentId)
            _uiState.update { it.copy(enrolledStudentIds = it.enrolledStudentIds + studentId) }
        }
    }

    fun onNewNameChange(value: String) = _uiState.update { it.copy(newName = value) }
    fun onNewPhoneChange(value: String) = _uiState.update { it.copy(newPhone = value) }
    fun onNewEmailChange(value: String) = _uiState.update { it.copy(newEmail = value) }
    fun onNewNotesChange(value: String) = _uiState.update { it.copy(newNotes = value) }

    fun createAndEnroll() {
        val state = _uiState.value
        if (state.newName.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isCreating = true) }
            val studentId = studentRepository.create(
                name = state.newName.trim(),
                phone = state.newPhone.ifBlank { null },
                email = state.newEmail.ifBlank { null },
                notes = state.newNotes.ifBlank { null }
            )
            enrollmentRepository.enroll(categoryId, studentId)
            _uiState.update {
                it.copy(
                    isCreating = false,
                    created = true,
                    enrolledStudentIds = it.enrolledStudentIds + studentId
                )
            }
        }
    }
}
