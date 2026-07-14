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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
    onStudentClick: (studentId: Long) -> Unit,
    onLessonClick: (lessonId: Long) -> Unit,
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statistics") },
                actions = {
                    IconButton(onClick = viewModel::refresh) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            val selectedTabIndex = if (uiState.selectedTab == StatisticsViewModel.Tab.STUDENTS) 0 else 1
            TabRow(selectedTabIndex = selectedTabIndex) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { viewModel.onTabSelected(StatisticsViewModel.Tab.STUDENTS) },
                    text = { Text("Students") }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { viewModel.onTabSelected(StatisticsViewModel.Tab.LESSONS) },
                    text = { Text("Lessons") }
                )
            }

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (selectedTabIndex == 0) {
                StudentStatsList(rows = uiState.studentRows, onStudentClick = onStudentClick)
            } else {
                LessonStatsList(rows = uiState.lessonRows, onLessonClick = onLessonClick)
            }
        }
    }
}

@Composable
private fun StudentStatsList(
    rows: List<StatisticsViewModel.StudentStatRow>,
    onStudentClick: (studentId: Long) -> Unit
) {
    if (rows.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No students yet.")
        }
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(rows, key = { it.studentId }) { row ->
            AttendanceStatCard(
                title = row.name,
                stats = row.stats,
                onClick = { onStudentClick(row.studentId) }
            )
        }
    }
}

@Composable
private fun LessonStatsList(
    rows: List<StatisticsViewModel.LessonStatRow>,
    onLessonClick: (lessonId: Long) -> Unit
) {
    if (rows.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No lessons yet.")
        }
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(rows, key = { it.lessonId }) { row ->
            AttendanceStatCard(
                title = row.title,
                stats = row.stats,
                onClick = { onLessonClick(row.lessonId) }
            )
        }
    }
}

@Composable
private fun AttendanceStatCard(title: String, stats: AttendanceStats, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(
                if (stats.totalCount == 0) {
                    "No sessions recorded yet"
                } else {
                    "${(stats.presentRate * 100).roundToInt()}% present (${stats.presentCount}/${stats.totalCount})"
                },
                style = MaterialTheme.typography.bodyMedium
            )
            PercentageBar(fraction = stats.presentRate, modifier = Modifier.padding(top = 8.dp))
        }
    }
}

/**
 * Hand-rolled percentage bar (a background track + a fraction-width filled
 * box) instead of Material3's `LinearProgressIndicator` or a charting
 * library — a stable, dependency-free "simple visualization" per the
 * prompt's §H suggestion, consistent with this project's low-risk-dependency
 * pattern (see the manual Calendar month grid, milestone #10).
 */
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
