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

    override fun getRosterForCategory(categoryId: Long): Flow<List<RosterEntry>> =
        combine(enrollmentDao.getActiveForCategory(categoryId), studentDao.getAll()) { enrollments, students ->
            enrollments
                .mapNotNull { enrollment ->
                    students.find { it.id == enrollment.studentId }?.let { student ->
                        RosterEntry(enrollment, student)
                    }
                }
                .sortedBy { it.student.name.lowercase() }
        }

    override suspend fun enroll(categoryId: Long, studentId: Long) {
        val now = System.currentTimeMillis()
        enrollmentDao.upsert(
            EnrollmentEntity(categoryId = categoryId, studentId = studentId, enrolledAt = now, active = true)
        )
    }

    override suspend fun unenroll(enrollment: EnrollmentEntity) {
        enrollmentDao.update(enrollment.copy(active = false))
    }
}
