package com.example.lessonmonitor.ui.auth

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Silently reads the credential/session state and routes to the right
 * destination with no user interaction — see [SplashViewModel].
 */
@Composable
fun SplashScreen(
    onNeedsLogin: () -> Unit,
    onNeedsCredentialSetup: () -> Unit,
    onAlreadyLoggedIn: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val destination by viewModel.destination.collectAsStateWithLifecycle()

    LaunchedEffect(destination) {
        when (destination) {
            SplashViewModel.Destination.NeedsLogin -> onNeedsLogin()
            SplashViewModel.Destination.NeedsCredentialSetup -> onNeedsCredentialSetup()
            SplashViewModel.Destination.AlreadyLoggedIn -> onAlreadyLoggedIn()
            SplashViewModel.Destination.Loading -> Unit
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

