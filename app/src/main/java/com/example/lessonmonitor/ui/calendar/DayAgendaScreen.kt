package com.example.lessonmonitor.ui.calendar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.lessonmonitor.domain.repository.CalendarSessionEntry
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayAgendaScreen(
    epochDay: Long,
    onSessionClick: (lessonId: Long, sessionId: Long) -> Unit,
    viewModel: DayAgendaViewModel = hiltViewModel()
) {
    LaunchedEffect(epochDay) { viewModel.load(epochDay) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val date = LocalDate.ofEpochDay(epochDay)

    Scaffold(
        topBar = { TopAppBar(title = { Text(date.format(DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy"))) }) }
    ) { innerPadding ->
        if (uiState.sessions.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("No sessions on this day.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.sessions, key = { it.session.id }) { entry ->
                    SessionRow(entry = entry, onClick = { onSessionClick(entry.session.lessonId, entry.session.id) })
                }
            }
        }
    }
}

@Composable
private fun SessionRow(entry: CalendarSessionEntry, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(entry.lessonTitle, style = MaterialTheme.typography.titleMedium)
            val subtitle = listOfNotNull(
                entry.session.facilitatorOverride?.takeIf { it.isNotBlank() },
                entry.session.placeOverride?.takeIf { it.isNotBlank() }
            ).joinToString(" · ")
            if (subtitle.isNotBlank()) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
