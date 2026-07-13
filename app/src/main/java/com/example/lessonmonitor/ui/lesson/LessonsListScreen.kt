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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.lessonmonitor.data.local.entity.LessonEntity

@Composable
fun LessonsListScreen(
    categoryId: Long,
    onLessonClick: (lessonId: Long) -> Unit,
    onAddLesson: () -> Unit,
    viewModel: LessonsListViewModel = hiltViewModel()
) {
    LaunchedEffect(categoryId) { viewModel.load(categoryId) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Lessons") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddLesson) {
                Icon(Icons.Default.Add, contentDescription = "Add lesson")
            }
        }
    ) { innerPadding ->
        if (uiState.lessons.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("No lessons yet. Tap + to add one.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.lessons, key = { it.id }) { lesson ->
                    LessonRow(
                        lesson = lesson,
                        onClick = { onLessonClick(lesson.id) },
                        onDeleteClick = { viewModel.requestDelete(lesson) }
                    )
                }
            }
        }
    }

    uiState.pendingDelete?.let { pending ->
        AlertDialog(
            onDismissRequest = viewModel::cancelDelete,
            title = { Text("Delete \"${pending.lesson.title}\"?") },
            text = {
                Text(
                    "This will remove ${pending.impact.enrollmentCount} roster enrollments, " +
                        "${pending.impact.sessionCount} sessions, " +
                        "${pending.impact.recordCount} attendance records."
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
private fun LessonRow(
    lesson: LessonEntity,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(lesson.title, style = MaterialTheme.typography.titleMedium)
                val subtitle = listOfNotNull(
                    lesson.facilitatorName?.takeIf { it.isNotBlank() },
                    lesson.place?.takeIf { it.isNotBlank() }
                ).joinToString(" · ")
                if (subtitle.isNotBlank()) {
                    Text(subtitle, style = MaterialTheme.typography.bodySmall)
                }
                if (lesson.isRecurring) {
                    Text(
                        "Recurring: ${lesson.recurrenceType}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            IconButton(onClick = onDeleteClick) {
                Icon(Icons.Default.Delete, contentDescription = "Delete lesson")
            }
        }
    }
}
