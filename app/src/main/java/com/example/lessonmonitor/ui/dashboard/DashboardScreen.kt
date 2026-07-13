package com.example.lessonmonitor.ui.dashboard

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.lessonmonitor.ui.components.PlaceholderScreen
import com.example.lessonmonitor.ui.theme.LessonMonitorTheme

/**
 * Real Category list + CRUD lands in the "Category & Lesson Management"
 * milestone (see PLAN.md §7). The callbacks below are wired now — with a
 * hard-coded demo id (`1L`) where a real one would normally come from a list
 * item — purely to prove the nav graph connects through to Lessons List,
 * Category Form, and Search.
 */
@Composable
fun DashboardScreen(
    onCategoryClick: (categoryId: Long) -> Unit,
    onAddCategory: () -> Unit,
    onSearchClick: () -> Unit
) {
    PlaceholderScreen(
        title = "Dashboard",
        description = "Categories will be listed here once Room and this milestone are implemented."
    ) {
        Button(onClick = { onCategoryClick(1L) }, modifier = Modifier.fillMaxWidth()) {
            Text("Open sample category's lessons")
        }
        Button(onClick = onAddCategory, modifier = Modifier.fillMaxWidth()) {
            Text("Add category")
        }
        Button(onClick = onSearchClick, modifier = Modifier.fillMaxWidth()) {
            Text("Search")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DashboardScreenPreview() {
    LessonMonitorTheme {
        DashboardScreen(onCategoryClick = {}, onAddCategory = {}, onSearchClick = {})
    }
}

