package com.example.lessonmonitor.data.repository

import com.example.lessonmonitor.data.local.dao.AttendanceRecordDao
import com.example.lessonmonitor.data.local.dao.AttendanceSessionDao
import com.example.lessonmonitor.data.local.entity.AttendanceRecordEntity
import com.example.lessonmonitor.data.local.entity.AttendanceSessionEntity
import com.example.lessonmonitor.data.local.entity.AttendanceStatus
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class AttendanceRepositoryImplTest {

    private val attendanceSessionDao: AttendanceSessionDao = mockk()
    private val attendanceRecordDao: AttendanceRecordDao = mockk()

    private lateinit var repository: AttendanceRepositoryImpl

    @Before
    fun setUp() {
        repository = AttendanceRepositoryImpl(attendanceSessionDao, attendanceRecordDao)
    }

    @Test
    fun `createSession returns the newly inserted id`() = runTest {
        coEvery { attendanceSessionDao.insert(any()) } returns 7L

        val id = repository.createSession(lessonId = 1L, sessionDate = 19000L)

        assertEquals(7L, id)
    }

    @Test
    fun `createSession falls back to the existing session id on a unique-index conflict`() = runTest {
        coEvery { attendanceSessionDao.insert(any()) } returns -1L
        val existing = AttendanceSessionEntity(id = 3L, lessonId = 1L, sessionDate = 19000L, createdAt = 1L)
        coEvery { attendanceSessionDao.getByLessonAndDate(1L, 19000L) } returns existing

        val id = repository.createSession(lessonId = 1L, sessionDate = 19000L)

        assertEquals(3L, id)
    }

    @Test
    fun `markAttendance upserts a record with the given status and reason`() = runTest {
        val slot = slot<AttendanceRecordEntity>()
        coEvery { attendanceRecordDao.upsert(capture(slot)) } returns 1L

        repository.markAttendance(sessionId = 5L, studentId = 2L, status = AttendanceStatus.ABSENT, absenceReason = "Sick")

        assertEquals(5L, slot.captured.sessionId)
        assertEquals(2L, slot.captured.studentId)
        assertEquals(AttendanceStatus.ABSENT, slot.captured.status)
        assertEquals("Sick", slot.captured.absenceReason)
    }
}
