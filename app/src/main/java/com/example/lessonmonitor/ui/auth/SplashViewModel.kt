package com.example.lessonmonitor.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lessonmonitor.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    /** Where Splash should route to; [Loading] means "stay on Splash a moment longer". */
    sealed interface Destination {
        data object Loading : Destination
        data object NeedsCredentialSetup : Destination
        data object NeedsLogin : Destination
        data object AlreadyLoggedIn : Destination
    }

    private val _destination = MutableStateFlow<Destination>(Destination.Loading)
    val destination: StateFlow<Destination> = _destination.asStateFlow()

    init {
        viewModelScope.launch {
            _destination.value = if (!authRepository.hasCredential()) {
                Destination.NeedsCredentialSetup
            } else if (authRepository.isLoggedIn().first()) {
                Destination.AlreadyLoggedIn
            } else {
                Destination.NeedsLogin
            }
        }
    }
}
