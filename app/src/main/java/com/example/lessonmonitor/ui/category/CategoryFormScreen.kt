package com.example.lessonmonitor.ui.category

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.lessonmonitor.navigation.Routes

/** Fixed swatch palette for `CategoryEntity.color` (ARGB ints) — a full color picker is out of scope for Phase 1. */
private val CATEGORY_COLORS = listOf(
    0xFFE57373.toInt(), // red
    0xFFFFB74D.toInt(), // orange
    0xFFFFF176.toInt(), // yellow
    0xFF81C784.toInt(), // green
    0xFF4FC3F7.toInt(), // blue
    0xFF9575CD.toInt(), // purple
    0xFF90A4AE.toInt() // gray
)

@Composable
fun CategoryFormScreen(
    categoryId: Long,
    onDone: () -> Unit,
    viewModel: CategoryFormViewModel = hiltViewModel()
) {
    LaunchedEffect(categoryId) { viewModel.load(categoryId) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isNew = categoryId == Routes.NEW_ID

    Scaffold(topBar = { TopAppBar(title = { Text(if (isNew) "Add Category" else "Edit Category") }) }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = uiState.name,
                onValueChange = viewModel::onNameChange,
                label = { Text("Name") },
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
                value = uiState.icon,
                onValueChange = viewModel::onIconChange,
                label = { Text("Icon (emoji, optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Text("Color", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CATEGORY_COLORS.forEach { color ->
                    ColorSwatch(
                        color = Color(color),
                        selected = uiState.color == color,
                        onClick = { viewModel.onColorSelected(color) }
                    )
                }
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

@Composable
private fun ColorSwatch(color: Color, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                width = if (selected) 3.dp else 1.dp,
                color = MaterialTheme.colorScheme.onSurface,
                shape = CircleShape
            )
            .clickable(onClick = onClick)
    )
}

