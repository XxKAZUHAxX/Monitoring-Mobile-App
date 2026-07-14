package com.example.lessonmonitor.domain.repository

import com.example.lessonmonitor.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * Phase-1 local-only Category data access (Feature: Category & Lesson CRUD,
 * PLAN.md §7 milestone 6). Wraps `CategoryDao` only for now — the Phase-2
 * cloud-sync swap point per PLAN.md §3 roadblock #6.
 */
interface CategoryRepository {
    fun getAll(): Flow<List<CategoryEntity>>

    fun getById(categoryId: Long): Flow<CategoryEntity?>

    /** Backs the global Search screen (PLAN.md §1 assumption #7, milestone #11). */
    fun search(query: String): Flow<List<CategoryEntity>>

    suspend fun create(name: String, description: String?, color: Int?, icon: String?): Long

    suspend fun update(category: CategoryEntity)

    suspend fun delete(category: CategoryEntity)

    /** Exact impact counts for the cascade-delete confirmation dialog (PLAN.md §1 assumption #3). */
    suspend fun getDeleteImpact(categoryId: Long): CategoryDeleteImpact
}

/** Mirrors the Category cascade-delete matrix in PLAN.md §2 ("Lessons → Enrollments, AttendanceSessions → AttendanceRecords"). */
data class CategoryDeleteImpact(
    val lessonCount: Int,
    val sessionCount: Int,
    val recordCount: Int
)
