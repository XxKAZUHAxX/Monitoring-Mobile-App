package com.example.lessonmonitor.domain.repository

import com.example.lessonmonitor.data.local.entity.AttendanceRecordEntity
import com.example.lessonmonitor.data.local.entity.AttendanceSessionEntity
import com.example.lessonmonitor.data.local.entity.AttendanceStatus
import kotlinx.coroutines.flow.Flow

/**
 * Per-occurrence attendance tracking (PLAN.md §7 milestone 7). Sessions here
 * are created on demand (manually, from the Lesson Detail "Sessions" tab);
 * automatically generating them from a recurring lesson's rule into a
 * rolling 60-day window (PLAN.md §1 assumption #4) is deferred to the
 * Recurring/Scheduled Lessons milestone (#9) — this repository only needs
 * [AttendanceSessionDao.insert]'s existing idempotent (lessonId, sessionDate)
 * uniqueness to make that later generator a drop-in caller of [createSession].
 */
interface AttendanceRepository {
    fun getSessionsForLesson(lessonId: Long): Flow<List<AttendanceSessionEntity>>

    fun getSession(sessionId: Long): Flow<AttendanceSessionEntity?>

    /** Idempotent: returns the existing session's id if one already exists for that (lessonId, sessionDate). */
    suspend fun createSession(lessonId: Long, sessionDate: Long): Long

    fun getRecordsForSession(sessionId: Long): Flow<List<AttendanceRecordEntity>>

    /** Marking again for the same (sessionId, studentId) overwrites the prior status/reason. */
    suspend fun markAttendance(sessionId: Long, studentId: Long, status: AttendanceStatus, absenceReason: String?)

    /** Cross-lesson attendance history for the Student Detail screen (PLAN.md §4 screen 11), newest first. */
    fun getHistoryForStudent(studentId: Long): Flow<List<StudentAttendanceHistoryEntry>>
}

data class StudentAttendanceHistoryEntry(
    val record: AttendanceRecordEntity,
    val session: AttendanceSessionEntity,
    val lessonTitle: String
)
