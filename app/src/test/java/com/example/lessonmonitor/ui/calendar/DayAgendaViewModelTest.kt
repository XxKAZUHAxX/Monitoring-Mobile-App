package com.example.lessonmonitor.ui.calendar

import com.example.lessonmonitor.MainDispatcherRule
import com.example.lessonmonitor.data.local.entity.AttendanceSessionEntity
import com.example.lessonmonitor.domain.repository.AttendanceRepository
import com.example.lessonmonitor.domain.repository.CalendarSessionEntry
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class DayAgendaViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val attendanceRepository: AttendanceRepository = mockk()

    @Test
    fun `load fetches and sorts sessions for that day by lesson title`() = runTest {
        val geometry = CalendarSessionEntry(
            AttendanceSessionEntity(id = 2L, lessonId = 20L, sessionDate = 19000L, createdAt = 1L),
            "Geometry"
        )
        val algebra = CalendarSessionEntry(
            AttendanceSessionEntity(id = 1L, lessonId = 10L, sessionDate = 19000L, createdAt = 1L),
            "Algebra"
        )
        every { attendanceRepository.getSessionsInRange(19000L, 19000L) } returns flowOf(listOf(geometry, algebra))

        val viewModel = DayAgendaViewModel(attendanceRepository)
        viewModel.load(19000L)

        assertEquals(listOf("Algebra", "Geometry"), viewModel.uiState.value.sessions.map { it.lessonTitle })
        assertEquals(false, viewModel.uiState.value.isLoading)
    }

    @Test
    fun `load is idempotent for the same epoch day`() = runTest {
        every { attendanceRepository.getSessionsInRange(19000L, 19000L) } returns flowOf(emptyList())

        val viewModel = DayAgendaViewModel(attendanceRepository)
        viewModel.load(19000L)
        viewModel.load(19000L)

        verify(exactly = 1) { attendanceRepository.getSessionsInRange(19000L, 19000L) }
    }
}
