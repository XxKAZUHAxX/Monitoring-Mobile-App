package com.example.lessonmonitor.ui.export

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lessonmonitor.data.export.CsvWriter
import com.example.lessonmonitor.data.local.entity.AttendanceRecordEntity
import com.example.lessonmonitor.data.local.entity.CategoryEntity
import com.example.lessonmonitor.domain.repository.AttendanceRepository
import com.example.lessonmonitor.domain.repository.CategoryRepository
import com.example.lessonmonitor.domain.repository.EnrollmentRepository
import com.example.lessonmonitor.domain.repository.LessonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExportViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val lessonRepository: LessonRepository,
    private val enrollmentRepository: EnrollmentRepository,
    private val attendanceRepository: AttendanceRepository
) : ViewModel() {

    data class PendingExport(val fileName: String, val content: String)

    data class SelectableCategory(
        val category: CategoryEntity,
        val isSelected: Boolean = false,
        val studentCount: Int = 0
    )

    data class UiState(
        val categories: List<SelectableCategory> = emptyList(),
        val isExporting: Boolean = false,
        val errorMessage: String? = null,
        val pendingExport: PendingExport? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            categoryRepository.getAll().collect { categories ->
                val selectable = categories.map { cat ->
                    val roster = enrollmentRepository.getRosterForCategory(cat.id).first()
                    SelectableCategory(cat, studentCount = roster.size)
                }
                _uiState.update {
                    it.copy(categories = selectable.map { sc -> sc.copy(isSelected = false) })
                }
            }
        }
    }

    fun toggleCategory(categoryId: Long) {
        _uiState.update { state ->
            state.copy(
                categories = state.categories.map { cat ->
                    if (cat.category.id == categoryId) cat.copy(isSelected = !cat.isSelected)
                    else cat
                }
            )
        }
    }

    fun exportSelected() {
        val selectedCategories = _uiState.value.categories.filter { it.isSelected }.map { it.category }
        if (selectedCategories.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Select at least one category.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, errorMessage = null) }

            val csvBuilder = StringBuilder()
            for (category in selectedCategories) {
                val lessons = lessonRepository.getAllByCategory(category.id).first()
                val roster = enrollmentRepository.getRosterForCategory(category.id).first()

                if (roster.isEmpty() || lessons.isEmpty()) continue

                // Header row
                csvBuilder.appendLine("Category: ${category.name}")
                csvBuilder.appendLine()

                // Column headers
                csvBuilder.append("Student")
                for (i in lessons.indices) {
                    csvBuilder.append(",Lesson ${i + 1}")
                }
                csvBuilder.appendLine()

                // Lesson titles sub-header
                csvBuilder.append("")
                for (lesson in lessons) {
                    csvBuilder.append(",\"${lesson.title}\"")
                }
                csvBuilder.appendLine()

                // Separator
                csvBuilder.append("---")
                repeat(lessons.size) { csvBuilder.append(",---") }
                csvBuilder.appendLine()

                // Student rows
                for (entry in roster) {
                    csvBuilder.append(entry.student.name)
                    for (lesson in lessons) {
                        val records = attendanceRepository.getRecordsForLesson(lesson.id).first()
                        val record = records.find { it.studentId == entry.student.id }
                        if (record != null) {
                            val status = record.status.name
                            val comp = if (record.completed) "Completed" else "Incomplete"
                            csvBuilder.append(",\"$status / $comp\"")
                        } else {
                            csvBuilder.append(",(no record)")
                        }
                    }
                    csvBuilder.appendLine()
                }
                csvBuilder.appendLine()
            }

            val csv = csvBuilder.toString()
            if (csv.isBlank()) {
                _uiState.update {
                    it.copy(isExporting = false, errorMessage = "No data to export.")
                }
            } else {
                _uiState.update {
                    it.copy(isExporting = false, pendingExport = PendingExport("export.csv", csv))
                }
            }
        }
    }

    fun onExportHandled() = _uiState.update { it.copy(pendingExport = null) }
}
