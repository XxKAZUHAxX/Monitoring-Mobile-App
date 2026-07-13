package com.example.lessonmonitor.ui.student

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.lessonmonitor.ui.components.PlaceholderScreen

@Composable
fun StudentPickerScreen(
    lessonId: Long,
    onCreateNewStudent: () -> Unit,
    onDone: () -> Unit
) {
    PlaceholderScreen(
        title = "Add Student to Lesson",
        description = "Search existing students or create a new one to enroll into lessonId = $lessonId. " +
            "Lands in the Student Profiles milestone."
    ) {
        Button(onClick = onCreateNewStudent, modifier = Modifier.fillMaxWidth()) {
            Text("Create new student")
        }
        Button(onClick = onDone, modifier = Modifier.fillMaxWidth()) {
            Text("Done (placeholder)")
        }
    }
}
