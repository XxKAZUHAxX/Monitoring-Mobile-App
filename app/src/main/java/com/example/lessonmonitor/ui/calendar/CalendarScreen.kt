package com.example.lessonmonitor.ui.calendar

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.lessonmonitor.ui.components.PlaceholderScreen
import java.time.LocalDate

@Composable
fun CalendarScreen(onDayClick: (epochDay: Long) -> Unit) {
    PlaceholderScreen(
        title = "Calendar",
        description = "Month/week grid of attendance sessions lands in the Calendar/Schedule View milestone."
    ) {
        Button(onClick = { onDayClick(LocalDate.now().toEpochDay()) }, modifier = Modifier.fillMaxWidth()) {
            Text("Open today's agenda")
        }
    }
}
