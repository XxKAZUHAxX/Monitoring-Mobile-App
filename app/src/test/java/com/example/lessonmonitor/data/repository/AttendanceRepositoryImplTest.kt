package com.example.lessonmonitor.data.repository

import com.example.lessonmonitor.data.local.dao.AttendanceRecordDao
import com.example.lessonmonitor.data.local.dao.LessonDao
import com.example.lessonmonitor.data.local.entity.AttendanceRecordEntity
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AttendanceRepositoryImplTest {

    private val attendanceRecordDao: AttendanceRecordDao = mockk()
    private val lessonDao: LessonDao = mockk()

    private lateinit var repository: AttendanceRepositoryImpl

    @Before
    fun setUp() {
        repository = AttendanceRepositoryImpl(attendanceRecordDao, lessonDao)
    }

    @Test
    fun `markAttendance upserts a record with the given status, reason, and completed`() = runTest {
        val slot = slot<AttendanceRecordEntity>()
        coEvery { attendanceRecordDao.upsert(capture(slot)) } returns 1L

        repository.markAttendance(
            lessonId = 5L, studentId = 2L,
            status = AttendanceStatus.ABSENT, absenceReason = "Sick", completed = false
        )

        assertEquals(5L, slot.captured.lessonId)
        assertEquals(2L, slot.captured.studentId)
        assertEquals(AttendanceStatus.ABSENT, slot.captured.status)
        assertEquals("Sick", slot.captured.absenceReason)
        assertFalse(slot.captured.completed)
    }

    @Test
    fun `markAttendance resets completed to false when status is not PRESENT`() = runTest {
        val slot = slot<AttendanceRecordEntity>()
        coEvery { attendanceRecordDao.upsert(capture(slot)) } returns 1L

        repository.markAttendance(
            lessonId = 5L, studentId = 2L,
            status = AttendanceStatus.ABSENT, absenceReason = null, completed = true
        )

        assertFalse(slot.captured.completed)
    }

    @Test
    fun `markAttendance keeps completed true when status is PRESENT`() = runTest {
        val slot = slot<AttendanceRecordEntity>()
        coEvery { attendanceRecordDao.upsert(capture(slot)) } returns 1L

        repository.markAttendance(
            lessonId = 5L, studentId = 2L,
            status = AttendanceStatus.PRESENT, absenceReason = null, completed = true
        )

        assertTrue(slot.captured.completed)
    }

    @Test
    fun `getHistoryForStudent joins records with lesson titles, newest first`() = runTest {
        val olderRecord = AttendanceRecordEntity(id = 1L, lessonId = 10L, studentId = 2L, status = AttendanceStatus.PRESENT, recordedAt = 1L)
        val newerRecord = AttendanceRecordEntity(id = 2L, lessonId = 11L, studentId = 2L, status = AttendanceStatus.ABSENT, recordedAt = 2L)
        every { attendanceRecordDao.getForStudent(2L) } returns flowOf(listOf(olderRecord, newerRecord))
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
    fun `getHistoryForStudent falls back to placeholder title for orphan lesson`() = runTest {
        val orphanRecord = AttendanceRecordEntity(id = 1L, lessonId = 99L, studentId = 2L, status = AttendanceStatus.PRESENT, recordedAt = 1L)
        every { attendanceRecordDao.getForStudent(2L) } returns flowOf(listOf(orphanRecord))
        every { lessonDao.getAll() } returns flowOf(emptyList())

        val history = repository.getHistoryForStudent(2L).first()

        assertEquals("Unknown lesson", history[0].lessonTitle)
    }

    @Test
    fun `getStudentAttendanceStats combines the present and total counts`() = runTest {
        coEvery { attendanceRecordDao.countPresentForStudent(2L) } returns 7
        coEvery { attendanceRecordDao.countForStudent(2L) } returns 10

        val stats = repository.getStudentAttendanceStats(2L)

        assertEquals(7, stats.presentCount)
        assertEquals(10, stats.totalCount)
        assertEquals(0.7f, stats.presentRate)
    }

    @Test
    fun `getStudentAttendanceStats presentRate is 0 when there are no records yet`() = runTest {
        coEvery { attendanceRecordDao.countPresentForStudent(2L) } returns 0
        coEvery { attendanceRecordDao.countForStudent(2L) } returns 0

        val stats = repository.getStudentAttendanceStats(2L)

        assertEquals(0f, stats.presentRate)
    }

    @Test
    fun `getLessonAttendanceStats combines the present and total counts`() = runTest {
        coEvery { attendanceRecordDao.countPresentForLesson(4L) } returns 3
        coEvery { attendanceRecordDao.countForLesson(4L) } returns 12

        val stats = repository.getLessonAttendanceStats(4L)

        assertEquals(3, stats.presentCount)
        assertEquals(12, stats.totalCount)
        assertEquals(0.25f, stats.presentRate)
    }
}
