package com.example.lessonmonitor.domain.schedule

import com.example.lessonmonitor.data.local.entity.LessonEntity
import com.example.lessonmonitor.data.local.entity.RecurrenceType
import com.example.lessonmonitor.domain.repository.AttendanceRepository
import com.example.lessonmonitor.domain.repository.LessonRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class RecurringSessionGeneratorTest {

    private val lessonRepository: LessonRepository = mockk()
    private val attendanceRepository: AttendanceRepository = mockk()

    private lateinit var generator: RecurringSessionGenerator

    @Before
    fun setUp() {
        generator = RecurringSessionGenerator(lessonRepository, attendanceRepository)
        coEvery { attendanceRepository.createSession(any(), any()) } returns 1L
    }

    private fun lesson(
        id: Long = 1L,
        recurrenceType: RecurrenceType,
        recurrenceDaysOfWeek: String? = null,
        startDate: Long,
        endDate: Long? = null
    ) = LessonEntity(
        id = id,
        categoryId = 1L,
        title = "Algebra",
        isRecurring = true,
        recurrenceType = recurrenceType,
        recurrenceDaysOfWeek = recurrenceDaysOfWeek,
        startDate = startDate,
        endDate = endDate,
        createdAt = 1L,
        updatedAt = 1L
    )

    @Test
    fun `daily lesson generates a session for every day in the window, inclusive`() = runTest {
        val today = LocalDate.of(2026, 1, 1)
        val recurring = lesson(recurrenceType = RecurrenceType.DAILY, startDate = today.toEpochDay())
        coEvery { lessonRepository.getAllRecurring() } returns listOf(recurring)

        generator.generateUpcomingSessions(today = today, windowDays = 5)

        for (offset in 0..5) {
            coVerify { attendanceRepository.createSession(recurring.id, today.toEpochDay() + offset) }
        }
    }

    @Test
    fun `weekly lesson only generates sessions on matching days of week`() = runTest {
        // 2026-01-05 is a Monday.
        val today = LocalDate.of(2026, 1, 5)
        val recurring = lesson(
            recurrenceType = RecurrenceType.WEEKLY,
            recurrenceDaysOfWeek = "1", // Monday
            startDate = today.toEpochDay()
        )
        coEvery { lessonRepository.getAllRecurring() } returns listOf(recurring)

        generator.generateUpcomingSessions(today = today, windowDays = 13)

        // Mondays in [2026-01-05, 2026-01-18]: 01-05 and 01-12.
        coVerify(exactly = 1) { attendanceRepository.createSession(recurring.id, LocalDate.of(2026, 1, 5).toEpochDay()) }
        coVerify(exactly = 1) { attendanceRepository.createSession(recurring.id, LocalDate.of(2026, 1, 12).toEpochDay()) }
        coVerify(exactly = 2) { attendanceRepository.createSession(recurring.id, any()) }
    }

    @Test
    fun `custom days lesson generates sessions only on its configured weekdays`() = runTest {
        // 2026-01-05 is Monday, 2026-01-07 is Wednesday.
        val today = LocalDate.of(2026, 1, 5)
        val recurring = lesson(
            recurrenceType = RecurrenceType.CUSTOM_DAYS,
            recurrenceDaysOfWeek = "1,3", // Monday, Wednesday
            startDate = today.toEpochDay()
        )
        coEvery { lessonRepository.getAllRecurring() } returns listOf(recurring)

        generator.generateUpcomingSessions(today = today, windowDays = 2)

        coVerify(exactly = 1) { attendanceRepository.createSession(recurring.id, LocalDate.of(2026, 1, 5).toEpochDay()) }
        coVerify(exactly = 1) { attendanceRepository.createSession(recurring.id, LocalDate.of(2026, 1, 7).toEpochDay()) }
        coVerify(exactly = 0) { attendanceRepository.createSession(recurring.id, LocalDate.of(2026, 1, 6).toEpochDay()) }
    }

    @Test
    fun `lesson whose start date is after the window end generates nothing`() = runTest {
        val today = LocalDate.of(2026, 1, 1)
        val recurring = lesson(
            recurrenceType = RecurrenceType.DAILY,
            startDate = today.toEpochDay() + 10
        )
        coEvery { lessonRepository.getAllRecurring() } returns listOf(recurring)

        generator.generateUpcomingSessions(today = today, windowDays = 5)

        coVerify(exactly = 0) { attendanceRepository.createSession(any(), any()) }
    }

    @Test
    fun `lesson end date clips generation before the window end`() = runTest {
        val today = LocalDate.of(2026, 1, 1)
        val recurring = lesson(
            recurrenceType = RecurrenceType.DAILY,
            startDate = today.toEpochDay(),
            endDate = today.toEpochDay() + 2
        )
        coEvery { lessonRepository.getAllRecurring() } returns listOf(recurring)

        generator.generateUpcomingSessions(today = today, windowDays = 10)

        coVerify(exactly = 3) { attendanceRepository.createSession(recurring.id, any()) }
    }

    @Test
    fun `NONE recurrence type generates nothing even if flagged recurring`() = runTest {
        val today = LocalDate.of(2026, 1, 1)
        val recurring = lesson(recurrenceType = RecurrenceType.NONE, startDate = today.toEpochDay())
        coEvery { lessonRepository.getAllRecurring() } returns listOf(recurring)

        generator.generateUpcomingSessions(today = today, windowDays = 5)

        coVerify(exactly = 0) { attendanceRepository.createSession(any(), any()) }
    }
}
