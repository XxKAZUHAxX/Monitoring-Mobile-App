package com.example.lessonmonitor.data.repository

import com.example.lessonmonitor.data.local.AppDatabase
import com.example.lessonmonitor.data.local.dao.AttendanceRecordDao
import com.example.lessonmonitor.data.local.dao.AttendanceSessionDao
import com.example.lessonmonitor.data.local.dao.CategoryDao
import com.example.lessonmonitor.data.local.dao.EnrollmentDao
import com.example.lessonmonitor.data.local.dao.LessonDao
import com.example.lessonmonitor.data.local.dao.StudentDao
import com.example.lessonmonitor.data.local.dao.UserDao
import com.example.lessonmonitor.data.local.entity.CategoryEntity
import com.example.lessonmonitor.data.local.entity.UserEntity
import com.example.lessonmonitor.domain.repository.BackupSnapshot
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
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
    private val attendanceSessionDao: AttendanceSessionDao = mockk()
    private val attendanceRecordDao: AttendanceRecordDao = mockk()

    private lateinit var repository: BackupRepositoryImpl

    @Before
    fun setUp() {
        repository = BackupRepositoryImpl(
            appDatabase, userDao, categoryDao, lessonDao, studentDao, enrollmentDao, attendanceSessionDao, attendanceRecordDao
        )
    }

    @Test
    fun `exportSnapshot gathers every table into one snapshot`() = runTest {
        val category = CategoryEntity(id = 1L, name = "Math", createdAt = 1L, updatedAt = 1L)
        val user = UserEntity(biometricEnabled = true, createdAt = 1L)
        coEvery { userDao.getOnce() } returns user
        every { categoryDao.getAll() } returns flowOf(listOf(category))
        every { lessonDao.getAll() } returns flowOf(emptyList())
        every { studentDao.getAll() } returns flowOf(emptyList())
        every { enrollmentDao.getAll() } returns flowOf(emptyList())
        every { attendanceSessionDao.getAll() } returns flowOf(emptyList())
        every { attendanceRecordDao.getAll() } returns flowOf(emptyList())

        val snapshot = repository.exportSnapshot()

        assertEquals(listOf(user), snapshot.users)
        assertEquals(listOf(category), snapshot.categories)
        assertEquals(1, snapshot.schemaVersion)
    }

    @Test
    fun `exportSnapshot omits the user list entry when no user row exists yet`() = runTest {
        coEvery { userDao.getOnce() } returns null
        every { categoryDao.getAll() } returns flowOf(emptyList())
        every { lessonDao.getAll() } returns flowOf(emptyList())
        every { studentDao.getAll() } returns flowOf(emptyList())
        every { enrollmentDao.getAll() } returns flowOf(emptyList())
        every { attendanceSessionDao.getAll() } returns flowOf(emptyList())
        every { attendanceRecordDao.getAll() } returns flowOf(emptyList())

        val snapshot = repository.exportSnapshot()

        assertEquals(emptyList<UserEntity>(), snapshot.users)
    }

    @Test
    fun `restoreSnapshot clears every table then inserts parent tables before their children`() = runTest {
        every { appDatabase.clearAllTables() } returns Unit
        coEvery { categoryDao.insertAll(any()) } returns Unit
        coEvery { studentDao.insertAll(any()) } returns Unit
        coEvery { lessonDao.insertAll(any()) } returns Unit
        coEvery { enrollmentDao.insertAll(any()) } returns Unit
        coEvery { attendanceSessionDao.insertAll(any()) } returns Unit
        coEvery { attendanceRecordDao.insertAll(any()) } returns Unit
        coEvery { userDao.upsert(any()) } returns Unit
        val snapshot = BackupSnapshot(
            schemaVersion = 1,
            exportedAt = 1L,
            users = listOf(UserEntity(biometricEnabled = false, createdAt = 1L)),
            categories = emptyList(),
            lessons = emptyList(),
            students = emptyList(),
            enrollments = emptyList(),
            attendanceSessions = emptyList(),
            attendanceRecords = emptyList()
        )

        repository.restoreSnapshot(snapshot)

        coVerifyOrder {
            appDatabase.clearAllTables()
            categoryDao.insertAll(any())
            studentDao.insertAll(any())
            lessonDao.insertAll(any())
            enrollmentDao.insertAll(any())
            attendanceSessionDao.insertAll(any())
            attendanceRecordDao.insertAll(any())
        }
        coVerify { userDao.upsert(snapshot.users[0]) }
    }
}
