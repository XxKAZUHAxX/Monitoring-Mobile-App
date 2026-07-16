package com.example.lessonmonitor.data.repository

import com.example.lessonmonitor.data.local.dao.AttendanceRecordDao
import com.example.lessonmonitor.data.local.dao.LessonDao
import com.example.lessonmonitor.data.local.entity.AttendanceRecordEntity
import com.example.lessonmonitor.data.local.entity.AttendanceStatus
import com.example.lessonmonitor.domain.repository.AttendanceRepository
import com.example.lessonmonitor.domain.repository.AttendanceStats
import com.example.lessonmonitor.domain.repository.StudentAttendanceHistoryEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AttendanceRepositoryImpl @Inject constructor(
    private val attendanceRecordDao: AttendanceRecordDao,
    private val lessonDao: LessonDao
) : AttendanceRepository {

    override fun getRecordsForLesson(lessonId: Long): Flow<List<AttendanceRecordEntity>> =
        attendanceRecordDao.getForLesson(lessonId)

    override suspend fun markAttendance(
        lessonId: Long,
        studentId: Long,
        status: AttendanceStatus,
        absenceReason: String?,
        completed: Boolean
    ) {
        // If status is changed away from PRESENT, reset completed to false.
        val effectiveCompleted = if (status != AttendanceStatus.PRESENT) false else completed
        attendanceRecordDao.upsert(
            AttendanceRecordEntity(
                lessonId = lessonId,
                studentId = studentId,
                status = status,
                absenceReason = absenceReason,
                completed = effectiveCompleted,
                recordedAt = System.currentTimeMillis()
            )
        )
    }

    override fun getHistoryForStudent(studentId: Long): Flow<List<StudentAttendanceHistoryEntry>> =
        combine(
            attendanceRecordDao.getForStudent(studentId),
            lessonDao.getAll()
        ) { records, lessons ->
            val lessonTitleById = lessons.associateBy({ it.id }, { it.title })
            records
                .map { record ->
                    val lessonTitle = lessonTitleById[record.lessonId] ?: "Unknown lesson"
                    StudentAttendanceHistoryEntry(record, lessonTitle)
                }
                .sortedByDescending { it.record.recordedAt }
        }

    override suspend fun getStudentAttendanceStats(studentId: Long): AttendanceStats = AttendanceStats(
        presentCount = attendanceRecordDao.countPresentForStudent(studentId),
        totalCount = attendanceRecordDao.countForStudent(studentId)
    )

    override suspend fun getLessonAttendanceStats(lessonId: Long): AttendanceStats = AttendanceStats(
        presentCount = attendanceRecordDao.countPresentForLesson(lessonId),
        totalCount = attendanceRecordDao.countForLesson(lessonId)
    )
}
