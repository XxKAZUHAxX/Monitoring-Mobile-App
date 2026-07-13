package com.example.lessonmonitor.ui.attendance

import androidx.compose.runtime.Composable
import com.example.lessonmonitor.ui.components.PlaceholderScreen

@Composable
fun AttendanceSessionScreen(lessonId: Long, sessionId: Long) {
    PlaceholderScreen(
        title = "Attendance Session",
        description = "Per-student Present/Absent/Late/Excused marking (+ reason) for " +
            "lessonId = $lessonId, sessionId = $sessionId lands in the Attendance Tracking milestone. " +
            "This is also the eventual notification deep-link target (Notifications milestone)."
    )
}
