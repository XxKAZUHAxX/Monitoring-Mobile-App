package com.example.lessonmonitor.data.repository

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

    private lateinit var repository: StudentRepositoryImpl

    @Before
    fun setUp() {
        repository = StudentRepositoryImpl(studentDao)
    }

    @Test
    fun `create inserts a new student with matching created and updated timestamps`() = runTest {
        val slot = slot<StudentEntity>()
        coEvery { studentDao.insert(capture(slot)) } returns 1L

        val id = repository.create("Ana")

        assertEquals(1L, id)
        assertEquals("Ana", slot.captured.name)
        assertEquals(slot.captured.createdAt, slot.captured.updatedAt)
    }

    @Test
    fun `getAll delegates to the DAO`() = runTest {
        val student = StudentEntity(id = 1L, name = "Ana", createdAt = 1L, updatedAt = 1L)
        every { studentDao.getAll() } returns flowOf(listOf(student))

        assertEquals(listOf(student), repository.getAll().first())
    }
}

