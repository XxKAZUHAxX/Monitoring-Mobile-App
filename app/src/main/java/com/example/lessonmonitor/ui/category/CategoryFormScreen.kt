package com.example.lessonmonitor.ui.category

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.lessonmonitor.navigation.Routes
import com.example.lessonmonitor.ui.components.PlaceholderScreen

@Composable
fun CategoryFormScreen(categoryId: Long, onDone: () -> Unit) {
    val isNew = categoryId == Routes.NEW_ID
    PlaceholderScreen(
        title = if (isNew) "Add Category" else "Edit Category",
        description = "Name/description/color/icon form lands in the Category & Lesson Management " +
            "milestone. categoryId = $categoryId"
    ) {
        Button(onClick = onDone, modifier = Modifier.fillMaxWidth()) {
            Text("Save (placeholder)")
        }
    }
}
