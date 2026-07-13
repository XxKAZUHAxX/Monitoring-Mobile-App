package com.example.lessonmonitor.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Lesson <-> Student join table. Removing a student from a roster should set
 * [active] = false rather than deleting the row outright in most UI flows, so
 * re-enrolling doesn't create a duplicate; the unique index below enforces at
 * most one row per (lessonId, studentId) pair. Attendance history does NOT
 * depend on this row — [AttendanceRecordEntity] references `studentId`
 * directly, so deactivating/removing an enrollment never deletes history
 * (see PLAN.md §3, roadblock #5).
 */
@Entity(
    tableName = "enrollments",
    foreignKeys = [
        ForeignKey(
            entity = LessonEntity::class,
            parentColumns = ["id"],
            childColumns = ["lessonId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = StudentEntity::class,
            parentColumns = ["id"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("lessonId"),
        Index("studentId"),
        Index(value = ["lessonId", "studentId"], unique = true)
    ]
)
data class EnrollmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val lessonId: Long,
    val studentId: Long,
    val enrolledAt: Long,
    val active: Boolean = true
)
