package com.example.lessonmonitor.data.repository

import com.example.lessonmonitor.data.local.dao.EnrollmentDao
import com.example.lessonmonitor.data.local.dao.StudentDao
import com.example.lessonmonitor.data.local.entity.EnrollmentEntity
import com.example.lessonmonitor.data.local.entity.StudentEntity
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

class EnrollmentRepositoryImplTest {

    private val enrollmentDao: EnrollmentDao = mockk()
    private val studentDao: StudentDao = mockk()

    private lateinit var repository: EnrollmentRepositoryImpl

    @Before
    fun setUp() {
        repository = EnrollmentRepositoryImpl(enrollmentDao, studentDao)
    }

    @Test
    fun `getRosterForLesson joins enrollments with their students and sorts by name`() = runTest {
        val ana = StudentEntity(id = 1L, name = "Ana", createdAt = 1L, updatedAt = 1L)
        val bo = StudentEntity(id = 2L, name = "Bo", createdAt = 1L, updatedAt = 1L)
        val enrollmentBo = EnrollmentEntity(id = 10L, lessonId = 5L, studentId = 2L, enrolledAt = 1L)
        val enrollmentAna = EnrollmentEntity(id = 11L, lessonId = 5L, studentId = 1L, enrolledAt = 1L)
        every { enrollmentDao.getActiveForLesson(5L) } returns flowOf(listOf(enrollmentBo, enrollmentAna))
        every { studentDao.getAll() } returns flowOf(listOf(ana, bo))

        val roster = repository.getRosterForLesson(5L).first()

        assertEquals(listOf("Ana", "Bo"), roster.map { it.student.name })
    }

    @Test
    fun `getRosterForLesson drops enrollments whose student can't be found`() = runTest {
        val ana = StudentEntity(id = 1L, name = "Ana", createdAt = 1L, updatedAt = 1L)
        val orphanEnrollment = EnrollmentEntity(id = 10L, lessonId = 5L, studentId = 99L, enrolledAt = 1L)
        every { enrollmentDao.getActiveForLesson(5L) } returns flowOf(listOf(orphanEnrollment))
        every { studentDao.getAll() } returns flowOf(listOf(ana))

        val roster = repository.getRosterForLesson(5L).first()

        assertEquals(emptyList<Any>(), roster)
    }

    @Test
    fun `enroll upserts an active enrollment`() = runTest {
        val slot = slot<EnrollmentEntity>()
        coEvery { enrollmentDao.upsert(capture(slot)) } returns 1L

        repository.enroll(lessonId = 5L, studentId = 1L)

        assertEquals(5L, slot.captured.lessonId)
        assertEquals(1L, slot.captured.studentId)
        assertEquals(true, slot.captured.active)
    }

    @Test
    fun `unenroll deactivates the enrollment without deleting it`() = runTest {
        val enrollment = EnrollmentEntity(id = 10L, lessonId = 5L, studentId = 1L, enrolledAt = 1L, active = true)
        val slot = slot<EnrollmentEntity>()
        coEvery { enrollmentDao.update(capture(slot)) } returns Unit

        repository.unenroll(enrollment)

        assertEquals(false, slot.captured.active)
        assertEquals(10L, slot.captured.id)
    }
}
