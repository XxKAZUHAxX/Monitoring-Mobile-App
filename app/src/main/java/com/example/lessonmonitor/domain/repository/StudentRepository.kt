package com.example.lessonmonitor.domain.repository

import com.example.lessonmonitor.data.local.entity.StudentEntity
import kotlinx.coroutines.flow.Flow

/**
 * Phase-1 local-only Student data access. Only what the Attendance Tracking
 * milestone (#7) needs — listing/searching/looking up students and a
 * minimal (name-only) creation for the roster "quick add" flow. Full profile
 * fields (photo/phone/email/notes) plus update/delete (with the Student
 * cascade-delete confirmation from PLAN.md §1 assumption #9) land in the
 * Student Profiles milestone (#8).
 */
interface StudentRepository {
    fun getAll(): Flow<List<StudentEntity>>

    fun search(query: String): Flow<List<StudentEntity>>

    fun getById(studentId: Long): Flow<StudentEntity?>

    /** Minimal (name-only) creation used by the roster "quick add" flow. */
    suspend fun create(name: String): Long
}
