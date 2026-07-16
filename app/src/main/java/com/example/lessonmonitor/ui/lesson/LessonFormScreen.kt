package com.example.lessonmonitor.ui.lesson

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.example.lessonmonitor.navigation.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonFormScreen(
    categoryId: Long,
    lessonId: Long,
    onDone: () -> Unit,
    viewModel: LessonFormViewModel = hiltViewModel()
) {
    LaunchedEffect(categoryId, lessonId) { viewModel.load(categoryId, lessonId) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isNew = lessonId == Routes.NEW_ID

    Scaffold(topBar = { TopAppBar(title = { Text(if (isNew) "Add Lesson" else "Edit Lesson") }) }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = uiState.title,
                onValueChange = viewModel::onTitleChange,
                label = { Text("Title") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = uiState.description,
                onValueChange = viewModel::onDescriptionChange,
                label = { Text("Description (optional)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = uiState.facilitatorName,
                onValueChange = viewModel::onFacilitatorNameChange,
                label = { Text("Facilitator (optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = uiState.place,
                onValueChange = viewModel::onPlaceChange,
                label = { Text("Place (optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = uiState.startDateText,
                onValueChange = viewModel::onStartDateTextChange,
                label = { Text("Date (yyyy-MM-dd)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = uiState.startTimeText,
                onValueChange = viewModel::onStartTimeTextChange,
                label = { Text("Start time HH:mm (optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            uiState.errorMessage?.let { message ->
                Text(text = message, color = MaterialTheme.colorScheme.error)
            }

            Button(
                onClick = { viewModel.submit(onDone) },
                enabled = !uiState.isSubmitting,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (uiState.isSubmitting) "Saving…" else "Save")
            }
        }
    }
}
