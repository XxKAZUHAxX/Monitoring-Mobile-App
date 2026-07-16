package com.example.lessonmonitor.data.repository

import com.example.lessonmonitor.data.local.AppDatabase
import com.example.lessonmonitor.data.local.dao.AttendanceRecordDao
import com.example.lessonmonitor.data.local.dao.CategoryDao
import com.example.lessonmonitor.data.local.dao.EnrollmentDao
import com.example.lessonmonitor.data.local.dao.LessonDao
import com.example.lessonmonitor.data.local.dao.StudentDao
import com.example.lessonmonitor.data.local.dao.UserDao
import com.example.lessonmonitor.data.local.entity.CategoryEntity
import com.example.lessonmonitor.domain.repository.BackupSnapshot
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class BackupRepositoryImplTest {

    private val appDatabase: AppDatabase = mockk()
    private val userDao: UserDao = mockk()
    private val categoryDao: CategoryDao = mockk()
    private val lessonDao: LessonDao = mockk()
    private val studentDao: StudentDao = mockk()
    private val enrollmentDao: EnrollmentDao = mockk()
    private val attendanceRecordDao: AttendanceRecordDao = mockk()

    private lateinit var repository: BackupRepositoryImpl

    @Before
    fun setUp() {
        repository = BackupRepositoryImpl(
            appDatabase, userDao, categoryDao, lessonDao,
            studentDao, enrollmentDao, attendanceRecordDao
        )
    }

    @Test
    fun `exportSnapshot gathers all tables into a snapshot`() = runTest {
        coEvery { userDao.getOnce() } returns null
        every { categoryDao.getAll() } returns flowOf(emptyList())
        every { lessonDao.getAll() } returns flowOf(emptyList())
        every { studentDao.getAll() } returns flowOf(emptyList())
        every { enrollmentDao.getAll() } returns flowOf(emptyList())
        every { attendanceRecordDao.getAll() } returns flowOf(emptyList())

        val snapshot = repository.exportSnapshot()

        assertEquals(2, snapshot.schemaVersion)
    }

    @Test
    fun `restoreSnapshot clears tables and inserts parents before children`() = runTest {
        coEvery { appDatabase.clearAllTables() } just Runs
        coEvery { categoryDao.insertAll(any()) } just Runs
        coEvery { studentDao.insertAll(any()) } just Runs
        coEvery { lessonDao.insertAll(any()) } just Runs
        coEvery { enrollmentDao.insertAll(any()) } just Runs
        coEvery { attendanceRecordDao.insertAll(any()) } just Runs
        coEvery { userDao.upsert(any()) } just Runs

        val snapshot = BackupSnapshot(
            schemaVersion = 2, exportedAt = 1L,
            users = emptyList(), categories = emptyList(), lessons = emptyList(),
            students = emptyList(), enrollments = emptyList(), attendanceRecords = emptyList()
        )

        repository.restoreSnapshot(snapshot)

        coVerify { appDatabase.clearAllTables() }
        coVerify { categoryDao.insertAll(any()) }
        coVerify { studentDao.insertAll(any()) }
        coVerify { lessonDao.insertAll(any()) }
        coVerify { enrollmentDao.insertAll(any()) }
        coVerify { attendanceRecordDao.insertAll(any()) }
    }
}
