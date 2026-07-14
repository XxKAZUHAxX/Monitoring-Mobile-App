package com.example.lessonmonitor.data.repository

import com.example.lessonmonitor.data.local.dao.AttendanceRecordDao
import com.example.lessonmonitor.data.local.dao.AttendanceSessionDao
import com.example.lessonmonitor.data.local.dao.LessonDao
import com.example.lessonmonitor.data.local.entity.AttendanceRecordEntity
import com.example.lessonmonitor.data.local.entity.AttendanceSessionEntity
import com.example.lessonmonitor.data.local.entity.AttendanceStatus
import com.example.lessonmonitor.data.local.entity.LessonEntity
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class AttendanceRepositoryImplTest {

    private val attendanceSessionDao: AttendanceSessionDao = mockk()
    private val attendanceRecordDao: AttendanceRecordDao = mockk()
    private val lessonDao: LessonDao = mockk()

    private lateinit var repository: AttendanceRepositoryImpl

    @Before
    fun setUp() {
        repository = AttendanceRepositoryImpl(attendanceSessionDao, attendanceRecordDao, lessonDao)
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

    @Test
    fun `getHistoryForStudent joins records with their session and lesson title, newest first`() = runTest {
        val olderSession = AttendanceSessionEntity(id = 1L, lessonId = 10L, sessionDate = 19000L, createdAt = 1L)
        val newerSession = AttendanceSessionEntity(id = 2L, lessonId = 11L, sessionDate = 19100L, createdAt = 1L)
        val olderRecord = AttendanceRecordEntity(id = 1L, sessionId = 1L, studentId = 2L, status = AttendanceStatus.PRESENT, recordedAt = 1L)
        val newerRecord = AttendanceRecordEntity(id = 2L, sessionId = 2L, studentId = 2L, status = AttendanceStatus.ABSENT, recordedAt = 2L)
        every { attendanceRecordDao.getForStudent(2L) } returns flowOf(listOf(olderRecord, newerRecord))
        every { attendanceSessionDao.getAll() } returns flowOf(listOf(olderSession, newerSession))
        every { lessonDao.getAll() } returns flowOf(
            listOf(
                LessonEntity(id = 10L, categoryId = 1L, title = "Algebra", startDate = 1L, createdAt = 1L, updatedAt = 1L),
                LessonEntity(id = 11L, categoryId = 1L, title = "Geometry", startDate = 1L, createdAt = 1L, updatedAt = 1L)
            )
        )

        val history = repository.getHistoryForStudent(2L).first()

        assertEquals(listOf("Geometry", "Algebra"), history.map { it.lessonTitle })
    }

    @Test
    fun `getHistoryForStudent drops records whose session can't be found`() = runTest {
        val orphanRecord = AttendanceRecordEntity(id = 1L, sessionId = 99L, studentId = 2L, status = AttendanceStatus.PRESENT, recordedAt = 1L)
        every { attendanceRecordDao.getForStudent(2L) } returns flowOf(listOf(orphanRecord))
        every { attendanceSessionDao.getAll() } returns flowOf(emptyList())
        every { lessonDao.getAll() } returns flowOf(emptyList())

        val history = repository.getHistoryForStudent(2L).first()

        assertEquals(emptyList<Any>(), history)
    }

    @Test
    fun `getSessionsInRange joins sessions in range with their lesson title`() = runTest {
        val session = AttendanceSessionEntity(id = 1L, lessonId = 10L, sessionDate = 19000L, createdAt = 1L)
        every { attendanceSessionDao.getInDateRange(19000L, 19010L) } returns flowOf(listOf(session))
        every { lessonDao.getAll() } returns flowOf(
            listOf(LessonEntity(id = 10L, categoryId = 1L, title = "Algebra", startDate = 1L, createdAt = 1L, updatedAt = 1L))
        )

        val entries = repository.getSessionsInRange(19000L, 19010L).first()

        assertEquals(1, entries.size)
        assertEquals("Algebra", entries[0].lessonTitle)
        assertEquals(session, entries[0].session)
    }

    @Test
    fun `getSessionsInRange falls back to a placeholder title for an orphaned session`() = runTest {
        val session = AttendanceSessionEntity(id = 1L, lessonId = 99L, sessionDate = 19000L, createdAt = 1L)
        every { attendanceSessionDao.getInDateRange(19000L, 19010L) } returns flowOf(listOf(session))
        every { lessonDao.getAll() } returns flowOf(emptyList())

        val entries = repository.getSessionsInRange(19000L, 19010L).first()

        assertEquals("Unknown lesson", entries[0].lessonTitle)
    }
}
