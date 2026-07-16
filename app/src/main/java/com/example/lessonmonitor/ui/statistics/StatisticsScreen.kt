package com.example.lessonmonitor.ui.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.lessonmonitor.domain.repository.AttendanceStats
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    categoryId: Long = 0L,
    onCategoryClick: (categoryId: Long) -> Unit,
    onStudentClick: (studentId: Long) -> Unit,
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Determine view: categories list, per-student breakdown, or student drill-down
    val showBack = uiState.selectedCategoryId != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when {
                            uiState.selectedStudentId != null -> uiState.selectedStudentName
                            uiState.selectedCategoryId != null -> uiState.selectedCategoryName
                            else -> "Statistics"
                        }
                    )
                },
                navigationIcon = {
                    if (uiState.selectedStudentId != null) {
                        IconButton(onClick = { viewModel.selectStudent(uiState.selectedStudentId!!) }) {} // no-op, handled below
                    }
                    if (showBack) {
                        IconButton(onClick = {
                            val studentId = uiState.selectedStudentId
                            if (studentId != null) {
                                viewModel.selectStudent(studentId)
                            }
                            viewModel.backToCategories()
                        }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    if (uiState.selectedCategoryId == null) {
                        IconButton(onClick = viewModel::loadCategories) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.selectedStudentId != null) {
            // Student drill-down: lessons for this category
            StudentLessonList(rows = uiState.studentLessonRows)
        } else if (uiState.selectedCategoryId != null) {
            // Category selected: per-student stats
            StudentStatsList(
                rows = uiState.studentRows,
                onStudentClick = { viewModel.selectStudent(it) }
            )
        } else {
            // Top level: list of categories
            CategoryList(
                categories = uiState.categories,
                onCategoryClick = { viewModel.selectCategory(it) }
            )
        }
    }
}

@Composable
private fun CategoryList(
    categories: List<StatisticsViewModel.CategoryOverview>,
    onCategoryClick: (Long) -> Unit
) {
    if (categories.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No categories yet.")
        }
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories, key = { it.categoryId }) { cat ->
            Card(onClick = { onCategoryClick(cat.categoryId) }, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text(cat.categoryName, style = MaterialTheme.typography.titleMedium)
                    Text("${cat.studentCount} students", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun StudentStatsList(
    rows: List<StatisticsViewModel.StudentStatRow>,
    onStudentClick: (Long) -> Unit
) {
    if (rows.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No students enrolled in this category.")
        }
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(rows, key = { it.studentId }) { row ->
            Card(onClick = { onStudentClick(row.studentId) }, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text(row.name, style = MaterialTheme.typography.titleMedium)
                    if (row.stats.totalCount == 0) {
                        Text("No records yet", style = MaterialTheme.typography.bodySmall)
                    } else {
                        Text(
                            "${(row.stats.presentRate * 100).roundToInt()}% present (${row.stats.presentCount}/${row.stats.totalCount})",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            "${row.completedCount} of ${row.totalLessons} completed",
                            style = MaterialTheme.typography.bodySmall
                        )
                        PercentageBar(fraction = row.stats.presentRate, modifier = Modifier.padding(top = 8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun StudentLessonList(rows: List<StatisticsViewModel.StudentLessonRow>) {
    if (rows.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No lessons in this category.")
        }
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(rows, key = { it.lessonId }) { row ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text(row.lessonTitle, style = MaterialTheme.typography.titleSmall)
                    Text("Status: ${row.status}", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        if (row.completed) "Completed" else "Incomplete",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun PercentageBar(fraction: Float, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.primary)
        )
    }
}
