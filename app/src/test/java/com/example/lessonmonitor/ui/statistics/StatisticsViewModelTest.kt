package com.example.lessonmonitor.ui.statistics

import com.example.lessonmonitor.MainDispatcherRule
import com.example.lessonmonitor.data.local.entity.LessonEntity
import com.example.lessonmonitor.data.local.entity.StudentEntity
import com.example.lessonmonitor.domain.repository.AttendanceRepository
import com.example.lessonmonitor.domain.repository.AttendanceStats
import com.example.lessonmonitor.domain.repository.LessonRepository
import com.example.lessonmonitor.domain.repository.StudentRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class StatisticsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val studentRepository: StudentRepository = mockk()
    private val lessonRepository: LessonRepository = mockk()
    private val attendanceRepository: AttendanceRepository = mockk()

    private fun buildViewModel(): StatisticsViewModel {
        every { studentRepository.getAll() } returns flowOf(
            listOf(StudentEntity(id = 1L, name = "Ada", createdAt = 1L, updatedAt = 1L))
        )
        every { lessonRepository.getAll() } returns flowOf(
            listOf(LessonEntity(id = 2L, categoryId = 1L, title = "Algebra", startDate = 1L, createdAt = 1L, updatedAt = 1L))
        )
        coEvery { attendanceRepository.getStudentAttendanceStats(1L) } returns AttendanceStats(presentCount = 3, totalCount = 4)
        coEvery { attendanceRepository.getLessonAttendanceStats(2L) } returns AttendanceStats(presentCount = 5, totalCount = 10)
        return StatisticsViewModel(studentRepository, lessonRepository, attendanceRepository)
    }

    @Test
    fun `refresh loads per-student and per-lesson attendance stats and clears loading`() {
        val viewModel = buildViewModel()

        val state = viewModel.uiState.value

        assertEquals(false, state.isLoading)
        assertEquals(1, state.studentRows.size)
        assertEquals("Ada", state.studentRows[0].name)
        assertEquals(3, state.studentRows[0].stats.presentCount)
        assertEquals(1, state.lessonRows.size)
        assertEquals("Algebra", state.lessonRows[0].title)
        assertEquals(10, state.lessonRows[0].stats.totalCount)
    }

    @Test
    fun `onTabSelected switches the selected tab`() {
        val viewModel = buildViewModel()

        viewModel.onTabSelected(StatisticsViewModel.Tab.LESSONS)

        assertEquals(StatisticsViewModel.Tab.LESSONS, viewModel.uiState.value.selectedTab)
    }
}
