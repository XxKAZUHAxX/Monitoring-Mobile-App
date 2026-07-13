package com.example.lessonmonitor.data.repository

import com.example.lessonmonitor.data.local.dao.StudentDao
import com.example.lessonmonitor.data.local.entity.StudentEntity
import com.example.lessonmonitor.domain.repository.StudentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StudentRepositoryImpl @Inject constructor(
    private val studentDao: StudentDao
) : StudentRepository {

    override fun getAll(): Flow<List<StudentEntity>> = studentDao.getAll()

    override fun search(query: String): Flow<List<StudentEntity>> = studentDao.search(query)

    override fun getById(studentId: Long): Flow<StudentEntity?> = studentDao.getById(studentId)

    override suspend fun create(name: String): Long {
        val now = System.currentTimeMillis()
        return studentDao.insert(StudentEntity(name = name, createdAt = now, updatedAt = now))
    }
}
