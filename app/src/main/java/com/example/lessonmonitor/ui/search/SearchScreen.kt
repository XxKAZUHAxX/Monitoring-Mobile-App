package com.example.lessonmonitor.ui.search

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.lessonmonitor.ui.components.PlaceholderScreen

@Composable
fun SearchScreen(
    onCategoryResultClick: (categoryId: Long) -> Unit,
    onLessonResultClick: (lessonId: Long) -> Unit,
    onStudentResultClick: (studentId: Long) -> Unit
) {
    PlaceholderScreen(
        title = "Search",
        description = "Global search across categories, lessons, and students lands in the Search & Filter milestone."
    ) {
        Button(onClick = { onCategoryResultClick(1L) }, modifier = Modifier.fillMaxWidth()) {
            Text("Sample category result")
        }
        Button(onClick = { onLessonResultClick(1L) }, modifier = Modifier.fillMaxWidth()) {
            Text("Sample lesson result")
        }
        Button(onClick = { onStudentResultClick(1L) }, modifier = Modifier.fillMaxWidth()) {
            Text("Sample student result")
        }
    }
}
