package com.example.lessonmonitor.ui.dashboard

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.lessonmonitor.data.local.entity.CategoryEntity
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onCategoryClick: (categoryId: Long) -> Unit,
    onAddCategory: () -> Unit,
    onSearchClick: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    // Drag-to-reorder state
    var draggedIndex by remember { mutableIntStateOf(-1) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    var localOrder by remember(uiState.categories) { mutableStateOf(uiState.categories.toList()) }

    // Sync localOrder when categories update from DB
    if (draggedIndex < 0) {
        localOrder = uiState.categories.toList()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Categories") },
                actions = {
                    IconButton(onClick = onSearchClick) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddCategory) {
                Icon(Icons.Default.Add, contentDescription = "Add category")
            }
        }
    ) { innerPadding ->
        if (localOrder.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("No categories yet. Tap + to add one.")
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(localOrder, key = { _, cat -> cat.id }) { index, category ->
                    val isDragged = index == draggedIndex
                    CategoryRow(
                        category = category,
                        onClick = { onCategoryClick(category.id) },
                        onEditClick = { onCategoryClick(category.id) },
                        onDeleteClick = { viewModel.requestDelete(category) },
                        modifier = Modifier
                            .zIndex(if (isDragged) 1f else 0f)
                            .offset { IntOffset(0, if (isDragged) dragOffset.roundToInt() else 0) }
                            .pointerInput(category.id) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = {
                                        draggedIndex = index
                                        dragOffset = 0f
                                    },
                                    onDragEnd = {
                                        // Persist the new order
                                        viewModel.reorderCategories(localOrder.map { it.id })
                                        draggedIndex = -1
                                        dragOffset = 0f
                                    },
                                    onDragCancel = {
                                        draggedIndex = -1
                                        dragOffset = 0f
                                        localOrder = uiState.categories.toList()
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        dragOffset += dragAmount.y

                                        // Calculate target position based on offset
                                        val itemHeight = 80.dp.toPx() // approximate card height + spacing
                                        val positionsMoved = (dragOffset / itemHeight).roundToInt()
                                        val targetIndex = (index + positionsMoved)
                                            .coerceIn(0, localOrder.lastIndex)

                                        if (targetIndex != index) {
                                            val mutable = localOrder.toMutableList()
                                            val moved = mutable.removeAt(index)
                                            mutable.add(targetIndex, moved)
                                            localOrder = mutable
                                            draggedIndex = targetIndex
                                            dragOffset = 0f // reset offset after swap
                                        }
                                    }
                                )
                            }
                    )
                }
            }
        }
    }

    uiState.pendingDelete?.let { pending ->
        AlertDialog(
            onDismissRequest = viewModel::cancelDelete,
            title = { Text("Delete \"${pending.category.name}\"?") },
            text = {
                Text(
                    "This will delete ${pending.impact.lessonCount} lessons and " +
                        "${pending.impact.recordCount} attendance records."
                )
            },
            confirmButton = {
                TextButton(onClick = viewModel::confirmDelete) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::cancelDelete) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun CategoryRow(
    category: CategoryEntity,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Card(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(category.name, style = MaterialTheme.typography.titleMedium)
                    category.description?.takeIf { it.isNotBlank() }?.let { description ->
                        Text(description, style = MaterialTheme.typography.bodySmall)
                    }
                }
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More options")
                }
            }
            DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                DropdownMenuItem(
                    text = { Text("Edit") },
                    onClick = { menuExpanded = false; onEditClick() }
                )
                DropdownMenuItem(
                    text = { Text("Delete") },
                    onClick = { menuExpanded = false; onDeleteClick() }
                )
            }
        }
    }
}
