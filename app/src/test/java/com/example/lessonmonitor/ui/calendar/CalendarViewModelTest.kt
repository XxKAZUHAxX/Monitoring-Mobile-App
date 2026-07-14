package com.example.lessonmonitor.ui.calendar

import com.example.lessonmonitor.MainDispatcherRule
import com.example.lessonmonitor.data.local.entity.AttendanceSessionEntity
import com.example.lessonmonitor.domain.repository.AttendanceRepository
import com.example.lessonmonitor.domain.repository.CalendarSessionEntry
import com.example.lessonmonitor.domain.schedule.RecurringSessionGenerator
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class CalendarViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val attendanceRepository: AttendanceRepository = mockk()
    private val recurringSessionGenerator: RecurringSessionGenerator = mockk()

    @Before
    fun setUp() {
        coEvery { recurringSessionGenerator.generateUpcomingSessions() } returns Unit
    }

    @Test
    fun `loads the current month on creation and re-runs the session generator`() = runTest {
        every { attendanceRepository.getSessionsInRange(any(), any()) } returns flowOf(emptyList())

        val viewModel = CalendarViewModel(attendanceRepository, recurringSessionGenerator)

        assertEquals(java.time.YearMonth.now(), viewModel.uiState.value.yearMonth)
        coVerify { recurringSessionGenerator.generateUpcomingSessions() }
    }

    @Test
    fun `groups sessions by their epoch day`() = runTest {
        val today = LocalDate.now()
        val startOfMonth = today.withDayOfMonth(1).toEpochDay()
        val endOfMonth = today.withDayOfMonth(today.lengthOfMonth()).toEpochDay()
        val session = AttendanceSessionEntity(id = 1L, lessonId = 5L, sessionDate = today.toEpochDay(), createdAt = 1L)
        every { attendanceRepository.getSessionsInRange(startOfMonth, endOfMonth) } returns
            flowOf(listOf(CalendarSessionEntry(session, "Algebra")))

        val viewModel = CalendarViewModel(attendanceRepository, recurringSessionGenerator)

        val sessionsToday = viewModel.uiState.value.sessionsByDay[today.toEpochDay()]
        assertEquals(1, sessionsToday?.size)
        assertEquals("Algebra", sessionsToday?.first()?.lessonTitle)
    }

    @Test
    fun `goToNextMonth and goToPreviousMonth update the visible month`() = runTest {
        every { attendanceRepository.getSessionsInRange(any(), any()) } returns flowOf(emptyList())
        val viewModel = CalendarViewModel(attendanceRepository, recurringSessionGenerator)
        val startingMonth = viewModel.uiState.value.yearMonth

        viewModel.goToNextMonth()
        assertEquals(startingMonth.plusMonths(1), viewModel.uiState.value.yearMonth)

        viewModel.goToPreviousMonth()
        assertEquals(startingMonth, viewModel.uiState.value.yearMonth)
    }
}
