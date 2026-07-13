package com.example.lessonmonitor.ui.export

import androidx.compose.runtime.Composable
import com.example.lessonmonitor.ui.components.PlaceholderScreen

@Composable
fun ExportScreen() {
    PlaceholderScreen(
        title = "Export",
        description = "Pick a Category/Lesson scope and export attendance to CSV via the Android " +
            "share sheet — lands in the Export/Backup milestone."
    )
}
