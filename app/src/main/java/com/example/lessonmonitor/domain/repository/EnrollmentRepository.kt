package com.example.lessonmonitor.domain.repository

import com.example.lessonmonitor.data.local.entity.EnrollmentEntity
import com.example.lessonmonitor.data.local.entity.StudentEntity
import kotlinx.coroutines.flow.Flow

/**
 * Manages which students are on a category's active roster (category-level
 * enrollment — a student enrolled in a category is on the roster for every
 * lesson in that category). Removing a student deactivates the row
 * (`active = false`) rather than deleting it, so attendance history is
 * never lost (records reference [studentId] directly).
 */
interface EnrollmentRepository {
    /** Active roster for a category, joined with each student's profile, sorted by name. */
    fun getRosterForCategory(categoryId: Long): Flow<List<RosterEntry>>

    /** Enrolls (or re-enrolls) a student onto a category's roster. */
    suspend fun enroll(categoryId: Long, studentId: Long)

    /** Removes a student from the roster by deactivating the enrollment (does not delete attendance history). */
    suspend fun unenroll(enrollment: EnrollmentEntity)
}

data class RosterEntry(
    val enrollment: EnrollmentEntity,
    val student: StudentEntity
)
