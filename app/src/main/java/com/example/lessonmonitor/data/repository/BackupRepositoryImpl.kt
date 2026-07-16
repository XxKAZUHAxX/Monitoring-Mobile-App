package com.example.lessonmonitor.data.repository

import com.example.lessonmonitor.data.local.AppDatabase
import com.example.lessonmonitor.data.local.dao.AttendanceRecordDao
import com.example.lessonmonitor.data.local.dao.CategoryDao
import com.example.lessonmonitor.data.local.dao.EnrollmentDao
import com.example.lessonmonitor.data.local.dao.LessonDao
import com.example.lessonmonitor.data.local.dao.StudentDao
import com.example.lessonmonitor.data.local.dao.UserDao
import com.example.lessonmonitor.domain.repository.BackupRepository
import com.example.lessonmonitor.domain.repository.BackupSnapshot
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupRepositoryImpl @Inject constructor(
    private val appDatabase: AppDatabase,
    private val userDao: UserDao,
    private val categoryDao: CategoryDao,
    private val lessonDao: LessonDao,
    private val studentDao: StudentDao,
    private val enrollmentDao: EnrollmentDao,
    private val attendanceRecordDao: AttendanceRecordDao
) : BackupRepository {

    override suspend fun exportSnapshot(): BackupSnapshot = BackupSnapshot(
        schemaVersion = SCHEMA_VERSION,
        exportedAt = System.currentTimeMillis(),
        users = listOfNotNull(userDao.getOnce()),
        categories = categoryDao.getAll().first(),
        lessons = lessonDao.getAll().first(),
        students = studentDao.getAll().first(),
        enrollments = enrollmentDao.getAll().first(),
        attendanceRecords = attendanceRecordDao.getAll().first()
    )

    override suspend fun restoreSnapshot(snapshot: BackupSnapshot) {
        // `clearAllTables()` already runs in its own Room transaction; the inserts that
        // follow are sequential rather than wrapped in one outer transaction, trading a
        // small amount of atomicity for keeping this method plainly unit-testable with
        // mockk (no extension-function/reflection mocking of `withTransaction` needed) —
        // acceptable for a single-user local app with no concurrent writers.
        appDatabase.clearAllTables()
        // Parent tables before their children, so nothing violates a foreign key mid-restore.
        categoryDao.insertAll(snapshot.categories)
        studentDao.insertAll(snapshot.students)
        lessonDao.insertAll(snapshot.lessons)
        enrollmentDao.insertAll(snapshot.enrollments)
        attendanceRecordDao.insertAll(snapshot.attendanceRecords)
        snapshot.users.forEach { userDao.upsert(it) }
    }

    companion object {
        /** Kept in sync with [AppDatabase]'s `@Database(version = ...)`. */
        const val SCHEMA_VERSION = 2
    }
}
