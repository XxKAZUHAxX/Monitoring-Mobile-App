package com.example.lessonmonitor.data.repository

import com.example.lessonmonitor.data.local.dao.AttendanceRecordDao
import com.example.lessonmonitor.data.local.dao.AttendanceSessionDao
import com.example.lessonmonitor.data.local.dao.LessonDao
import com.example.lessonmonitor.data.local.dao.StudentDao
import com.example.lessonmonitor.data.local.entity.AttendanceRecordEntity
import com.example.lessonmonitor.data.local.entity.AttendanceSessionEntity
import com.example.lessonmonitor.data.local.entity.AttendanceStatus
import com.example.lessonmonitor.domain.repository.AttendanceRepository
import com.example.lessonmonitor.domain.repository.AttendanceStats
import com.example.lessonmonitor.domain.repository.CalendarSessionEntry
import com.example.lessonmonitor.domain.repository.LessonExportRow
import com.example.lessonmonitor.domain.repository.StudentAttendanceHistoryEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AttendanceRepositoryImpl @Inject constructor(
    private val attendanceSessionDao: AttendanceSessionDao,
    private val attendanceRecordDao: AttendanceRecordDao,
    private val lessonDao: LessonDao,
    private val studentDao: StudentDao
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

    override fun getHistoryForStudent(studentId: Long): Flow<List<StudentAttendanceHistoryEntry>> =
        combine(
            attendanceRecordDao.getForStudent(studentId),
            attendanceSessionDao.getAll(),
            lessonDao.getAll()
        ) { records, sessions, lessons ->
            val sessionsById = sessions.associateBy { it.id }
            val lessonTitleById = lessons.associateBy({ it.id }, { it.title })
            records
                .mapNotNull { record ->
                    val session = sessionsById[record.sessionId] ?: return@mapNotNull null
                    val lessonTitle = lessonTitleById[session.lessonId] ?: "Unknown lesson"
                    StudentAttendanceHistoryEntry(record, session, lessonTitle)
                }
                .sortedByDescending { it.session.sessionDate }
        }

    override fun getSessionsInRange(startEpochDay: Long, endEpochDay: Long): Flow<List<CalendarSessionEntry>> =
        combine(
            attendanceSessionDao.getInDateRange(startEpochDay, endEpochDay),
            lessonDao.getAll()
        ) { sessions, lessons ->
            val lessonTitleById = lessons.associateBy({ it.id }, { it.title })
            sessions.map { session ->
                CalendarSessionEntry(session, lessonTitleById[session.lessonId] ?: "Unknown lesson")
            }
        }

    override suspend fun getStudentAttendanceStats(studentId: Long): AttendanceStats = AttendanceStats(
        presentCount = attendanceRecordDao.countPresentForStudent(studentId),
        totalCount = attendanceRecordDao.countForStudent(studentId)
    )

    override suspend fun getLessonAttendanceStats(lessonId: Long): AttendanceStats = AttendanceStats(
        presentCount = attendanceRecordDao.countPresentForLesson(lessonId),
        totalCount = attendanceRecordDao.countForLesson(lessonId)
    )

    override suspend fun getExportRowsForLesson(lessonId: Long): List<LessonExportRow> {
        val sessions = attendanceSessionDao.getForLesson(lessonId).first()
        val sessionsById = sessions.associateBy { it.id }
        val students = studentDao.getAll().first()
        val studentNameById = students.associateBy({ it.id }, { it.name })
        val records = attendanceRecordDao.getAllForLesson(lessonId)
        return records
            .mapNotNull { record ->
                val session = sessionsById[record.sessionId] ?: return@mapNotNull null
                LessonExportRow(
                    sessionDate = session.sessionDate,
                    studentName = studentNameById[record.studentId] ?: "Unknown student",
                    status = record.status,
                    absenceReason = record.absenceReason
                )
            }
            .sortedWith(compareBy({ it.sessionDate }, { it.studentName }))
    }
}
