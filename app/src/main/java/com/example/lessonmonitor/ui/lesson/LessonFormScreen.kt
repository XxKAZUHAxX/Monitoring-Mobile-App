package com.example.lessonmonitor.ui.lesson

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.lessonmonitor.data.local.entity.RecurrenceType
import com.example.lessonmonitor.navigation.Routes
import java.time.DayOfWeek

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
    var showRecurrenceMenu by remember { mutableStateOf(false) }

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
                label = { Text("Start date (yyyy-MM-dd)") },
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
            OutlinedTextField(
                value = uiState.endTimeText,
                onValueChange = viewModel::onEndTimeTextChange,
                label = { Text("End time HH:mm (optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Recurring lesson", modifier = Modifier.weight(1f))
                Switch(checked = uiState.isRecurring, onCheckedChange = viewModel::onRecurringChange)
            }

            if (uiState.isRecurring) {
                Box {
                    OutlinedButton(onClick = { showRecurrenceMenu = true }, modifier = Modifier.fillMaxWidth()) {
                        Text("Repeats: ${uiState.recurrenceType}")
                    }
                    DropdownMenu(expanded = showRecurrenceMenu, onDismissRequest = { showRecurrenceMenu = false }) {
                        listOf(RecurrenceType.DAILY, RecurrenceType.WEEKLY, RecurrenceType.CUSTOM_DAYS).forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.name) },
                                onClick = {
                                    viewModel.onRecurrenceTypeChange(type)
                                    showRecurrenceMenu = false
                                }
                            )
                        }
                    }
                }

                if (uiState.recurrenceType != RecurrenceType.DAILY) {
                    Text("Days of week", style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        DayOfWeek.values().forEach { day ->
                            FilterChip(
                                selected = uiState.recurrenceDaysOfWeek.contains(day.value),
                                onClick = { viewModel.onToggleDayOfWeek(day.value) },
                                label = { Text(day.name.take(1)) }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = uiState.endDateText,
                    onValueChange = viewModel::onEndDateTextChange,
                    label = { Text("End date yyyy-MM-dd (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

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

