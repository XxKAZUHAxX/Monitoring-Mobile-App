package com.example.lessonmonitor.data.repository

import com.example.lessonmonitor.data.local.dao.EnrollmentDao
import com.example.lessonmonitor.data.local.dao.StudentDao
import com.example.lessonmonitor.data.local.entity.EnrollmentEntity
import com.example.lessonmonitor.domain.repository.EnrollmentRepository
import com.example.lessonmonitor.domain.repository.RosterEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EnrollmentRepositoryImpl @Inject constructor(
    private val enrollmentDao: EnrollmentDao,
    private val studentDao: StudentDao
) : EnrollmentRepository {

    override fun getRosterForLesson(lessonId: Long): Flow<List<RosterEntry>> =
        combine(enrollmentDao.getActiveForLesson(lessonId), studentDao.getAll()) { enrollments, students ->
            enrollments
                .mapNotNull { enrollment ->
                    students.find { it.id == enrollment.studentId }?.let { student ->
                        RosterEntry(enrollment, student)
                    }
                }
                .sortedBy { it.student.name.lowercase() }
        }

    override suspend fun enroll(lessonId: Long, studentId: Long) {
        val now = System.currentTimeMillis()
        enrollmentDao.upsert(
            EnrollmentEntity(lessonId = lessonId, studentId = studentId, enrolledAt = now, active = true)
        )
    }

    override suspend fun unenroll(enrollment: EnrollmentEntity) {
        enrollmentDao.update(enrollment.copy(active = false))
    }
}
