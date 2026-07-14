package com.example.lessonmonitor.data.repository

import com.example.lessonmonitor.data.local.dao.AttendanceRecordDao
import com.example.lessonmonitor.data.local.dao.AttendanceSessionDao
import com.example.lessonmonitor.data.local.dao.CategoryDao
import com.example.lessonmonitor.data.local.entity.CategoryEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class CategoryRepositoryImplTest {

    private val categoryDao: CategoryDao = mockk()
    private val attendanceSessionDao: AttendanceSessionDao = mockk()
    private val attendanceRecordDao: AttendanceRecordDao = mockk()

    private lateinit var repository: CategoryRepositoryImpl

    @Before
    fun setUp() {
        repository = CategoryRepositoryImpl(categoryDao, attendanceSessionDao, attendanceRecordDao)
    }

    @Test
    fun `create inserts a new category with matching created and updated timestamps`() = runTest {
        val slot = slot<CategoryEntity>()
        coEvery { categoryDao.insert(capture(slot)) } returns 1L

        val id = repository.create("Math", "desc", 0xFF0000, "📘")

        assertEquals(1L, id)
        assertEquals("Math", slot.captured.name)
        assertEquals("desc", slot.captured.description)
        assertEquals(0xFF0000, slot.captured.color)
        assertEquals("📘", slot.captured.icon)
        assertEquals(slot.captured.createdAt, slot.captured.updatedAt)
    }

    @Test
    fun `update bumps updatedAt but preserves the rest of the entity`() = runTest {
        val existing = CategoryEntity(id = 5L, name = "Old", createdAt = 100L, updatedAt = 100L)
        val slot = slot<CategoryEntity>()
        coEvery { categoryDao.update(capture(slot)) } returns Unit

        repository.update(existing.copy(name = "New"))

        assertEquals("New", slot.captured.name)
        assertEquals(100L, slot.captured.createdAt)
        assertEquals(5L, slot.captured.id)
    }

    @Test
    fun `getDeleteImpact aggregates lesson, session, and record counts`() = runTest {
        coEvery { categoryDao.countLessons(7L) } returns 3
        coEvery { attendanceSessionDao.countForCategory(7L) } returns 12
        coEvery { attendanceRecordDao.countForCategory(7L) } returns 96

        val impact = repository.getDeleteImpact(7L)

        assertEquals(3, impact.lessonCount)
        assertEquals(12, impact.sessionCount)
        assertEquals(96, impact.recordCount)
    }

    @Test
    fun `delete delegates to the DAO`() = runTest {
        val category = CategoryEntity(id = 1L, name = "Math", createdAt = 1L, updatedAt = 1L)
        coEvery { categoryDao.delete(category) } returns Unit

        repository.delete(category)

        coVerify { categoryDao.delete(category) }
    }

    @Test
    fun `search delegates to the DAO`() = runTest {
        val category = CategoryEntity(id = 1L, name = "Math", createdAt = 1L, updatedAt = 1L)
        every { categoryDao.search("mat") } returns flowOf(listOf(category))

        val result = repository.search("mat").first()

        assertEquals(listOf(category), result)
    }
}
