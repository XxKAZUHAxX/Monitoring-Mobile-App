package com.example.lessonmonitor.data.repository

import com.example.lessonmonitor.data.local.dao.AttendanceRecordDao
import com.example.lessonmonitor.data.local.dao.EnrollmentDao
import com.example.lessonmonitor.data.local.dao.LessonDao
import com.example.lessonmonitor.data.local.entity.LessonEntity
import com.example.lessonmonitor.domain.repository.LessonDeleteImpact
import com.example.lessonmonitor.domain.repository.LessonRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LessonRepositoryImpl @Inject constructor(
    private val lessonDao: LessonDao,
    private val enrollmentDao: EnrollmentDao,
    private val attendanceRecordDao: AttendanceRecordDao
) : LessonRepository {

    override fun getAllByCategory(categoryId: Long): Flow<List<LessonEntity>> =
        lessonDao.getAllByCategory(categoryId)

    override fun getAll(): Flow<List<LessonEntity>> = lessonDao.getAll()

    override fun getById(lessonId: Long): Flow<LessonEntity?> = lessonDao.getById(lessonId)

    override fun search(query: String): Flow<List<LessonEntity>> = lessonDao.search(query)

    override suspend fun create(
        categoryId: Long,
        title: String,
        description: String?,
        facilitatorName: String?,
        place: String?,
        startDate: Long,
        startTime: Int?
    ): Long {
        val now = System.currentTimeMillis()
        val sortOrder = (lessonDao.getMaxSortOrder(categoryId) ?: -1) + 1
        return lessonDao.insert(
            LessonEntity(
                categoryId = categoryId,
                title = title,
                description = description,
                facilitatorName = facilitatorName,
                place = place,
                startDate = startDate,
                startTime = startTime,
                sortOrder = sortOrder,
                createdAt = now,
                updatedAt = now
            )
        )
    }

    override suspend fun update(lesson: LessonEntity) {
        lessonDao.update(lesson.copy(updatedAt = System.currentTimeMillis()))
    }

    override suspend fun delete(lesson: LessonEntity) {
        lessonDao.delete(lesson)
    }

    override suspend fun getDeleteImpact(lessonId: Long): LessonDeleteImpact = LessonDeleteImpact(
        enrollmentCount = 0, // enrollment is now category-scoped, not lesson-scoped
        recordCount = attendanceRecordDao.countForLesson(lessonId)
    )

    override suspend fun getMaxSortOrder(categoryId: Long): Int? =
        lessonDao.getMaxSortOrder(categoryId)

    override suspend fun updateSortOrder(lessonId: Long, sortOrder: Int) {
        lessonDao.updateSortOrder(lessonId, sortOrder)
    }

    override suspend fun reorderLessons(categoryId: Long, orderedIds: List<Long>) {
        orderedIds.forEachIndexed { index, lessonId ->
            lessonDao.updateSortOrder(lessonId, index)
        }
    }
}
