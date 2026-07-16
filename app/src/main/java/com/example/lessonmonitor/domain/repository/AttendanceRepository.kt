package com.example.lessonmonitor.domain.repository

import com.example.lessonmonitor.data.local.entity.AttendanceRecordEntity
import com.example.lessonmonitor.data.local.entity.AttendanceStatus
import kotlinx.coroutines.flow.Flow

/**
 * Per-lesson attendance tracking. Each lesson is its own session — marking
 * attendance works directly against a lesson. Students are drawn from the
 * lesson's category enrollment roster.
 */
interface AttendanceRepository {
    /** All attendance records for a lesson (one per enrolled student). */
    fun getRecordsForLesson(lessonId: Long): Flow<List<AttendanceRecordEntity>>

    /** Marking again for the same (lessonId, studentId) overwrites the prior status/reason/completed. */
    suspend fun markAttendance(
        lessonId: Long,
        studentId: Long,
        status: AttendanceStatus,
        absenceReason: String?,
        completed: Boolean = false
    )

    /** Cross-lesson attendance history for the Student Detail screen, newest first. */
    fun getHistoryForStudent(studentId: Long): Flow<List<StudentAttendanceHistoryEntry>>

    /** Statistics dashboard: present-vs-total across every lesson for one student. */
    suspend fun getStudentAttendanceStats(studentId: Long): AttendanceStats

    /** Statistics dashboard: present-vs-total for one lesson. */
    suspend fun getLessonAttendanceStats(lessonId: Long): AttendanceStats
}

data class StudentAttendanceHistoryEntry(
    val record: AttendanceRecordEntity,
    val lessonTitle: String
)

/** [presentCount] out of [totalCount] attendance records with status PRESENT. */
data class AttendanceStats(val presentCount: Int, val totalCount: Int) {
    /** 0f (not NaN) when there are no records yet, so the UI can show "0%" instead of crashing. */
    val presentRate: Float get() = if (totalCount == 0) 0f else presentCount.toFloat() / totalCount
}
