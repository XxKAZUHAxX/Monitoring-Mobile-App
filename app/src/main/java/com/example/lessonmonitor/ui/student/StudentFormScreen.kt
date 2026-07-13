package com.example.lessonmonitor.ui.student

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.lessonmonitor.navigation.Routes
import com.example.lessonmonitor.ui.components.PlaceholderScreen

@Composable
fun StudentFormScreen(studentId: Long, onDone: () -> Unit) {
    val isNew = studentId == Routes.NEW_ID
    PlaceholderScreen(
        title = if (isNew) "Add Student" else "Edit Student",
        description = "Name/photo/contact/notes form lands in the Student Profiles milestone. studentId = $studentId"
    ) {
        Button(onClick = onDone, modifier = Modifier.fillMaxWidth()) {
            Text("Save (placeholder)")
        }
    }
}
