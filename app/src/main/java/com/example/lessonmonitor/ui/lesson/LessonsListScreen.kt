package com.example.lessonmonitor.ui.lesson

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import kotlin.math.roundToInt
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.lessonmonitor.data.local.entity.LessonEntity
import com.example.lessonmonitor.domain.repository.RosterEntry
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonsListScreen(
    categoryId: Long,
    onLessonClick: (lessonId: Long) -> Unit,
    onAddLesson: () -> Unit,
    onAddStudent: () -> Unit,
    onStudentClick: (studentId: Long) -> Unit,
    viewModel: LessonsListViewModel = hiltViewModel()
) {
    LaunchedEffect(categoryId) { viewModel.load(categoryId) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lessonListState = rememberLazyListState()
    val density = LocalDensity.current

    // Drag-to-reorder state for lessons
    var draggedLessonIndex by remember { mutableIntStateOf(-1) }
    var lessonDragOffset by remember { mutableFloatStateOf(0f) }
    var localLessonOrder by remember(uiState.lessons) { mutableStateOf(uiState.lessons.toList()) }

    // Sync localOrder when lessons update from DB and not dragging
    if (draggedLessonIndex < 0) {
        localLessonOrder = uiState.lessons.toList()
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Category") }) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (uiState.selectedTab == LessonsListViewModel.Tab.LESSONS) onAddLesson()
                    else onAddStudent()
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            val selectedIndex = if (uiState.selectedTab == LessonsListViewModel.Tab.LESSONS) 0 else 1
            TabRow(selectedTabIndex = selectedIndex) {
                Tab(
                    selected = selectedIndex == 0,
                    onClick = { viewModel.onTabSelected(LessonsListViewModel.Tab.LESSONS) },
                    text = { Text("Lessons") }
                )
                Tab(
                    selected = selectedIndex == 1,
                    onClick = { viewModel.onTabSelected(LessonsListViewModel.Tab.STUDENTS) },
                    text = { Text("Students") }
                )
            }

            if (selectedIndex == 0) {
                LessonsTab(
                    lessons = localLessonOrder,
                    listState = lessonListState,
                    draggedIndex = draggedLessonIndex,
                    dragOffset = lessonDragOffset,
                    onDragStart = { index -> draggedLessonIndex = index; lessonDragOffset = 0f },
                    onDrag = { change: androidx.compose.ui.input.pointer.PointerInputChange, amount: androidx.compose.ui.geometry.Offset ->
                        change.consume()
                        lessonDragOffset += amount.y
                        val itemHeight = with(density) { 80.dp.toPx() }
                        val positionsMoved = (lessonDragOffset / itemHeight).roundToInt()
                        val currentIdx = draggedLessonIndex
                        if (currentIdx in localLessonOrder.indices) {
                            val targetIdx = (currentIdx + positionsMoved).coerceIn(0, localLessonOrder.lastIndex)
                            if (targetIdx != currentIdx) {
                                val mutable = localLessonOrder.toMutableList()
                                val moved = mutable.removeAt(currentIdx)
                                mutable.add(targetIdx, moved)
                                localLessonOrder = mutable
                                draggedLessonIndex = targetIdx
                                lessonDragOffset = 0f
                            }
                        }
                    },
                    onDragEnd = {
                        viewModel.reorderLessons(localLessonOrder.map { it.id })
                        draggedLessonIndex = -1
                        lessonDragOffset = 0f
                    },
                    onDragCancel = {
                        draggedLessonIndex = -1
                        lessonDragOffset = 0f
                        localLessonOrder = uiState.lessons.toList()
                    },
                    onLessonClick = onLessonClick,
                    onEditClick = { /* navigate to edit */ },
                    onDeleteClick = { viewModel.requestDelete(it) }
                )
            } else {
                StudentsTab(
                    students = uiState.students,
                    onStudentClick = onStudentClick,
                    onEditClick = { onStudentClick(it.student.id) },
                    onRemoveClick = { viewModel.requestUnenroll(it) }
                )
            }
        }
    }

    // Delete lesson confirmation
    uiState.pendingDelete?.let { pending ->
        AlertDialog(
            onDismissRequest = viewModel::cancelDelete,
            title = { Text("Delete \"${pending.lesson.title}\"?") },
            text = {
                Text(
                    "This will remove ${pending.impact.recordCount} attendance records."
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

    // Remove student from category confirmation
    uiState.pendingUnenroll?.let { entry ->
        AlertDialog(
            onDismissRequest = viewModel::cancelUnenroll,
            title = { Text("Remove \"${entry.student.name}\"?") },
            text = {
                Text(
                    "This will remove the student from this category's roster. " +
                        "Their historical attendance records will NOT be deleted."
                )
            },
            confirmButton = {
                TextButton(onClick = viewModel::confirmUnenroll) { Text("Remove") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::cancelUnenroll) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun LessonsTab(
    lessons: List<LessonEntity>,
    listState: androidx.compose.foundation.lazy.LazyListState,
    draggedIndex: Int,
    dragOffset: Float,
    onDragStart: (Int) -> Unit,
    onDrag: (androidx.compose.ui.input.pointer.PointerInputChange, androidx.compose.ui.geometry.Offset) -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit,
    onLessonClick: (Long) -> Unit,
    onEditClick: (LessonEntity) -> Unit,
    onDeleteClick: (LessonEntity) -> Unit
) {
    if (lessons.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No lessons yet. Tap + to add one.")
        }
        return
    }
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(lessons, key = { _, lesson -> lesson.id }) { index, lesson ->
            val isDragged = index == draggedIndex
            val elevation by animateDpAsState(
                targetValue = if (isDragged) 8.dp else 0.dp,
                label = "lessonCardElevation"
            )
            val scale by animateFloatAsState(
                targetValue = if (isDragged) 1.03f else 1f,
                label = "lessonCardScale"
            )
            LessonCard(
                lesson = lesson,
                onClick = { onLessonClick(lesson.id) },
                onEditClick = { onEditClick(lesson) },
                onDeleteClick = { onDeleteClick(lesson) },
                modifier = Modifier
                    .zIndex(if (isDragged) 1f else 0f)
                    .offset { IntOffset(0, if (isDragged) dragOffset.roundToInt() else 0) }
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        shadowElevation = elevation.toPx()
                    }
                    .animateItemPlacement()
                    .pointerInput(lesson.id) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = { onDragStart(index) },
                            onDragEnd = onDragEnd,
                            onDragCancel = onDragCancel,
                            onDrag = onDrag
                        )
                    }
            )
        }
    }
}

@Composable
private fun LessonCard(
    lesson: LessonEntity,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Card(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(lesson.title, style = MaterialTheme.typography.titleMedium)
                val subtitle = listOfNotNull(
                    lesson.facilitatorName?.takeIf { it.isNotBlank() },
                    lesson.place?.takeIf { it.isNotBlank() }
                ).joinToString(" · ")
                if (subtitle.isNotBlank()) {
                    Text(subtitle, style = MaterialTheme.typography.bodySmall)
                }
                Text(LocalDate.ofEpochDay(lesson.startDate).toString(), style = MaterialTheme.typography.bodySmall)
            }
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More options")
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
}

@Composable
private fun StudentsTab(
    students: List<RosterEntry>,
    onStudentClick: (Long) -> Unit,
    onEditClick: (RosterEntry) -> Unit,
    onRemoveClick: (RosterEntry) -> Unit
) {
    if (students.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No students enrolled. Tap + to add one.")
        }
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(students, key = { it.enrollment.id }) { entry ->
            StudentRow(
                entry = entry,
                onClick = { onStudentClick(entry.student.id) },
                onEditClick = { onEditClick(entry) },
                onRemoveClick = { onRemoveClick(entry) }
            )
        }
    }
}

@Composable
private fun StudentRow(
    entry: RosterEntry,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onRemoveClick: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(entry.student.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More options")
                }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        onClick = { menuExpanded = false; onEditClick() }
                    )
                    DropdownMenuItem(
                        text = { Text("Remove from category") },
                        onClick = { menuExpanded = false; onRemoveClick() }
                    )
                }
            }
        }
    }
}
