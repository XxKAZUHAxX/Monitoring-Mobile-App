package com.example.lessonmonitor.domain.repository

import com.example.lessonmonitor.data.local.entity.StudentEntity
import kotlinx.coroutines.flow.Flow

/**
 * Phase-1 local-only Student data access. Listing/searching/looking up
 * students plus full profile CRUD (Student Profiles milestone, #8) and the
 * minimal (name-only) creation used by the Attendance Tracking milestone's
 * (#7) roster "quick add" flow.
 */
interface StudentRepository {
    fun getAll(): Flow<List<StudentEntity>>

    fun search(query: String): Flow<List<StudentEntity>>

    fun getById(studentId: Long): Flow<StudentEntity?>

    /** [phone]/[email]/[notes]/[photoPath] default to null for the roster "quick add" flow's name-only creation. */
    suspend fun create(
        name: String,
        phone: String? = null,
        email: String? = null,
        notes: String? = null,
        photoPath: String? = null
    ): Long

    suspend fun update(student: StudentEntity)

    suspend fun delete(student: StudentEntity)

    /** Exact impact counts for the cascade-delete confirmation dialog (PLAN.md §1 assumption #9). */
    suspend fun getDeleteImpact(studentId: Long): StudentDeleteImpact
}

/** Mirrors the Student cascade-delete matrix in PLAN.md §2 ("Enrollments, AttendanceRecords referencing them"). */
data class StudentDeleteImpact(
    val enrollmentCount: Int,
    val recordCount: Int
)

