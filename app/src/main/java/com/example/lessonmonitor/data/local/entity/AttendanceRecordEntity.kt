package com.example.lessonmonitor.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One row per enrolled student per session. References `studentId` directly
 * (not `enrollmentId`) so a student's attendance history survives even if
 * they're later removed from the lesson's roster — see PLAN.md §3, roadblock #5.
 */
@Entity(
    tableName = "attendance_records",
    foreignKeys = [
        ForeignKey(
            entity = AttendanceSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
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
        Index("sessionId"),
        Index("studentId"),
        Index(value = ["sessionId", "studentId"], unique = true)
    ]
)
data class AttendanceRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val sessionId: Long,
    val studentId: Long,
    val status: AttendanceStatus,
    /** Free-text reason; relevant when [status] is ABSENT or EXCUSED. */
    val absenceReason: String? = null,
    val recordedAt: Long
)
