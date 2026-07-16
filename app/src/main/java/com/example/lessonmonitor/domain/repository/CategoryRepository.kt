package com.example.lessonmonitor.domain.repository

import com.example.lessonmonitor.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * Phase-1 local-only Category data access. Wraps `CategoryDao` only for now —
 * the Phase-2 cloud-sync swap point per PLAN.md §3 roadblock #6.
 */
interface CategoryRepository {
    fun getAll(): Flow<List<CategoryEntity>>

    fun getById(categoryId: Long): Flow<CategoryEntity?>

    /** Backs the global Search screen. */
    fun search(query: String): Flow<List<CategoryEntity>>

    suspend fun create(name: String, description: String?, color: Int?, icon: String?): Long

    suspend fun update(category: CategoryEntity)

    suspend fun delete(category: CategoryEntity)

    /** Exact impact counts for the cascade-delete confirmation dialog. */
    suspend fun getDeleteImpact(categoryId: Long): CategoryDeleteImpact

    // Sort-order helpers for drag-to-reorder

    suspend fun getMaxSortOrder(): Int?

    suspend fun updateSortOrder(categoryId: Long, sortOrder: Int)

    suspend fun reorderCategories(orderedIds: List<Long>)
}

data class CategoryDeleteImpact(
    val lessonCount: Int,
    val recordCount: Int
)
