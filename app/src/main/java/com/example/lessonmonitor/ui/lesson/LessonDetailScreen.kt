package com.example.lessonmonitor.ui.lesson

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.lessonmonitor.ui.components.PlaceholderScreen

@Composable
fun LessonDetailScreen(
    lessonId: Long,
    onAddStudent: () -> Unit,
    onStudentClick: (studentId: Long) -> Unit,
    onSessionClick: (sessionId: Long) -> Unit
) {
    PlaceholderScreen(
        title = "Lesson Detail",
        description = "Facilitator/place, roster tab, and sessions tab for lessonId = $lessonId " +
            "land in the Attendance Tracking and Student Profiles milestones."
    ) {
        Button(onClick = onAddStudent, modifier = Modifier.fillMaxWidth()) {
            Text("Add student to roster")
        }
        Button(onClick = { onStudentClick(1L) }, modifier = Modifier.fillMaxWidth()) {
            Text("Open sample student")
        }
        Button(onClick = { onSessionClick(1L) }, modifier = Modifier.fillMaxWidth()) {
            Text("Open sample attendance session")
        }
    }
}
