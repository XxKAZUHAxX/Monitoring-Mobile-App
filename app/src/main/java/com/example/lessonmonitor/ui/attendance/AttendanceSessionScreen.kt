package com.example.lessonmonitor.ui.attendance

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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.example.lessonmonitor.data.local.entity.AttendanceStatus
import com.example.lessonmonitor.ui.attendance.AttendanceSessionViewModel.RosterRowState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceSessionScreen(
    lessonId: Long,
    sessionId: Long,
    viewModel: AttendanceSessionViewModel = hiltViewModel()
) {
    LaunchedEffect(lessonId, sessionId) { viewModel.load(lessonId, sessionId) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.sessionDateText.ifBlank { "Attendance Session" }) }
            )
        }
    ) { innerPadding ->
        if (uiState.rows.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(if (uiState.isLoading) "Loading…" else "No students enrolled in this lesson yet.")
            }
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.rows, key = { it.studentId }) { row ->
                        AttendanceRow(
                            row = row,
                            onStatusChange = { status -> viewModel.onStatusChange(row.studentId, status) },
                            onReasonChange = { reason -> viewModel.onReasonChange(row.studentId, reason) }
                        )
                    }
                }
                Button(
                    onClick = { viewModel.submit {} },
                    enabled = !uiState.isSubmitting,
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) {
                    Text(if (uiState.isSubmitting) "Saving…" else "Save attendance")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AttendanceRow(
    row: RosterRowState,
    onStatusChange: (AttendanceStatus) -> Unit,
    onReasonChange: (String) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(row.studentName, style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                AttendanceStatus.values().forEach { status ->
                    FilterChip(
                        selected = row.status == status,
                        onClick = { onStatusChange(status) },
                        label = { Text(status.name.take(4)) }
                    )
                }
            }
            if (row.status == AttendanceStatus.ABSENT || row.status == AttendanceStatus.EXCUSED) {
                OutlinedTextField(
                    value = row.reason,
                    onValueChange = onReasonChange,
                    label = { Text("Reason (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
            }
        }
    }
}

