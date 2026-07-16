package com.example.lessonmonitor.ui.student

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentEnrollmentScreen(
    categoryId: Long,
    onCreateNewStudent: () -> Unit,
    onDone: () -> Unit,
    viewModel: StudentEnrollmentViewModel = hiltViewModel()
) {
    LaunchedEffect(categoryId) { viewModel.load() }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.created) {
        if (uiState.created) onDone()
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Add Student to Category") }) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Search existing students
            Text("Search existing students", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                label = { Text("Search by name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            if (uiState.isSearching) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else if (uiState.searchResults.isNotEmpty()) {
                uiState.searchResults.forEach { student ->
                    val alreadyEnrolled = student.id in uiState.enrolledStudentIds
                    Card(
                        onClick = { if (!alreadyEnrolled) viewModel.enrollStudent(student.id) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                            Text(student.name, style = MaterialTheme.typography.titleSmall)
                            if (alreadyEnrolled) {
                                Text("Already enrolled", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                            } else {
                                Text("Tap to enroll", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Create new student
            Text("Create new student", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = uiState.newName,
                onValueChange = viewModel::onNewNameChange,
                label = { Text("Name *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = uiState.newPhone,
                onValueChange = viewModel::onNewPhoneChange,
                label = { Text("Phone (optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = uiState.newEmail,
                onValueChange = viewModel::onNewEmailChange,
                label = { Text("Email (optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = uiState.newNotes,
                onValueChange = viewModel::onNewNotesChange,
                label = { Text("Notes (optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = viewModel::createAndEnroll,
                enabled = uiState.newName.isNotBlank() && !uiState.isCreating,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (uiState.isCreating) "Creating…" else "Create & Enroll")
            }
        }
    }
}
