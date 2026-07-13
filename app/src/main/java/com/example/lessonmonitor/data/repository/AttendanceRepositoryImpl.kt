package com.example.lessonmonitor.data.repository

import com.example.lessonmonitor.data.local.dao.AttendanceRecordDao
import com.example.lessonmonitor.data.local.dao.AttendanceSessionDao
import com.example.lessonmonitor.data.local.entity.AttendanceRecordEntity
import com.example.lessonmonitor.data.local.entity.AttendanceSessionEntity
import com.example.lessonmonitor.data.local.entity.AttendanceStatus
import com.example.lessonmonitor.domain.repository.AttendanceRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AttendanceRepositoryImpl @Inject constructor(
    private val attendanceSessionDao: AttendanceSessionDao,
    private val attendanceRecordDao: AttendanceRecordDao
) : AttendanceRepository {

    override fun getSessionsForLesson(lessonId: Long): Flow<List<AttendanceSessionEntity>> =
        attendanceSessionDao.getForLesson(lessonId)

    override fun getSession(sessionId: Long): Flow<AttendanceSessionEntity?> =
        attendanceSessionDao.getById(sessionId)

    override suspend fun createSession(lessonId: Long, sessionDate: Long): Long {
        val inserted = attendanceSessionDao.insert(
            AttendanceSessionEntity(lessonId = lessonId, sessionDate = sessionDate, createdAt = System.currentTimeMillis())
        )
        if (inserted != -1L) return inserted
        // Conflict on the unique (lessonId, sessionDate) index means a session already exists for that date.
        return attendanceSessionDao.getByLessonAndDate(lessonId, sessionDate)?.id
            ?: error("Session insert conflicted but no existing row was found for lessonId=$lessonId, sessionDate=$sessionDate")
    }

    override fun getRecordsForSession(sessionId: Long): Flow<List<AttendanceRecordEntity>> =
        attendanceRecordDao.getForSession(sessionId)

    override suspend fun markAttendance(sessionId: Long, studentId: Long, status: AttendanceStatus, absenceReason: String?) {
        attendanceRecordDao.upsert(
            AttendanceRecordEntity(
                sessionId = sessionId,
                studentId = studentId,
                status = status,
                absenceReason = absenceReason,
                recordedAt = System.currentTimeMillis()
            )
        )
    }
}
