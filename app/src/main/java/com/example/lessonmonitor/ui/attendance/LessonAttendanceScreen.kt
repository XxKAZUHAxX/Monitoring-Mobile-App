package com.example.lessonmonitor.ui.attendance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonAttendanceScreen(
    lessonId: Long,
    onInfoClick: (lessonId: Long) -> Unit = {},
    viewModel: LessonAttendanceViewModel = hiltViewModel()
) {
    LaunchedEffect(lessonId) { viewModel.load() }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${uiState.lessonTitle} — ${uiState.lessonDate}") },
                actions = {
                    IconButton(onClick = { onInfoClick(lessonId) }) {
                        Icon(Icons.Default.Info, contentDescription = "Lesson info")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (uiState.students.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("No students enrolled in this category.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.students, key = { it.studentId }) { row ->
                    AttendanceRow(
                        studentName = row.studentName,
                        status = row.status,
                        absenceReason = row.absenceReason,
                        completed = row.completed,
                        onStatusChange = { viewModel.onStatusChange(row.studentId, it) },
                        onReasonChange = { viewModel.onReasonChange(row.studentId, it) },
                        onCompletedChange = { viewModel.onCompletedChange(row.studentId, it) }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = viewModel::save,
                        enabled = !uiState.isSaving,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (uiState.isSaving) "Saving…" else "Save Attendance")
                    }
                }
            }
        }
    }
}

@Composable
private fun AttendanceRow(
    studentName: String,
    status: AttendanceStatus,
    absenceReason: String,
    completed: Boolean,
    onStatusChange: (AttendanceStatus) -> Unit,
    onReasonChange: (String) -> Unit,
    onCompletedChange: (Boolean) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(studentName, style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            AttendanceStatus.values().forEach { chipStatus ->
                FilterChip(
                    selected = status == chipStatus,
                    onClick = { onStatusChange(chipStatus) },
                    label = { Text(chipStatus.name) }
                )
            }
        }
        if (status == AttendanceStatus.ABSENT || status == AttendanceStatus.EXCUSED) {
            OutlinedTextField(
                value = absenceReason,
                onValueChange = onReasonChange,
                label = { Text("Reason") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = completed,
                onCheckedChange = onCompletedChange,
                enabled = status == AttendanceStatus.PRESENT
            )
            Text(
                "Completed",
                style = MaterialTheme.typography.bodyMedium,
                color = if (status == AttendanceStatus.PRESENT) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                }
            )
        }
    }
}
