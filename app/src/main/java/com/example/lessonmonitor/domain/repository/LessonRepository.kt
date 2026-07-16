package com.example.lessonmonitor.domain.repository

import com.example.lessonmonitor.data.local.entity.LessonEntity
import kotlinx.coroutines.flow.Flow

/**
 * Phase-1 local-only Lesson data access. Each lesson is a single occurrence
 * (recurring lessons have been removed). Wraps `LessonDao` only for now —
 * the Phase-2 cloud-sync swap point per PLAN.md §3 roadblock #6.
 */
interface LessonRepository {
    fun getAllByCategory(categoryId: Long): Flow<List<LessonEntity>>

    /** Global (cross-category) list, backing the Statistics dashboard. */
    fun getAll(): Flow<List<LessonEntity>>

    fun getById(lessonId: Long): Flow<LessonEntity?>

    /** Global (cross-category) title search, backing the Search screen. */
    fun search(query: String): Flow<List<LessonEntity>>

    suspend fun create(
        categoryId: Long,
        title: String,
        description: String?,
        facilitatorName: String?,
        place: String?,
        startDate: Long,
        startTime: Int?
    ): Long

    suspend fun update(lesson: LessonEntity)

    suspend fun delete(lesson: LessonEntity)

    /** Exact impact counts for the cascade-delete confirmation dialog. */
    suspend fun getDeleteImpact(lessonId: Long): LessonDeleteImpact

    // Sort-order helpers for drag-to-reorder

    suspend fun getMaxSortOrder(categoryId: Long): Int?

    suspend fun updateSortOrder(lessonId: Long, sortOrder: Int)

    suspend fun reorderLessons(categoryId: Long, orderedIds: List<Long>)
}

data class LessonDeleteImpact(
    val enrollmentCount: Int,
    val recordCount: Int
)
