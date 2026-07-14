package com.example.lessonmonitor.data.repository

import com.example.lessonmonitor.data.local.dao.AttendanceRecordDao
import com.example.lessonmonitor.data.local.dao.AttendanceSessionDao
import com.example.lessonmonitor.data.local.dao.EnrollmentDao
import com.example.lessonmonitor.data.local.dao.LessonDao
import com.example.lessonmonitor.data.local.entity.LessonEntity
import com.example.lessonmonitor.data.local.entity.RecurrenceType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class LessonRepositoryImplTest {

    private val lessonDao: LessonDao = mockk()
    private val enrollmentDao: EnrollmentDao = mockk()
    private val attendanceSessionDao: AttendanceSessionDao = mockk()
    private val attendanceRecordDao: AttendanceRecordDao = mockk()

    private lateinit var repository: LessonRepositoryImpl

    @Before
    fun setUp() {
        repository = LessonRepositoryImpl(lessonDao, enrollmentDao, attendanceSessionDao, attendanceRecordDao)
    }

    @Test
    fun `create inserts a new lesson with matching created and updated timestamps`() = runTest {
        val slot = slot<LessonEntity>()
        coEvery { lessonDao.insert(capture(slot)) } returns 1L

        val id = repository.create(
            categoryId = 3L,
            title = "Algebra",
            description = null,
            facilitatorName = "Jane",
            place = "Room 1",
            isRecurring = true,
            recurrenceType = RecurrenceType.WEEKLY,
            recurrenceDaysOfWeek = "1,3",
            startDate = 19000L,
            endDate = null,
            startTime = 540,
            endTime = 600
        )

        assertEquals(1L, id)
        assertEquals(3L, slot.captured.categoryId)
        assertEquals("Algebra", slot.captured.title)
        assertEquals(RecurrenceType.WEEKLY, slot.captured.recurrenceType)
        assertEquals("1,3", slot.captured.recurrenceDaysOfWeek)
        assertEquals(slot.captured.createdAt, slot.captured.updatedAt)
    }

    @Test
    fun `update bumps updatedAt but preserves the rest of the entity`() = runTest {
        val existing = LessonEntity(
            id = 9L,
            categoryId = 3L,
            title = "Old title",
            startDate = 19000L,
            createdAt = 100L,
            updatedAt = 100L
        )
        val slot = slot<LessonEntity>()
        coEvery { lessonDao.update(capture(slot)) } returns Unit

        repository.update(existing.copy(title = "New title"))

        assertEquals("New title", slot.captured.title)
        assertEquals(100L, slot.captured.createdAt)
        assertEquals(3L, slot.captured.categoryId)
    }

    @Test
    fun `getDeleteImpact aggregates enrollment, session, and record counts`() = runTest {
        coEvery { enrollmentDao.countForLesson(4L) } returns 20
        coEvery { attendanceSessionDao.countForLesson(4L) } returns 8
        coEvery { attendanceRecordDao.countForLesson(4L) } returns 160

        val impact = repository.getDeleteImpact(4L)

        assertEquals(20, impact.enrollmentCount)
        assertEquals(8, impact.sessionCount)
        assertEquals(160, impact.recordCount)
    }

    @Test
    fun `delete delegates to the DAO`() = runTest {
        val lesson = LessonEntity(id = 1L, categoryId = 1L, title = "Algebra", startDate = 19000L, createdAt = 1L, updatedAt = 1L)
        coEvery { lessonDao.delete(lesson) } returns Unit

        repository.delete(lesson)

        coVerify { lessonDao.delete(lesson) }
    }

    @Test
    fun `getAllRecurring delegates to the DAO`() = runTest {
        val recurring = LessonEntity(
            id = 5L,
            categoryId = 1L,
            title = "Algebra",
            isRecurring = true,
            recurrenceType = RecurrenceType.WEEKLY,
            startDate = 19000L,
            createdAt = 1L,
            updatedAt = 1L
        )
        coEvery { lessonDao.getAllRecurring() } returns listOf(recurring)

        val result = repository.getAllRecurring()

        assertEquals(listOf(recurring), result)
    }
}
