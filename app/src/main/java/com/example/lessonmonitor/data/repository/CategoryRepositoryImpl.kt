package com.example.lessonmonitor.data.repository

import com.example.lessonmonitor.data.local.dao.AttendanceRecordDao
import com.example.lessonmonitor.data.local.dao.CategoryDao
import com.example.lessonmonitor.data.local.entity.CategoryEntity
import com.example.lessonmonitor.domain.repository.CategoryDeleteImpact
import com.example.lessonmonitor.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao,
    private val attendanceRecordDao: AttendanceRecordDao
) : CategoryRepository {

    override fun getAll(): Flow<List<CategoryEntity>> = categoryDao.getAll()

    override fun getById(categoryId: Long): Flow<CategoryEntity?> = categoryDao.getById(categoryId)

    override fun search(query: String): Flow<List<CategoryEntity>> = categoryDao.search(query)

    override suspend fun create(name: String, description: String?, color: Int?, icon: String?): Long {
        val now = System.currentTimeMillis()
        val sortOrder = (categoryDao.getMaxSortOrder() ?: -1) + 1
        return categoryDao.insert(
            CategoryEntity(
                name = name,
                description = description,
                color = color,
                icon = icon,
                sortOrder = sortOrder,
                createdAt = now,
                updatedAt = now
            )
        )
    }

    override suspend fun update(category: CategoryEntity) {
        categoryDao.update(category.copy(updatedAt = System.currentTimeMillis()))
    }

    override suspend fun delete(category: CategoryEntity) {
        categoryDao.delete(category)
    }

    override suspend fun getDeleteImpact(categoryId: Long): CategoryDeleteImpact = CategoryDeleteImpact(
        lessonCount = categoryDao.countLessons(categoryId),
        recordCount = attendanceRecordDao.countForCategory(categoryId)
    )

    override suspend fun getMaxSortOrder(): Int? = categoryDao.getMaxSortOrder()

    override suspend fun updateSortOrder(categoryId: Long, sortOrder: Int) {
        categoryDao.updateSortOrder(categoryId, sortOrder)
    }

    override suspend fun reorderCategories(orderedIds: List<Long>) {
        orderedIds.forEachIndexed { index, categoryId ->
            categoryDao.updateSortOrder(categoryId, index)
        }
    }
}
