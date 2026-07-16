package com.example.lessonmonitor.ui.statistics

import com.example.lessonmonitor.MainDispatcherRule
import com.example.lessonmonitor.data.local.entity.AttendanceRecordEntity
import com.example.lessonmonitor.data.local.entity.AttendanceStatus
import com.example.lessonmonitor.data.local.entity.CategoryEntity
import com.example.lessonmonitor.data.local.entity.LessonEntity
import com.example.lessonmonitor.data.local.entity.StudentEntity
import com.example.lessonmonitor.domain.repository.AttendanceRepository
import com.example.lessonmonitor.domain.repository.AttendanceStats
import com.example.lessonmonitor.domain.repository.CategoryRepository
import com.example.lessonmonitor.domain.repository.EnrollmentRepository
import com.example.lessonmonitor.domain.repository.LessonRepository
import com.example.lessonmonitor.domain.repository.RosterEntry
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

    private val categoryRepository: CategoryRepository = mockk()
    private val studentRepository: StudentRepository = mockk()
    private val lessonRepository: LessonRepository = mockk()
    private val enrollmentRepository: EnrollmentRepository = mockk()
    private val attendanceRepository: AttendanceRepository = mockk()

    private fun buildViewModel(): StatisticsViewModel {
        every { categoryRepository.getAll() } returns flowOf(
            listOf(CategoryEntity(id = 1L, name = "Math", createdAt = 1L, updatedAt = 1L))
        )
        every { categoryRepository.getById(1L) } returns flowOf(
            CategoryEntity(id = 1L, name = "Math", createdAt = 1L, updatedAt = 1L)
        )
        every { enrollmentRepository.getRosterForCategory(1L) } returns flowOf(
            listOf(
                RosterEntry(
                    enrollment = mockk(),
                    student = StudentEntity(id = 10L, name = "Ada", createdAt = 1L, updatedAt = 1L)
                )
            )
        )
        every { lessonRepository.getAllByCategory(1L) } returns flowOf(
            listOf(LessonEntity(id = 2L, categoryId = 1L, title = "Algebra", startDate = 1L, createdAt = 1L, updatedAt = 1L))
        )
        coEvery { attendanceRepository.getStudentAttendanceStats(10L) } returns AttendanceStats(presentCount = 3, totalCount = 4)
        coEvery { attendanceRepository.getRecordsForLesson(2L) } returns flowOf(
            listOf(
                AttendanceRecordEntity(
                    id = 1L, lessonId = 2L, studentId = 10L,
                    status = AttendanceStatus.PRESENT, completed = true, recordedAt = 1L
                )
            )
        )
        every { studentRepository.getById(10L) } returns flowOf(
            StudentEntity(id = 10L, name = "Ada", createdAt = 1L, updatedAt = 1L)
        )
        return StatisticsViewModel(categoryRepository, studentRepository, lessonRepository, enrollmentRepository, attendanceRepository)
    }

    @Test
    fun `loadCategories populates category overviews`() {
        val viewModel = buildViewModel()

        val state = viewModel.uiState.value

        assertEquals(false, state.isLoading)
        assertEquals(1, state.categories.size)
        assertEquals("Math", state.categories[0].categoryName)
        assertEquals(1, state.categories[0].studentCount)
    }

    @Test
    fun `selectCategory loads per-student attendance stats`() {
        val viewModel = buildViewModel()

        viewModel.selectCategory(1L)
        val state = viewModel.uiState.value

        assertEquals(false, state.isLoading)
        assertEquals(1L, state.selectedCategoryId)
        assertEquals("Math", state.selectedCategoryName)
        assertEquals(1, state.studentRows.size)
        assertEquals("Ada", state.studentRows[0].name)
        assertEquals(3, state.studentRows[0].stats.presentCount)
        assertEquals(4, state.studentRows[0].stats.totalCount)
        assertEquals(1, state.studentRows[0].completedCount)
        assertEquals(1, state.studentRows[0].totalLessons)
    }

    @Test
    fun `selectStudent loads per-lesson rows`() {
        val viewModel = buildViewModel()
        viewModel.selectCategory(1L)

        viewModel.selectStudent(10L)
        val state = viewModel.uiState.value

        assertEquals(10L, state.selectedStudentId)
        assertEquals("Ada", state.selectedStudentName)
        assertEquals(1, state.studentLessonRows.size)
        assertEquals("Algebra", state.studentLessonRows[0].lessonTitle)
        assertEquals("PRESENT", state.studentLessonRows[0].status)
        assertEquals(true, state.studentLessonRows[0].completed)
    }

    @Test
    fun `backToStudentStats clears student selection`() {
        val viewModel = buildViewModel()
        viewModel.selectCategory(1L)
        viewModel.selectStudent(10L)

        viewModel.backToStudentStats()
        val state = viewModel.uiState.value

        assertEquals(null, state.selectedStudentId)
        assertEquals("", state.selectedStudentName)
        assertEquals(0, state.studentLessonRows.size)
    }

    @Test
    fun `backToCategories clears category and student selection`() {
        val viewModel = buildViewModel()
        viewModel.selectCategory(1L)

        viewModel.backToCategories()
        val state = viewModel.uiState.value

        assertEquals(null, state.selectedCategoryId)
        assertEquals(null, state.selectedStudentId)
        assertEquals(0, state.studentRows.size)
    }
}
