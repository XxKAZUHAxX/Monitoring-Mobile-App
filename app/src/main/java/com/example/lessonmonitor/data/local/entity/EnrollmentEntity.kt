package com.example.lessonmonitor.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Category <-> Student join table. Enrolling a student in a category places
 * them on the roster for *every* lesson in that category. Removing a student
 * sets [active] = false rather than deleting the row, so re-enrolling reuses
 * the existing row (unique index on categoryId+studentId). Attendance history
 * is NOT affected — [AttendanceRecordEntity] references [studentId] directly.
 */
@Serializable
@Entity(
    tableName = "enrollments",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
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
        Index("categoryId"),
        Index("studentId"),
        Index(value = ["categoryId", "studentId"], unique = true)
    ]
)
data class EnrollmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val categoryId: Long,
    val studentId: Long,
    val enrolledAt: Long,
    val active: Boolean = true
)
