package com.example.lessonmonitor.ui.search

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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onCategoryResultClick: (categoryId: Long) -> Unit,
    onLessonResultClick: (lessonId: Long) -> Unit,
    onStudentResultClick: (studentId: Long) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val hasAnyResults = uiState.categoryResults.isNotEmpty() ||
        uiState.lessonResults.isNotEmpty() ||
        uiState.studentResults.isNotEmpty()

    Scaffold(topBar = { TopAppBar(title = { Text("Search") }) }) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            OutlinedTextField(
                value = uiState.query,
                onValueChange = viewModel::onQueryChange,
                label = { Text("Search categories, lessons, students") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            )

            when {
                uiState.query.isBlank() -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Type to search.")
                }
                !hasAnyResults -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No results found.")
                }
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (uiState.categoryResults.isNotEmpty()) {
                        item { SectionHeader("Categories") }
                        items(uiState.categoryResults, key = { "category_${it.id}" }) { category ->
                            ResultRow(title = category.name, onClick = { onCategoryResultClick(category.id) })
                        }
                    }
                    if (uiState.lessonResults.isNotEmpty()) {
                        item { SectionHeader("Lessons") }
                        items(uiState.lessonResults, key = { "lesson_${it.id}" }) { lesson ->
                            ResultRow(title = lesson.title, onClick = { onLessonResultClick(lesson.id) })
                        }
                    }
                    if (uiState.studentResults.isNotEmpty()) {
                        item { SectionHeader("Students") }
                        items(uiState.studentResults, key = { "student_${it.id}" }) { student ->
                            ResultRow(title = student.name, onClick = { onStudentResultClick(student.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
}

@Composable
private fun ResultRow(title: String, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Text(title, modifier = Modifier.fillMaxWidth().padding(16.dp))
    }
}
