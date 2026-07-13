package com.example.lessonmonitor.ui.calendar

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.lessonmonitor.ui.components.PlaceholderScreen
import java.time.LocalDate

@Composable
fun DayAgendaScreen(
    epochDay: Long,
    onSessionClick: (lessonId: Long, sessionId: Long) -> Unit
) {
    val date = LocalDate.ofEpochDay(epochDay)
    PlaceholderScreen(
        title = "Agenda — $date",
        description = "Sessions occurring on $date land here in the Calendar/Schedule View milestone."
    ) {
        Button(onClick = { onSessionClick(1L, 1L) }, modifier = Modifier.fillMaxWidth()) {
            Text("Open sample session")
        }
    }
}
