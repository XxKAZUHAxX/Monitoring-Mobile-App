package com.example.lessonmonitor.ui.lesson

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.lessonmonitor.navigation.Routes
import com.example.lessonmonitor.ui.components.PlaceholderScreen

@Composable
fun LessonFormScreen(categoryId: Long, lessonId: Long, onDone: () -> Unit) {
    val isNew = lessonId == Routes.NEW_ID
    PlaceholderScreen(
        title = if (isNew) "Add Lesson" else "Edit Lesson",
        description = "Title/description/facilitator/place + recurrence config lands in the " +
            "Category & Lesson Management and Recurring Lessons milestones. " +
            "categoryId = $categoryId, lessonId = $lessonId"
    ) {
        Button(onClick = onDone, modifier = Modifier.fillMaxWidth()) {
            Text("Save (placeholder)")
        }
    }
}
