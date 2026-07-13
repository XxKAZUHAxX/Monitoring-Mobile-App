package com.example.lessonmonitor.ui.student

import androidx.compose.runtime.Composable
import com.example.lessonmonitor.ui.components.PlaceholderScreen

@Composable
fun StudentDetailScreen(studentId: Long) {
    PlaceholderScreen(
        title = "Student Detail",
        description = "Profile + cross-lesson attendance history for studentId = $studentId " +
            "lands in the Student Profiles milestone."
    )
}
