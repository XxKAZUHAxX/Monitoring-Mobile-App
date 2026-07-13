package com.example.lessonmonitor.ui.lesson

import com.example.lessonmonitor.MainDispatcherRule
import com.example.lessonmonitor.data.local.entity.AttendanceSessionEntity
import com.example.lessonmonitor.data.local.entity.EnrollmentEntity
import com.example.lessonmonitor.data.local.entity.LessonEntity
import com.example.lessonmonitor.data.local.entity.StudentEntity
import com.example.lessonmonitor.domain.repository.AttendanceRepository
import com.example.lessonmonitor.domain.repository.EnrollmentRepository
import com.example.lessonmonitor.domain.repository.LessonRepository
import com.example.lessonmonitor.domain.repository.RosterEntry
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class LessonDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val lessonRepository: LessonRepository = mockk()
    private val enrollmentRepository: EnrollmentRepository = mockk()
    private val attendanceRepository: AttendanceRepository = mockk()

    private val lesson = LessonEntity(id = 5L, categoryId = 1L, title = "Algebra", startDate = 19000L, createdAt = 1L, updatedAt = 1L)
    private val rosterEntry = RosterEntry(
        enrollment = EnrollmentEntity(id = 1L, lessonId = 5L, studentId = 2L, enrolledAt = 1L),
        student = StudentEntity(id = 2L, name = "Ana", createdAt = 1L, updatedAt = 1L)
    )

    private fun buildViewModel(): LessonDetailViewModel {
        coEvery { lessonRepository.getById(5L) } returns flowOf(lesson)
        coEvery { enrollmentRepository.getRosterForLesson(5L) } returns flowOf(listOf(rosterEntry))
        coEvery { attendanceRepository.getSessionsForLesson(5L) } returns flowOf(emptyList())
        return LessonDetailViewModel(lessonRepository, enrollmentRepository, attendanceRepository)
    }

    @Test
    fun `load populates lesson, roster, and sessions`() {
        val viewModel = buildViewModel()

        viewModel.load(5L)

        assertEquals(lesson, viewModel.uiState.value.lesson)
        assertEquals(listOf(rosterEntry), viewModel.uiState.value.roster)
        assertEquals(emptyList<AttendanceSessionEntity>(), viewModel.uiState.value.sessions)
    }

    @Test
    fun `addSession with a malformed date sets an error and does not call the repository`() {
        val viewModel = buildViewModel()
        viewModel.load(5L)
        viewModel.onNewSessionDateTextChange("not-a-date")

        viewModel.addSession()

        assertEquals("Date must be in yyyy-MM-dd format", viewModel.uiState.value.errorMessage)
        coVerify(exactly = 0) { attendanceRepository.createSession(any(), any()) }
    }

    @Test
    fun `addSession with a valid date creates a session for the parsed epoch day`() {
        coEvery { attendanceRepository.createSession(5L, any()) } returns 9L
        val viewModel = buildViewModel()
        viewModel.load(5L)
        viewModel.onNewSessionDateTextChange("2025-01-15")

        viewModel.addSession()

        coVerify { attendanceRepository.createSession(5L, java.time.LocalDate.of(2025, 1, 15).toEpochDay()) }
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `unenroll delegates to the repository with the roster entry's enrollment`() {
        coEvery { enrollmentRepository.unenroll(rosterEntry.enrollment) } returns Unit
        val viewModel = buildViewModel()
        viewModel.load(5L)

        viewModel.unenroll(rosterEntry)

        coVerify { enrollmentRepository.unenroll(rosterEntry.enrollment) }
    }
}
