package com.example.lessonmonitor.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

/**
 * Month grid (PLAN.md §4 screen 13). Weeks start on Monday to match
 * [com.example.lessonmonitor.data.local.entity.LessonEntity.recurrenceDaysOfWeek]'s
 * ISO day-of-week convention used throughout the app. Days with at least one
 * `AttendanceSession` show a small dot; today is highlighted.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(onDayClick: (epochDay: Long) -> Unit, viewModel: CalendarViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val yearMonth = uiState.yearMonth

    Scaffold(
        topBar = { TopAppBar(title = { Text("Calendar") }) }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxWidth().padding(innerPadding).padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = viewModel::goToPreviousMonth) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Previous month")
                }
                Text(
                    "${yearMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${yearMonth.year}",
                    style = MaterialTheme.typography.titleMedium
                )
                IconButton(onClick = viewModel::goToNextMonth) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Next month")
                }
            }
            TextButton(onClick = viewModel::goToToday) { Text("Today") }

            WeekdayHeader()
            monthGridWeeks(yearMonth).forEach { week ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    week.forEach { date ->
                        DayCell(
                            date = date,
                            hasSessions = date != null && uiState.sessionsByDay.containsKey(date.toEpochDay()),
                            onClick = { date?.let { onDayClick(it.toEpochDay()) } },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WeekdayHeader() {
    Row(modifier = Modifier.fillMaxWidth()) {
        val monday = LocalDate.now().minusDays((LocalDate.now().dayOfWeek.value - 1).toLong())
        for (offset in 0..6) {
            val label = monday.plusDays(offset.toLong()).dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.getDefault())
            Text(
                label,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun DayCell(date: LocalDate?, hasSessions: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val isToday = date != null && date == LocalDate.now()
    Card(
        onClick = onClick,
        modifier = modifier.aspectRatio(1f).padding(2.dp),
        colors = if (isToday) {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        } else {
            CardDefaults.cardColors()
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(date?.dayOfMonth?.toString() ?: "", style = MaterialTheme.typography.bodyMedium)
            if (hasSessions) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}

/** Splits [yearMonth] into Monday-starting weeks, padding leading/trailing cells with `null`. */
private fun monthGridWeeks(yearMonth: YearMonth): List<List<LocalDate?>> {
    val firstOfMonth = yearMonth.atDay(1)
    val leadingBlanks = firstOfMonth.dayOfWeek.value - 1 // 1=Monday..7=Sunday
    val daysInMonth = yearMonth.lengthOfMonth()
    val cells = mutableListOf<LocalDate?>()
    repeat(leadingBlanks) { cells.add(null) }
    for (day in 1..daysInMonth) {
        cells.add(yearMonth.atDay(day))
    }
    while (cells.size % 7 != 0) {
        cells.add(null)
    }
    return cells.chunked(7)
}
