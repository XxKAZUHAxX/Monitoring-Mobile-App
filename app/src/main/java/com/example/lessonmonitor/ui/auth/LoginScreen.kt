package com.example.lessonmonitor.ui.auth

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.lessonmonitor.ui.components.PlaceholderScreen

@Composable
fun LoginScreen(onLoggedIn: () -> Unit) {
    PlaceholderScreen(
        title = "Log In",
        description = "PIN/password entry + optional biometric prompt lands in the User Account milestone."
    ) {
        Button(onClick = onLoggedIn, modifier = Modifier.fillMaxWidth()) {
            Text("Log in")
        }
    }
}
