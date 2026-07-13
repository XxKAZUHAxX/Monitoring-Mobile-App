package com.example.lessonmonitor.domain.repository

import com.example.lessonmonitor.data.local.entity.EnrollmentEntity
import com.example.lessonmonitor.data.local.entity.StudentEntity
import kotlinx.coroutines.flow.Flow

/**
 * Manages which students are on a lesson's active roster (PLAN.md §2
 * `Enrollment`). Removing a student from a roster deactivates the row
 * (`active = false`) rather than deleting it, so a student's
 * [com.example.lessonmonitor.data.local.entity.AttendanceRecordEntity]
 * history for that lesson is never lost (PLAN.md §3 roadblock #5).
 */
interface EnrollmentRepository {
    /** Active roster for a lesson, joined with each student's profile, sorted by name. */
    fun getRosterForLesson(lessonId: Long): Flow<List<RosterEntry>>

    /** Enrolls (or re-enrolls) a student onto a lesson's roster. */
    suspend fun enroll(lessonId: Long, studentId: Long)

    /** Removes a student from the roster by deactivating the enrollment (does not delete attendance history). */
    suspend fun unenroll(enrollment: EnrollmentEntity)
}

data class RosterEntry(
    val enrollment: EnrollmentEntity,
    val student: StudentEntity
)
