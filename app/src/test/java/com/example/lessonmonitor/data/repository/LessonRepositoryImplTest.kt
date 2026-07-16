package com.example.lessonmonitor.data.repository

import com.example.lessonmonitor.data.local.dao.AttendanceRecordDao
import com.example.lessonmonitor.data.local.dao.EnrollmentDao
import com.example.lessonmonitor.data.local.dao.LessonDao
import com.example.lessonmonitor.data.local.entity.LessonEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class LessonRepositoryImplTest {

    private val lessonDao: LessonDao = mockk()
    private val enrollmentDao: EnrollmentDao = mockk()
    private val attendanceRecordDao: AttendanceRecordDao = mockk()

    private lateinit var repository: LessonRepositoryImpl

    @Before
    fun setUp() {
        repository = LessonRepositoryImpl(lessonDao, enrollmentDao, attendanceRecordDao)
    }

    @Test
    fun `create sets sortOrder to max plus one`() = runTest {
        coEvery { lessonDao.getMaxSortOrder(1L) } returns 3
        val slot = slot<LessonEntity>()
        coEvery { lessonDao.insert(capture(slot)) } returns 100L

        val id = repository.create(
            categoryId = 1L, title = "Test", description = null,
            facilitatorName = null, place = null, startDate = 19000L, startTime = null
        )

        assertEquals(100L, id)
        assertEquals(4, slot.captured.sortOrder)
    }

    @Test
    fun `create sets sortOrder to zero when no existing lessons`() = runTest {
        coEvery { lessonDao.getMaxSortOrder(1L) } returns null
        val slot = slot<LessonEntity>()
        coEvery { lessonDao.insert(capture(slot)) } returns 101L

        repository.create(
            categoryId = 1L, title = "Test", description = null,
            facilitatorName = null, place = null, startDate = 19000L, startTime = null
        )

        assertEquals(0, slot.captured.sortOrder)
    }

    @Test
    fun `update sets updatedAt to now`() = runTest {
        val lesson = LessonEntity(
            id = 1L, categoryId = 1L, title = "Test",
            startDate = 19000L, createdAt = 1L, updatedAt = 1L
        )
        coEvery { lessonDao.update(any()) } returns Unit
        repository.update(lesson)
        coVerify { lessonDao.update(match { it.updatedAt > 1L }) }
    }

    @Test
    fun `getDeleteImpact returns record count`() = runTest {
        coEvery { attendanceRecordDao.countForLesson(1L) } returns 5
        val impact = repository.getDeleteImpact(1L)
        assertEquals(0, impact.enrollmentCount)
        assertEquals(5, impact.recordCount)
    }

    @Test
    fun `reorderLessons updates sort order for each lesson`() = runTest {
        coEvery { lessonDao.updateSortOrder(any(), any()) } returns Unit
        repository.reorderLessons(1L, listOf(3L, 1L, 2L))
        coVerify { lessonDao.updateSortOrder(3L, 0) }
        coVerify { lessonDao.updateSortOrder(1L, 1) }
        coVerify { lessonDao.updateSortOrder(2L, 2) }
    }
}
