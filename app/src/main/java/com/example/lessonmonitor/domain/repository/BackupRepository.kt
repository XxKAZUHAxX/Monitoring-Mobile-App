package com.example.lessonmonitor.domain.repository

import com.example.lessonmonitor.data.local.entity.AttendanceRecordEntity
import com.example.lessonmonitor.data.local.entity.CategoryEntity
import com.example.lessonmonitor.data.local.entity.EnrollmentEntity
import com.example.lessonmonitor.data.local.entity.LessonEntity
import com.example.lessonmonitor.data.local.entity.StudentEntity
import com.example.lessonmonitor.data.local.entity.UserEntity
import kotlinx.serialization.Serializable

/**
 * Whole-database JSON backup/restore (PLAN.md §7 milestone 13, §6 "Backup
 * format" decision: a JSON snapshot via kotlinx.serialization, not a raw
 * `.db` file copy). [UserEntity] is included since it only holds
 * non-sensitive state (`biometricEnabled`, `createdAt`) — the actual
 * password hash/salt live in the encrypted DataStore and are never part
 * of this snapshot.
 */
interface BackupRepository {
    suspend fun exportSnapshot(): BackupSnapshot

    /** Destructive: replaces every row in every table with the snapshot's contents. */
    suspend fun restoreSnapshot(snapshot: BackupSnapshot)
}

/** `schemaVersion` mirrors [com.example.lessonmonitor.data.local.AppDatabase]'s Room version, for future compatibility checks. */
@Serializable
data class BackupSnapshot(
    val schemaVersion: Int,
    val exportedAt: Long,
    val users: List<UserEntity>,
    val categories: List<CategoryEntity>,
    val lessons: List<LessonEntity>,
    val students: List<StudentEntity>,
    val enrollments: List<EnrollmentEntity>,
    val attendanceRecords: List<AttendanceRecordEntity>
)
