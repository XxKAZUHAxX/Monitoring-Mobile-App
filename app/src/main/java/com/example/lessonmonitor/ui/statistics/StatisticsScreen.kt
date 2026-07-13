package com.example.lessonmonitor.ui.statistics

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.lessonmonitor.ui.components.PlaceholderScreen

@Composable
fun StatisticsScreen(onStudentClick: (studentId: Long) -> Unit) {
    PlaceholderScreen(
        title = "Statistics",
        description = "Per-student and per-lesson attendance percentages + charts land in the " +
            "Attendance Statistics Dashboard milestone."
    ) {
        Button(onClick = { onStudentClick(1L) }, modifier = Modifier.fillMaxWidth()) {
            Text("Open sample student")
        }
    }
}
