package com.example.lessonmonitor.ui.student

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
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentPickerScreen(
    lessonId: Long,
    onCreateNewStudent: () -> Unit,
    onDone: () -> Unit,
    viewModel: StudentPickerViewModel = hiltViewModel()
) {
    LaunchedEffect(lessonId) { viewModel.load(lessonId) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(topBar = { TopAppBar(title = { Text("Add Student to Lesson") }) }) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                label = { Text("Search students") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = uiState.filter == StudentPickerViewModel.RosterFilter.ALL,
                    onClick = { viewModel.onFilterChange(StudentPickerViewModel.RosterFilter.ALL) },
                    label = { Text("All") }
                )
                FilterChip(
                    selected = uiState.filter == StudentPickerViewModel.RosterFilter.ENROLLED,
                    onClick = { viewModel.onFilterChange(StudentPickerViewModel.RosterFilter.ENROLLED) },
                    label = { Text("Enrolled") }
                )
                FilterChip(
                    selected = uiState.filter == StudentPickerViewModel.RosterFilter.NOT_ENROLLED,
                    onClick = { viewModel.onFilterChange(StudentPickerViewModel.RosterFilter.NOT_ENROLLED) },
                    label = { Text("Not enrolled") }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = uiState.quickAddName,
                    onValueChange = viewModel::onQuickAddNameChange,
                    label = { Text("Quick add new student") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = viewModel::quickAddAndEnroll) { Text("Add & enroll") }
            }
            uiState.errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(horizontal = 16.dp))
            }
            TextButton(onClick = onCreateNewStudent, modifier = Modifier.padding(horizontal = 16.dp)) {
                Text("Create a full student profile instead")
            }

            if (uiState.students.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No students found.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.students, key = { it.student.id }) { row ->
                        Card(onClick = { viewModel.toggleEnrollment(row) }, modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // onCheckedChange is null (display-only) since the parent Card already
                                // handles the tap — wiring both would double-toggle on a direct checkbox tap.
                                Checkbox(checked = row.enrolled, onCheckedChange = null)
                                Text(row.student.name, modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            TextButton(onClick = onDone, modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text("Done")
            }
        }
    }
}

