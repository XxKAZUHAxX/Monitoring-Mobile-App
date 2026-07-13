package com.example.lessonmonitor.domain.repository

import com.example.lessonmonitor.data.local.entity.LessonEntity
import com.example.lessonmonitor.data.local.entity.RecurrenceType
import kotlinx.coroutines.flow.Flow

/**
 * Phase-1 local-only Lesson data access (Feature: Category & Lesson CRUD,
 * PLAN.md §7 milestone 6). Wraps `LessonDao` only for now — the Phase-2
 * cloud-sync swap point per PLAN.md §3 roadblock #6.
 *
 * Rolling-window recurring *session* generation from a recurring lesson's
 * config lands in the Recurring/Scheduled Lessons milestone (#9); this
 * repository only persists the lesson template itself.
 */
interface LessonRepository {
    fun getAllByCategory(categoryId: Long): Flow<List<LessonEntity>>

    fun getById(lessonId: Long): Flow<LessonEntity?>

    suspend fun create(
        categoryId: Long,
        title: String,
        description: String?,
        facilitatorName: String?,
        place: String?,
        isRecurring: Boolean,
        recurrenceType: RecurrenceType,
        recurrenceDaysOfWeek: String?,
        startDate: Long,
        endDate: Long?,
        startTime: Int?,
        endTime: Int?
    ): Long

    suspend fun update(lesson: LessonEntity)

    suspend fun delete(lesson: LessonEntity)

    /** Exact impact counts for the cascade-delete confirmation dialog (PLAN.md §1 assumption #3). */
    suspend fun getDeleteImpact(lessonId: Long): LessonDeleteImpact
}

/** Mirrors the Lesson cascade-delete matrix in PLAN.md §2 ("Enrollments, AttendanceSessions → AttendanceRecords"). */
data class LessonDeleteImpact(
    val enrollmentCount: Int,
    val sessionCount: Int,
    val recordCount: Int
)
