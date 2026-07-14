package com.example.lessonmonitor.ui.export

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lessonmonitor.data.export.CsvWriter
import com.example.lessonmonitor.data.local.entity.LessonEntity
import com.example.lessonmonitor.domain.repository.AttendanceRepository
import com.example.lessonmonitor.domain.repository.LessonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * CSV export scoped to a single Lesson (PLAN.md §7 milestone 13's "pick
 * category/lesson → CSV → share sheet"). Scoping to one lesson at a time
 * (rather than a whole category's combined lessons) is a deliberate
 * lower-risk scope decision — see `APP_LOGIC.md`. The actual file write +
 * share-sheet launch is Android-framework-dependent and deliberately kept
 * out of this ViewModel (done in `ExportScreen` via `util/FileSharer.kt`,
 * the same split already used for the Photo Picker in Milestone 8) so this
 * class stays a plain JVM-testable data producer.
 */
@HiltViewModel
class ExportViewModel @Inject constructor(
    private val lessonRepository: LessonRepository,
    private val attendanceRepository: AttendanceRepository
) : ViewModel() {

    /** A CSV file ready to be written + shared by the UI layer. */
    data class PendingExport(val fileName: String, val content: String)

    data class UiState(
        val lessons: List<LessonEntity> = emptyList(),
        val isExporting: Boolean = false,
        val errorMessage: String? = null,
        val pendingExport: PendingExport? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            lessonRepository.getAll().collect { lessons ->
                _uiState.update { it.copy(lessons = lessons) }
            }
        }
    }

    fun exportLesson(lesson: LessonEntity) {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, errorMessage = null) }
            val rows = attendanceRepository.getExportRowsForLesson(lesson.id)
            if (rows.isEmpty()) {
                _uiState.update {
                    it.copy(isExporting = false, errorMessage = "No attendance recorded for this lesson yet.")
                }
                return@launch
            }
            val csv = CsvWriter.writeLessonAttendanceCsv(rows)
            _uiState.update {
                it.copy(isExporting = false, pendingExport = PendingExport("${sanitizeFileName(lesson.title)}.csv", csv))
            }
        }
    }

    fun onExportHandled() = _uiState.update { it.copy(pendingExport = null) }

    private fun sanitizeFileName(name: String): String =
        name.replace(Regex("[^A-Za-z0-9_-]"), "_").ifBlank { "lesson" }
}
