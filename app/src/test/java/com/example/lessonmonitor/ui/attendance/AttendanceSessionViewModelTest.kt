package com.example.lessonmonitor.ui.attendance

import com.example.lessonmonitor.MainDispatcherRule
import com.example.lessonmonitor.data.local.entity.AttendanceRecordEntity
import com.example.lessonmonitor.data.local.entity.AttendanceSessionEntity
import com.example.lessonmonitor.data.local.entity.AttendanceStatus
import com.example.lessonmonitor.data.local.entity.EnrollmentEntity
import com.example.lessonmonitor.data.local.entity.StudentEntity
import com.example.lessonmonitor.domain.repository.AttendanceRepository
import com.example.lessonmonitor.domain.repository.EnrollmentRepository
import com.example.lessonmonitor.domain.repository.RosterEntry
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class AttendanceSessionViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val attendanceRepository: AttendanceRepository = mockk()
    private val enrollmentRepository: EnrollmentRepository = mockk()

    private val ana = StudentEntity(id = 1L, name = "Ana", createdAt = 1L, updatedAt = 1L)
    private val bo = StudentEntity(id = 2L, name = "Bo", createdAt = 1L, updatedAt = 1L)
    private val session = AttendanceSessionEntity(id = 9L, lessonId = 5L, sessionDate = 19723L, createdAt = 1L)

    private fun buildViewModel(existingRecords: List<AttendanceRecordEntity> = emptyList()): AttendanceSessionViewModel {
        coEvery { attendanceRepository.getSession(9L) } returns flowOf(session)
        coEvery { enrollmentRepository.getRosterForLesson(5L) } returns flowOf(
            listOf(
                RosterEntry(EnrollmentEntity(id = 1L, lessonId = 5L, studentId = 1L, enrolledAt = 1L), ana),
                RosterEntry(EnrollmentEntity(id = 2L, lessonId = 5L, studentId = 2L, enrolledAt = 1L), bo)
            )
        )
        coEvery { attendanceRepository.getRecordsForSession(9L) } returns flowOf(existingRecords)
        return AttendanceSessionViewModel(attendanceRepository, enrollmentRepository)
    }

    @Test
    fun `load defaults every roster student to PRESENT when no records exist yet`() {
        val viewModel = buildViewModel()

        viewModel.load(lessonId = 5L, sessionId = 9L)

        val rows = viewModel.uiState.value.rows.associateBy { it.studentId }
        assertEquals(AttendanceStatus.PRESENT, rows[1L]?.status)
        assertEquals(AttendanceStatus.PRESENT, rows[2L]?.status)
    }

    @Test
    fun `load reflects an existing record's status and reason`() {
        val existing = AttendanceRecordEntity(
            id = 1L,
            sessionId = 9L,
            studentId = 2L,
            status = AttendanceStatus.ABSENT,
            absenceReason = "Sick",
            recordedAt = 1L
        )
        val viewModel = buildViewModel(listOf(existing))

        viewModel.load(lessonId = 5L, sessionId = 9L)

        val boRow = viewModel.uiState.value.rows.first { it.studentId == 2L }
        assertEquals(AttendanceStatus.ABSENT, boRow.status)
        assertEquals("Sick", boRow.reason)
    }

    @Test
    fun `onStatusChange clears the reason when moving away from ABSENT or EXCUSED`() {
        val existing = AttendanceRecordEntity(
            id = 1L,
            sessionId = 9L,
            studentId = 1L,
            status = AttendanceStatus.ABSENT,
            absenceReason = "Sick",
            recordedAt = 1L
        )
        val viewModel = buildViewModel(listOf(existing))
        viewModel.load(lessonId = 5L, sessionId = 9L)

        viewModel.onStatusChange(1L, AttendanceStatus.PRESENT)

        val row = viewModel.uiState.value.rows.first { it.studentId == 1L }
        assertEquals(AttendanceStatus.PRESENT, row.status)
        assertEquals("", row.reason)
    }

    @Test
    fun `submit persists every row, passing null reason for PRESENT and LATE`() {
        coEvery { attendanceRepository.markAttendance(any(), any(), any(), any()) } returns Unit
        val viewModel = buildViewModel()
        viewModel.load(lessonId = 5L, sessionId = 9L)
        viewModel.onStatusChange(2L, AttendanceStatus.ABSENT)
        viewModel.onReasonChange(2L, "Sick")

        var onSavedCalled = false
        viewModel.submit { onSavedCalled = true }

        assertEquals(true, onSavedCalled)
        coVerify { attendanceRepository.markAttendance(9L, 1L, AttendanceStatus.PRESENT, null) }
        coVerify { attendanceRepository.markAttendance(9L, 2L, AttendanceStatus.ABSENT, "Sick") }
    }
}
