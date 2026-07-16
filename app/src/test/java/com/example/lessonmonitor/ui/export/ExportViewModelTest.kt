package com.example.lessonmonitor.ui.export

import com.example.lessonmonitor.MainDispatcherRule
import com.example.lessonmonitor.data.local.entity.AttendanceRecordEntity
import com.example.lessonmonitor.data.local.entity.AttendanceStatus
import com.example.lessonmonitor.data.local.entity.CategoryEntity
import com.example.lessonmonitor.data.local.entity.LessonEntity
import com.example.lessonmonitor.data.local.entity.StudentEntity
import com.example.lessonmonitor.domain.repository.AttendanceRepository
import com.example.lessonmonitor.domain.repository.CategoryRepository
import com.example.lessonmonitor.domain.repository.EnrollmentRepository
import com.example.lessonmonitor.domain.repository.LessonRepository
import com.example.lessonmonitor.domain.repository.RosterEntry
import com.example.lessonmonitor.data.local.entity.EnrollmentEntity
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ExportViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val categoryRepository: CategoryRepository = mockk()
    private val lessonRepository: LessonRepository = mockk()
    private val enrollmentRepository: EnrollmentRepository = mockk()
    private val attendanceRepository: AttendanceRepository = mockk()

    private val category = CategoryEntity(
        id = 1L, name = "Math", createdAt = 1L, updatedAt = 1L
    )
    private val lesson = LessonEntity(
        id = 1L, categoryId = 1L, title = "Algebra 101",
        startDate = 1L, createdAt = 1L, updatedAt = 1L
    )
    private val student = StudentEntity(
        id = 1L, name = "Ada", createdAt = 1L, updatedAt = 1L
    )
    private val enrollment = EnrollmentEntity(
        id = 1L, categoryId = 1L, studentId = 1L, enrolledAt = 1L, active = true
    )

    private fun buildViewModel(): ExportViewModel {
        every { categoryRepository.getAll() } returns flowOf(listOf(category))
        every { enrollmentRepository.getRosterForCategory(1L) } returns flowOf(
            listOf(RosterEntry(enrollment, student))
        )
        return ExportViewModel(categoryRepository, lessonRepository, enrollmentRepository, attendanceRepository)
    }

    @Test
    fun `loads categories on creation`() {
        val viewModel = buildViewModel()

        assertEquals(1, viewModel.uiState.value.categories.size)
        assertEquals("Math", viewModel.uiState.value.categories[0].category.name)
    }

    @Test
    fun `toggleCategory toggles selection state`() {
        val viewModel = buildViewModel()

        assertFalse(viewModel.uiState.value.categories[0].isSelected)

        viewModel.toggleCategory(1L)
        assertTrue(viewModel.uiState.value.categories[0].isSelected)

        viewModel.toggleCategory(1L)
        assertFalse(viewModel.uiState.value.categories[0].isSelected)
    }

    @Test
    fun `exportSelected with no selection sets error`() = runTest {
        val viewModel = buildViewModel()

        viewModel.exportSelected()

        assertEquals("Select at least one category.", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `exportSelected builds CSV content and sets pending export`() = runTest {
        val viewModel = buildViewModel()
        // Select the category
        viewModel.toggleCategory(1L)

        every { lessonRepository.getAllByCategory(1L) } returns flowOf(listOf(lesson))
        every { attendanceRepository.getRecordsForLesson(1L) } returns flowOf(
            listOf(
                AttendanceRecordEntity(
                    id = 1L, lessonId = 1L, studentId = 1L,
                    status = AttendanceStatus.PRESENT, completed = true, recordedAt = 1L
                )
            )
        )

        viewModel.exportSelected()

        val pending = viewModel.uiState.value.pendingExport
        assertNotNull(pending)
        assertEquals("export.csv", pending!!.fileName)
        assertTrue(pending.content.contains("Math"))
        assertTrue(pending.content.contains("Algebra 101"))
        assertTrue(pending.content.contains("Ada"))
        assertFalse(viewModel.uiState.value.isExporting)
    }

    @Test
    fun `exportSelected with no data sets error`() = runTest {
        val viewModel = buildViewModel()
        viewModel.toggleCategory(1L)

        // Empty roster
        every { enrollmentRepository.getRosterForCategory(1L) } returns flowOf(emptyList())
        every { lessonRepository.getAllByCategory(1L) } returns flowOf(listOf(lesson))

        viewModel.exportSelected()

        assertNull(viewModel.uiState.value.pendingExport)
        assertEquals("No data to export.", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `onExportHandled clears the pending export`() = runTest {
        val viewModel = buildViewModel()
        viewModel.toggleCategory(1L)

        every { lessonRepository.getAllByCategory(1L) } returns flowOf(listOf(lesson))
        every { attendanceRepository.getRecordsForLesson(1L) } returns flowOf(
            listOf(
                AttendanceRecordEntity(
                    id = 1L, lessonId = 1L, studentId = 1L,
                    status = AttendanceStatus.PRESENT, completed = true, recordedAt = 1L
                )
            )
        )

        viewModel.exportSelected()

        assertNotNull(viewModel.uiState.value.pendingExport)

        viewModel.onExportHandled()

        assertNull(viewModel.uiState.value.pendingExport)
    }
}
