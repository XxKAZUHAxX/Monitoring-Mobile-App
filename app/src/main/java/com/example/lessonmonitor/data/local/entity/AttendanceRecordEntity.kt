package com.example.lessonmonitor.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * One row per student per lesson. References [studentId] directly (not
 * enrollment) so a student's attendance history survives even if they're
 * later removed from the category roster.
 */
@Serializable
@Entity(
    tableName = "attendance_records",
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
data class AttendanceRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val lessonId: Long,
    val studentId: Long,
    val status: AttendanceStatus,
    /** Free-text reason; relevant when [status] is ABSENT or EXCUSED. */
    val absenceReason: String? = null,
    /** Whether the student completed the lesson. Only settable when status = PRESENT. */
    val completed: Boolean = false,
    val recordedAt: Long
)
