package com.example.lessonmonitor.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * One row per lesson *occurrence* (a single date). Recurring lessons generate
 * one of these per date inside a rolling lookahead window (PLAN.md §3,
 * roadblock #2) rather than up front, so history is never overwritten.
 * The unique index makes generation idempotent — re-running the generator
 * for a date that already has a session is a no-op.
 */
@Serializable
@Entity(
    tableName = "attendance_sessions",
    foreignKeys = [
        ForeignKey(
            entity = LessonEntity::class,
            parentColumns = ["id"],
            childColumns = ["lessonId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("lessonId"),
        Index(value = ["lessonId", "sessionDate"], unique = true)
    ]
)
data class AttendanceSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val lessonId: Long,
    /** Epoch day (days since 1970-01-01) this occurrence falls on. */
    val sessionDate: Long,
    val facilitatorOverride: String? = null,
    val placeOverride: String? = null,
    val notes: String? = null,
    val createdAt: Long
)
