package com.example.lessonmonitor.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lessonmonitor.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    data class UiState(
        val password: String = "",
        val errorMessage: String? = null,
        val isSubmitting: Boolean = false,
        val biometricEnabled: Boolean = false
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.isBiometricEnabled().collect { enabled ->
                _uiState.update { it.copy(biometricEnabled = enabled) }
            }
        }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, errorMessage = null) }
    }

    fun submit(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            val valid = authRepository.verifyPassword(_uiState.value.password)
            if (valid) {
                authRepository.setLoggedIn(true)
                _uiState.update { it.copy(isSubmitting = false) }
                onSuccess()
            } else {
                _uiState.update { it.copy(isSubmitting = false, errorMessage = "Incorrect password") }
            }
        }
    }

    fun onBiometricSuccess(onSuccess: () -> Unit) {
        viewModelScope.launch {
            authRepository.setLoggedIn(true)
            onSuccess()
        }
    }
}
