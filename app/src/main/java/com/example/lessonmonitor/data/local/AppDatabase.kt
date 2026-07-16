package com.example.lessonmonitor.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.lessonmonitor.data.local.converter.Converters
import com.example.lessonmonitor.data.local.dao.AttendanceRecordDao
import com.example.lessonmonitor.data.local.dao.CategoryDao
import com.example.lessonmonitor.data.local.dao.EnrollmentDao
import com.example.lessonmonitor.data.local.dao.LessonDao
import com.example.lessonmonitor.data.local.dao.StudentDao
import com.example.lessonmonitor.data.local.dao.UserDao
import com.example.lessonmonitor.data.local.entity.AttendanceRecordEntity
import com.example.lessonmonitor.data.local.entity.CategoryEntity
import com.example.lessonmonitor.data.local.entity.EnrollmentEntity
import com.example.lessonmonitor.data.local.entity.LessonEntity
import com.example.lessonmonitor.data.local.entity.StudentEntity
import com.example.lessonmonitor.data.local.entity.UserEntity

/**
 * Room database for Lesson Monitor.
 *
 * Version 2: AttendanceSessionEntity merged into LessonEntity, enrollment
 * moved from lesson-level to category-level, recurring lesson fields removed,
 * sortOrder added to CategoryEntity and LessonEntity, completed added to
 * AttendanceRecordEntity. Destructive migration — beta app, data preservation
 * is out of scope for this rework.
 */
@Database(
    entities = [
        UserEntity::class,
        CategoryEntity::class,
        LessonEntity::class,
        StudentEntity::class,
        EnrollmentEntity::class,
        AttendanceRecordEntity::class
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun lessonDao(): LessonDao
    abstract fun studentDao(): StudentDao
    abstract fun enrollmentDao(): EnrollmentDao
    abstract fun attendanceRecordDao(): AttendanceRecordDao

    companion object {
        const val DATABASE_NAME = "lesson_monitor.db"
    }
}
