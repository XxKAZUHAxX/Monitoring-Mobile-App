package com.example.lessonmonitor.ui.lesson

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.lessonmonitor.data.local.entity.AttendanceSessionEntity
import com.example.lessonmonitor.domain.repository.RosterEntry
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonDetailScreen(
    lessonId: Long,
    onAddStudent: () -> Unit,
    onStudentClick: (studentId: Long) -> Unit,
    onSessionClick: (sessionId: Long) -> Unit,
    viewModel: LessonDetailViewModel = hiltViewModel()
) {
    LaunchedEffect(lessonId) { viewModel.load(lessonId) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = { TopAppBar(title = { Text(uiState.lesson?.title ?: "Lesson Detail") }) },
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(onClick = onAddStudent) {
                    Icon(Icons.Default.Add, contentDescription = "Add student to roster")
                }
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            val subtitle = listOfNotNull(
                uiState.lesson?.facilitatorName?.takeIf { it.isNotBlank() },
                uiState.lesson?.place?.takeIf { it.isNotBlank() }
            ).joinToString(" · ")
            if (subtitle.isNotBlank()) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Roster") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Sessions") })
            }

            if (selectedTab == 0) {
                RosterTab(
                    roster = uiState.roster,
                    onStudentClick = { entry -> onStudentClick(entry.student.id) },
                    onUnenrollClick = viewModel::unenroll
                )
            } else {
                SessionsTab(
                    sessions = uiState.sessions,
                    newSessionDateText = uiState.newSessionDateText,
                    errorMessage = uiState.errorMessage,
                    onNewSessionDateTextChange = viewModel::onNewSessionDateTextChange,
                    onAddSessionClick = viewModel::addSession,
                    onSessionClick = onSessionClick
                )
            }
        }
    }
}

@Composable
private fun RosterTab(
    roster: List<RosterEntry>,
    onStudentClick: (RosterEntry) -> Unit,
    onUnenrollClick: (RosterEntry) -> Unit
) {
    if (roster.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No students enrolled yet. Tap + to add one.")
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(roster, key = { it.student.id }) { entry ->
                Card(onClick = { onStudentClick(entry) }, modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(entry.student.name, modifier = Modifier.weight(1f))
                        IconButton(onClick = { onUnenrollClick(entry) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove from roster")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SessionsTab(
    sessions: List<AttendanceSessionEntity>,
    newSessionDateText: String,
    errorMessage: String?,
    onNewSessionDateTextChange: (String) -> Unit,
    onAddSessionClick: () -> Unit,
    onSessionClick: (sessionId: Long) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = newSessionDateText,
                onValueChange = onNewSessionDateTextChange,
                label = { Text("Session date (yyyy-MM-dd)") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onAddSessionClick) { Text("Add session") }
        }
        errorMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(horizontal = 16.dp))
        }
        if (sessions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No sessions yet. Add one above.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sessions.sortedByDescending { it.sessionDate }, key = { it.id }) { session ->
                    Card(onClick = { onSessionClick(session.id) }, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            LocalDate.ofEpochDay(session.sessionDate).toString(),
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}

