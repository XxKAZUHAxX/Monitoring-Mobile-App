package com.example.lessonmonitor.data.repository

import com.example.lessonmonitor.data.local.dao.AttendanceRecordDao
import com.example.lessonmonitor.data.local.dao.EnrollmentDao
import com.example.lessonmonitor.data.local.dao.StudentDao
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

class StudentRepositoryImplTest {

    private val studentDao: StudentDao = mockk()
    private val enrollmentDao: EnrollmentDao = mockk()
    private val attendanceRecordDao: AttendanceRecordDao = mockk()

    private lateinit var repository: StudentRepositoryImpl

    @Before
    fun setUp() {
        repository = StudentRepositoryImpl(studentDao, enrollmentDao, attendanceRecordDao)
    }

    @Test
    fun `create inserts a new student with matching created and updated timestamps`() = runTest {
        val slot = slot<StudentEntity>()
        coEvery { studentDao.insert(capture(slot)) } returns 1L

        val id = repository.create("Ana", phone = "555", email = "ana@example.com", notes = "note", photoPath = "/path.jpg")

        assertEquals(1L, id)
        assertEquals("Ana", slot.captured.name)
        assertEquals("555", slot.captured.phone)
        assertEquals("ana@example.com", slot.captured.email)
        assertEquals("note", slot.captured.notes)
        assertEquals("/path.jpg", slot.captured.photoPath)
        assertEquals(slot.captured.createdAt, slot.captured.updatedAt)
    }

    @Test
    fun `create defaults optional fields to null for the quick-add flow`() = runTest {
        val slot = slot<StudentEntity>()
        coEvery { studentDao.insert(capture(slot)) } returns 1L

        repository.create("Ana")

        assertEquals(null, slot.captured.phone)
        assertEquals(null, slot.captured.email)
        assertEquals(null, slot.captured.notes)
        assertEquals(null, slot.captured.photoPath)
    }

    @Test
    fun `getAll delegates to the DAO`() = runTest {
        val student = StudentEntity(id = 1L, name = "Ana", createdAt = 1L, updatedAt = 1L)
        every { studentDao.getAll() } returns flowOf(listOf(student))

        assertEquals(listOf(student), repository.getAll().first())
    }

    @Test
    fun `update bumps updatedAt but preserves the rest of the entity`() = runTest {
        val existing = StudentEntity(id = 5L, name = "Old", createdAt = 100L, updatedAt = 100L)
        val slot = slot<StudentEntity>()
        coEvery { studentDao.update(capture(slot)) } returns Unit

        repository.update(existing.copy(name = "New"))

        assertEquals("New", slot.captured.name)
        assertEquals(100L, slot.captured.createdAt)
    }

    @Test
    fun `getDeleteImpact aggregates enrollment and record counts`() = runTest {
        coEvery { enrollmentDao.countForStudent(7L) } returns 3
        coEvery { attendanceRecordDao.countForStudent(7L) } returns 42

        val impact = repository.getDeleteImpact(7L)

        assertEquals(3, impact.enrollmentCount)
        assertEquals(42, impact.recordCount)
    }
}

