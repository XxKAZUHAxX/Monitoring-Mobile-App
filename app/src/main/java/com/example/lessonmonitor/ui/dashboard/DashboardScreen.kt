package com.example.lessonmonitor.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.lessonmonitor.ui.theme.LessonMonitorTheme

/**
 * Temporary placeholder for the Dashboard (Categories list) screen. Replaced
 * with the real Category list + CRUD UI in the "Category & Lesson Management"
 * milestone (see PLAN.md §7). Exists now purely to prove the Application ->
 * MainActivity -> Theme -> NavHost -> Screen scaffold wires up end to end.
 */
@Composable
fun DashboardScreen() {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Lesson Monitor") }) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Project scaffold ready.",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Categories will appear here once Room and the Category/Lesson " +
                    "management milestone are implemented.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DashboardScreenPreview() {
    LessonMonitorTheme {
        DashboardScreen()
    }
}
