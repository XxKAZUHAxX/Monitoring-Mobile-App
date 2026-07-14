package com.example.lessonmonitor.domain.repository

import com.example.lessonmonitor.data.local.entity.AttendanceRecordEntity
import com.example.lessonmonitor.data.local.entity.AttendanceSessionEntity
import com.example.lessonmonitor.data.local.entity.AttendanceStatus
import kotlinx.coroutines.flow.Flow

/**
 * Per-occurrence attendance tracking (PLAN.md §7 milestone 7). Sessions can
 * be created manually (from the Lesson Detail "Sessions" tab) or in bulk by
 * `domain.schedule.RecurringSessionGenerator` (milestone #9), which walks a
 * recurring lesson's rule across a rolling 60-day window (PLAN.md §1
 * assumption #4) and calls [createSession] once per occurrence date —
 * [AttendanceSessionDao.insert]'s idempotent (lessonId, sessionDate)
 * uniqueness means re-running the generator is always safe.
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

    /** Backs the Calendar/DayAgenda screens (PLAN.md §4 screens 13/14), inclusive of both bounds. */
    fun getSessionsInRange(startEpochDay: Long, endEpochDay: Long): Flow<List<CalendarSessionEntry>>
}

data class StudentAttendanceHistoryEntry(
    val record: AttendanceRecordEntity,
    val session: AttendanceSessionEntity,
    val lessonTitle: String
)

data class CalendarSessionEntry(
    val session: AttendanceSessionEntity,
    val lessonTitle: String
)
