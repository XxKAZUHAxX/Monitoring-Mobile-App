package com.example.lessonmonitor.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.lessonmonitor.data.local.converter.Converters
import com.example.lessonmonitor.data.local.dao.AttendanceRecordDao
import com.example.lessonmonitor.data.local.dao.AttendanceSessionDao
import com.example.lessonmonitor.data.local.dao.CategoryDao
import com.example.lessonmonitor.data.local.dao.EnrollmentDao
import com.example.lessonmonitor.data.local.dao.LessonDao
import com.example.lessonmonitor.data.local.dao.StudentDao
import com.example.lessonmonitor.data.local.dao.UserDao
import com.example.lessonmonitor.data.local.entity.AttendanceRecordEntity
import com.example.lessonmonitor.data.local.entity.AttendanceSessionEntity
import com.example.lessonmonitor.data.local.entity.CategoryEntity
import com.example.lessonmonitor.data.local.entity.EnrollmentEntity
import com.example.lessonmonitor.data.local.entity.LessonEntity
import com.example.lessonmonitor.data.local.entity.StudentEntity
import com.example.lessonmonitor.data.local.entity.UserEntity

/**
 * Room schema per PLAN.md §2. Version 1 — the very first schema; bump this
 * and add a `Migration` (kept in this file/companion object) whenever a
 * field/table changes after Phase 1 ships, rather than relying on a
 * destructive fallback that would wipe user data.
 */
@Database(
    entities = [
        UserEntity::class,
        CategoryEntity::class,
        LessonEntity::class,
        StudentEntity::class,
        EnrollmentEntity::class,
        AttendanceSessionEntity::class,
        AttendanceRecordEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun lessonDao(): LessonDao
    abstract fun studentDao(): StudentDao
    abstract fun enrollmentDao(): EnrollmentDao
    abstract fun attendanceSessionDao(): AttendanceSessionDao
    abstract fun attendanceRecordDao(): AttendanceRecordDao

    companion object {
        const val DATABASE_NAME = "lesson_monitor.db"
    }
}
