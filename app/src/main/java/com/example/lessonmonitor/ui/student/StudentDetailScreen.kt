package com.example.lessonmonitor.ui.student

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.lessonmonitor.domain.repository.StudentAttendanceHistoryEntry
import java.time.LocalDate

@Composable
fun StudentDetailScreen(
    studentId: Long,
    onDeleted: () -> Unit,
    viewModel: StudentDetailViewModel = hiltViewModel()
) {
    LaunchedEffect(studentId) { viewModel.load(studentId) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.deleted) {
        if (uiState.deleted) onDeleted()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.student?.name ?: "Student Detail") },
                actions = {
                    IconButton(onClick = viewModel::requestDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete student")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            uiState.student?.let { student ->
                Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        val photoBitmap = remember(student.photoPath) {
                            student.photoPath?.let { path -> BitmapFactory.decodeFile(path)?.asImageBitmap() }
                        }
                        if (photoBitmap != null) {
                            Image(
                                bitmap = photoBitmap,
                                contentDescription = "Student photo",
                                modifier = Modifier.size(80.dp).clip(CircleShape)
                            )
                        }
                        student.phone?.takeIf { it.isNotBlank() }?.let { Text("Phone: $it") }
                        student.email?.takeIf { it.isNotBlank() }?.let { Text("Email: $it") }
                        student.notes?.takeIf { it.isNotBlank() }?.let { Text("Notes: $it") }
                    }
                }
            }

            Text(
                "Attendance history",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            if (uiState.history.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No attendance history yet.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.history, key = { it.record.id }) { entry ->
                        HistoryRow(entry)
                    }
                }
            }
        }
    }

    uiState.pendingDelete?.let { impact ->
        AlertDialog(
            onDismissRequest = viewModel::cancelDelete,
            title = { Text("Delete \"${uiState.student?.name}\"?") },
            text = {
                Text(
                    "This will remove ${impact.enrollmentCount} roster enrollments and " +
                        "${impact.recordCount} attendance records for this student."
                )
            },
            confirmButton = {
                TextButton(onClick = viewModel::confirmDelete) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::cancelDelete) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun HistoryRow(entry: StudentAttendanceHistoryEntry) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(entry.lessonTitle, style = MaterialTheme.typography.titleSmall)
            Text(LocalDate.ofEpochDay(entry.session.sessionDate).toString(), style = MaterialTheme.typography.bodySmall)
            Text(entry.record.status.name, style = MaterialTheme.typography.bodyMedium)
            entry.record.absenceReason?.takeIf { it.isNotBlank() }?.let {
                Text("Reason: $it", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

