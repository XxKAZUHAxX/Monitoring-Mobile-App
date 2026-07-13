package com.example.lessonmonitor.data.repository

import com.example.lessonmonitor.data.local.dao.AttendanceRecordDao
import com.example.lessonmonitor.data.local.dao.EnrollmentDao
import com.example.lessonmonitor.data.local.dao.StudentDao
import com.example.lessonmonitor.data.local.entity.StudentEntity
import com.example.lessonmonitor.domain.repository.StudentDeleteImpact
import com.example.lessonmonitor.domain.repository.StudentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StudentRepositoryImpl @Inject constructor(
    private val studentDao: StudentDao,
    private val enrollmentDao: EnrollmentDao,
    private val attendanceRecordDao: AttendanceRecordDao
) : StudentRepository {

    override fun getAll(): Flow<List<StudentEntity>> = studentDao.getAll()

    override fun search(query: String): Flow<List<StudentEntity>> = studentDao.search(query)

    override fun getById(studentId: Long): Flow<StudentEntity?> = studentDao.getById(studentId)

    override suspend fun create(
        name: String,
        phone: String?,
        email: String?,
        notes: String?,
        photoPath: String?
    ): Long {
        val now = System.currentTimeMillis()
        return studentDao.insert(
            StudentEntity(
                name = name,
                phone = phone,
                email = email,
                notes = notes,
                photoPath = photoPath,
                createdAt = now,
                updatedAt = now
            )
        )
    }

    override suspend fun update(student: StudentEntity) {
        studentDao.update(student.copy(updatedAt = System.currentTimeMillis()))
    }

    override suspend fun delete(student: StudentEntity) {
        studentDao.delete(student)
    }

    override suspend fun getDeleteImpact(studentId: Long): StudentDeleteImpact = StudentDeleteImpact(
        enrollmentCount = enrollmentDao.countForStudent(studentId),
        recordCount = attendanceRecordDao.countForStudent(studentId)
    )
}

