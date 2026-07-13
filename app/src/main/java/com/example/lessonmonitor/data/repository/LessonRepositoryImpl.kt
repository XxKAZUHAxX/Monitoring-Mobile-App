package com.example.lessonmonitor.data.repository

import com.example.lessonmonitor.data.local.dao.AttendanceRecordDao
import com.example.lessonmonitor.data.local.dao.AttendanceSessionDao
import com.example.lessonmonitor.data.local.dao.EnrollmentDao
import com.example.lessonmonitor.data.local.dao.LessonDao
import com.example.lessonmonitor.data.local.entity.LessonEntity
import com.example.lessonmonitor.data.local.entity.RecurrenceType
import com.example.lessonmonitor.domain.repository.LessonDeleteImpact
import com.example.lessonmonitor.domain.repository.LessonRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LessonRepositoryImpl @Inject constructor(
    private val lessonDao: LessonDao,
    private val enrollmentDao: EnrollmentDao,
    private val attendanceSessionDao: AttendanceSessionDao,
    private val attendanceRecordDao: AttendanceRecordDao
) : LessonRepository {

    override fun getAllByCategory(categoryId: Long): Flow<List<LessonEntity>> =
        lessonDao.getAllByCategory(categoryId)

    override fun getById(lessonId: Long): Flow<LessonEntity?> = lessonDao.getById(lessonId)

    override suspend fun create(
        categoryId: Long,
        title: String,
        description: String?,
        facilitatorName: String?,
        place: String?,
        isRecurring: Boolean,
        recurrenceType: RecurrenceType,
        recurrenceDaysOfWeek: String?,
        startDate: Long,
        endDate: Long?,
        startTime: Int?,
        endTime: Int?
    ): Long {
        val now = System.currentTimeMillis()
        return lessonDao.insert(
            LessonEntity(
                categoryId = categoryId,
                title = title,
                description = description,
                facilitatorName = facilitatorName,
                place = place,
                isRecurring = isRecurring,
                recurrenceType = recurrenceType,
                recurrenceDaysOfWeek = recurrenceDaysOfWeek,
                startDate = startDate,
                endDate = endDate,
                startTime = startTime,
                endTime = endTime,
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
        enrollmentCount = enrollmentDao.countForLesson(lessonId),
        sessionCount = attendanceSessionDao.countForLesson(lessonId),
        recordCount = attendanceRecordDao.countForLesson(lessonId)
    )
}
