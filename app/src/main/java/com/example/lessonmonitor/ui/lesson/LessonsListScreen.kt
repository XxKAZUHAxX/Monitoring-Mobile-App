package com.example.lessonmonitor.ui.lesson

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.lessonmonitor.ui.components.PlaceholderScreen

@Composable
fun LessonsListScreen(
    categoryId: Long,
    onLessonClick: (lessonId: Long) -> Unit,
    onAddLesson: () -> Unit
) {
    PlaceholderScreen(
        title = "Lessons",
        description = "Lessons for categoryId = $categoryId will be listed here once the " +
            "Category & Lesson Management milestone is implemented."
    ) {
        Button(onClick = { onLessonClick(1L) }, modifier = Modifier.fillMaxWidth()) {
            Text("Open sample lesson")
        }
        Button(onClick = onAddLesson, modifier = Modifier.fillMaxWidth()) {
            Text("Add lesson")
        }
    }
}
