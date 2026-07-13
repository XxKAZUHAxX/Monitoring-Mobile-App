package com.example.lessonmonitor.ui.auth

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.lessonmonitor.ui.components.PlaceholderScreen

/**
 * Real behavior (built in the "User account" milestone): silently reads the
 * `isLoggedIn` DataStore flag and routes straight to [onAlreadyLoggedIn] or
 * [onNeedsLogin]/[onNeedsCredentialSetup] with no user interaction. Exposed
 * as buttons for now so all three outcomes can be exercised manually while
 * the nav graph shape is being verified.
 */
@Composable
fun SplashScreen(
    onNeedsLogin: () -> Unit,
    onNeedsCredentialSetup: () -> Unit,
    onAlreadyLoggedIn: () -> Unit
) {
    PlaceholderScreen(
        title = "Lesson Monitor",
        description = "Checking local session… (placeholder — real session check lands in the User Account milestone)"
    ) {
        Button(onClick = onNeedsCredentialSetup, modifier = Modifier.fillMaxWidth()) {
            Text("First run: create credential")
        }
        Button(onClick = onNeedsLogin, modifier = Modifier.fillMaxWidth()) {
            Text("Returning: go to login")
        }
        Button(onClick = onAlreadyLoggedIn, modifier = Modifier.fillMaxWidth()) {
            Text("Dev shortcut: already logged in")
        }
    }
}
