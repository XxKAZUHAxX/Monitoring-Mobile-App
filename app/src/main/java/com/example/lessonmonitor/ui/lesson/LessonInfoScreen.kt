package com.example.lessonmonitor.ui.lesson

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonInfoScreen(
    lessonId: Long,
    onEditClick: () -> Unit,
    viewModel: LessonInfoViewModel = hiltViewModel()
) {
    LaunchedEffect(lessonId) { viewModel.load() }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lesson = uiState.lesson

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lesson Info") },
                actions = {
                    IconButton(onClick = onEditClick) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit lesson")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            lesson?.let {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(it.title, style = MaterialTheme.typography.headlineSmall)
                        it.description?.takeIf { desc -> desc.isNotBlank() }?.let { desc ->
                            Text(desc, style = MaterialTheme.typography.bodyLarge)
                        }
                        it.facilitatorName?.takeIf { f -> f.isNotBlank() }?.let { f ->
                            FieldRow("Facilitator", f)
                        }
                        it.place?.takeIf { p -> p.isNotBlank() }?.let { p ->
                            FieldRow("Place", p)
                        }
                        FieldRow("Date", uiState.lessonDate)
                        if (uiState.lessonTime.isNotBlank()) {
                            FieldRow("Time", uiState.lessonTime)
                        }
                    }
                }
            } ?: run {
                Text("Lesson not found.", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
private fun FieldRow(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}
