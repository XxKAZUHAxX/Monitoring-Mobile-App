package com.example.lessonmonitor.ui.auth

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.lessonmonitor.ui.components.PlaceholderScreen

@Composable
fun CreateCredentialScreen(onCredentialCreated: () -> Unit) {
    PlaceholderScreen(
        title = "Create Credential",
        description = "PIN/password setup form + PBKDF2 hashing (stored in an encrypted DataStore) " +
            "lands in the User Account milestone."
    ) {
        Button(onClick = onCredentialCreated, modifier = Modifier.fillMaxWidth()) {
            Text("Create & continue")
        }
    }
}
