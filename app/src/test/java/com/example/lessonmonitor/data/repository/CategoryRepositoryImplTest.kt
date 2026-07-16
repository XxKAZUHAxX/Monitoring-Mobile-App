package com.example.lessonmonitor.data.repository

import com.example.lessonmonitor.data.local.dao.AttendanceRecordDao
import com.example.lessonmonitor.data.local.dao.CategoryDao
import com.example.lessonmonitor.data.local.entity.CategoryEntity
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

class CategoryRepositoryImplTest {

    private val categoryDao: CategoryDao = mockk()
    private val attendanceRecordDao: AttendanceRecordDao = mockk()

    private lateinit var repository: CategoryRepositoryImpl

    @Before
    fun setUp() {
        repository = CategoryRepositoryImpl(categoryDao, attendanceRecordDao)
    }

    @Test
    fun `create sets sortOrder to max plus one`() = runTest {
        coEvery { categoryDao.getMaxSortOrder() } returns 5
        val slot = slot<CategoryEntity>()
        coEvery { categoryDao.insert(capture(slot)) } returns 10L

        val id = repository.create("Test", null, null, null)

        assertEquals(10L, id)
        assertEquals(6, slot.captured.sortOrder)
    }

    @Test
    fun `create sets sortOrder to zero when no existing categories`() = runTest {
        coEvery { categoryDao.getMaxSortOrder() } returns null
        val slot = slot<CategoryEntity>()
        coEvery { categoryDao.insert(capture(slot)) } returns 10L

        repository.create("Test", null, null, null)

        assertEquals(0, slot.captured.sortOrder)
    }

    @Test
    fun `getAll delegates to dao`() = runTest {
        every { categoryDao.getAll() } returns flowOf(emptyList())
        repository.getAll()
    }

    @Test
    fun `update sets updatedAt to now`() = runTest {
        val cat = CategoryEntity(id = 1L, name = "Test", createdAt = 1L, updatedAt = 1L)
        coEvery { categoryDao.update(any()) } returns Unit
        repository.update(cat)
        coVerify { categoryDao.update(match { it.updatedAt > 1L }) }
    }

    @Test
    fun `getDeleteImpact returns lesson and record counts`() = runTest {
        coEvery { categoryDao.countLessons(1L) } returns 3
        coEvery { attendanceRecordDao.countForCategory(1L) } returns 10
        val impact = repository.getDeleteImpact(1L)
        assertEquals(3, impact.lessonCount)
        assertEquals(10, impact.recordCount)
    }

    @Test
    fun `reorderCategories updates sort order for each category`() = runTest {
        coEvery { categoryDao.updateSortOrder(any(), any()) } returns Unit
        repository.reorderCategories(listOf(3L, 1L, 2L))
        coVerify { categoryDao.updateSortOrder(3L, 0) }
        coVerify { categoryDao.updateSortOrder(1L, 1) }
        coVerify { categoryDao.updateSortOrder(2L, 2) }
    }
}
