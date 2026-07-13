package com.example.lessonmonitor.ui.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lessonmonitor.data.local.entity.StudentEntity
import com.example.lessonmonitor.domain.repository.StudentRepository
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
class StudentFormViewModel @Inject constructor(
    private val studentRepository: StudentRepository
) : ViewModel() {

    data class UiState(
        val studentId: Long = Routes.NEW_ID,
        val name: String = "",
        val phone: String = "",
        val email: String = "",
        val notes: String = "",
        val photoPath: String? = null,
        val errorMessage: String? = null,
        val isSubmitting: Boolean = false
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var loadedStudentId: Long? = null
    private var loadedStudent: StudentEntity? = null

    /** Idempotent per [studentId] so re-entering the same edit screen doesn't re-fetch. */
    fun load(studentId: Long) {
        if (studentId == loadedStudentId) return
        loadedStudentId = studentId
        if (studentId == Routes.NEW_ID) {
            loadedStudent = null
            _uiState.value = UiState(studentId = Routes.NEW_ID)
            return
        }
        viewModelScope.launch {
            val student = studentRepository.getById(studentId).first()
            loadedStudent = student
            _uiState.value = UiState(
                studentId = studentId,
                name = student?.name.orEmpty(),
                phone = student?.phone.orEmpty(),
                email = student?.email.orEmpty(),
                notes = student?.notes.orEmpty(),
                photoPath = student?.photoPath
            )
        }
    }

    fun onNameChange(value: String) = _uiState.update { it.copy(name = value, errorMessage = null) }
    fun onPhoneChange(value: String) = _uiState.update { it.copy(phone = value) }
    fun onEmailChange(value: String) = _uiState.update { it.copy(email = value) }
    fun onNotesChange(value: String) = _uiState.update { it.copy(notes = value) }
    fun onPhotoPathChange(path: String?) = _uiState.update { it.copy(photoPath = path) }

    fun submit(onSaved: () -> Unit) {
        val state = _uiState.value
        if (state.name.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Name is required") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            val phone = state.phone.ifBlank { null }
            val email = state.email.ifBlank { null }
            val notes = state.notes.ifBlank { null }
            if (state.studentId == Routes.NEW_ID) {
                studentRepository.create(
                    name = state.name.trim(),
                    phone = phone,
                    email = email,
                    notes = notes,
                    photoPath = state.photoPath
                )
            } else {
                val base = loadedStudent
                if (base != null) {
                    studentRepository.update(
                        base.copy(
                            name = state.name.trim(),
                            phone = phone,
                            email = email,
                            notes = notes,
                            photoPath = state.photoPath
                        )
                    )
                }
            }
            _uiState.update { it.copy(isSubmitting = false) }
            onSaved()
        }
    }
}
