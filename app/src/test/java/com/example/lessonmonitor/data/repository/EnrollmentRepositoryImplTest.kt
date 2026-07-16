package com.example.lessonmonitor.data.repository

import com.example.lessonmonitor.data.local.dao.EnrollmentDao
import com.example.lessonmonitor.data.local.dao.StudentDao
import com.example.lessonmonitor.data.local.entity.EnrollmentEntity
import com.example.lessonmonitor.data.local.entity.StudentEntity
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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
    fun `getRosterForCategory joins active enrollments with student profiles, sorted by name`() = runTest {
        val enrollment1 = EnrollmentEntity(id = 1L, categoryId = 5L, studentId = 10L, enrolledAt = 1L)
        val enrollment2 = EnrollmentEntity(id = 2L, categoryId = 5L, studentId = 11L, enrolledAt = 1L)
        every { enrollmentDao.getActiveForCategory(5L) } returns flowOf(listOf(enrollment1, enrollment2))
        every { studentDao.getAll() } returns flowOf(
            listOf(
                StudentEntity(id = 10L, name = "Zoe", createdAt = 1L, updatedAt = 1L),
                StudentEntity(id = 11L, name = "Ada", createdAt = 1L, updatedAt = 1L)
            )
        )

        val roster = repository.getRosterForCategory(5L).first()

        assertEquals(listOf("Ada", "Zoe"), roster.map { it.student.name })
    }

    @Test
    fun `getRosterForCategory omits enrollments whose student is missing`() = runTest {
        every { enrollmentDao.getActiveForCategory(5L) } returns flowOf(
            listOf(EnrollmentEntity(id = 1L, categoryId = 5L, studentId = 99L, enrolledAt = 1L))
        )
        every { studentDao.getAll() } returns flowOf(emptyList())

        val roster = repository.getRosterForCategory(5L).first()

        assertTrue(roster.isEmpty())
    }

    @Test
    fun `enroll upserts an active enrollment for the category`() = runTest {
        repository.enroll(categoryId = 5L, studentId = 10L)

        coVerify { enrollmentDao.upsert(match { it.categoryId == 5L && it.studentId == 10L && it.active }) }
    }

    @Test
    fun `unenroll sets active to false`() = runTest {
        val enrollment = EnrollmentEntity(id = 1L, categoryId = 5L, studentId = 10L, enrolledAt = 1L, active = true)

        repository.unenroll(enrollment)

        coVerify { enrollmentDao.update(match { it.active == false }) }
    }
}
