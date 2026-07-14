package com.example.lessonmonitor.ui.export

import com.example.lessonmonitor.MainDispatcherRule
import com.example.lessonmonitor.data.local.entity.AttendanceStatus
import com.example.lessonmonitor.data.local.entity.LessonEntity
import com.example.lessonmonitor.domain.repository.AttendanceRepository
import com.example.lessonmonitor.domain.repository.LessonExportRow
import com.example.lessonmonitor.domain.repository.LessonRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ExportViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val lessonRepository: LessonRepository = mockk()
    private val attendanceRepository: AttendanceRepository = mockk()

    private val lesson = LessonEntity(id = 1L, categoryId = 1L, title = "Algebra 101", startDate = 1L, createdAt = 1L, updatedAt = 1L)

    private fun buildViewModel(): ExportViewModel {
        every { lessonRepository.getAll() } returns flowOf(listOf(lesson))
        return ExportViewModel(lessonRepository, attendanceRepository)
    }

    @Test
    fun `loads the lesson list on creation`() {
        val viewModel = buildViewModel()

        assertEquals(listOf(lesson), viewModel.uiState.value.lessons)
    }

    @Test
    fun `exportLesson builds a sanitized CSV file name and CSV content`() = runTest {
        val viewModel = buildViewModel()
        coEvery { attendanceRepository.getExportRowsForLesson(1L) } returns listOf(
            LessonExportRow(sessionDate = 0L, studentName = "Ada", status = AttendanceStatus.PRESENT, absenceReason = null)
        )

        viewModel.exportLesson(lesson)

        val pending = viewModel.uiState.value.pendingExport
        assertNotNull(pending)
        assertEquals("Algebra_101.csv", pending!!.fileName)
        assertTrue(pending.content.contains("Ada"))
        assertEquals(false, viewModel.uiState.value.isExporting)
    }

    @Test
    fun `exportLesson with no attendance recorded sets an error instead of a pending export`() = runTest {
        val viewModel = buildViewModel()
        coEvery { attendanceRepository.getExportRowsForLesson(1L) } returns emptyList()

        viewModel.exportLesson(lesson)

        assertNull(viewModel.uiState.value.pendingExport)
        assertNotNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `onExportHandled clears the pending export`() = runTest {
        val viewModel = buildViewModel()
        coEvery { attendanceRepository.getExportRowsForLesson(1L) } returns listOf(
            LessonExportRow(sessionDate = 0L, studentName = "Ada", status = AttendanceStatus.PRESENT, absenceReason = null)
        )
        viewModel.exportLesson(lesson)

        viewModel.onExportHandled()

        assertNull(viewModel.uiState.value.pendingExport)
    }
}
