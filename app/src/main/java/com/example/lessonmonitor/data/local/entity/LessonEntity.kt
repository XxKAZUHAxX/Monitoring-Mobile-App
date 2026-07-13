package com.example.lessonmonitor.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * `facilitatorName`/`place` are the *default* values for this lesson;
 * individual [AttendanceSessionEntity] rows may override either for a single
 * occurrence (e.g. a substitute facilitator on one date) — see PLAN.md §1
 * assumption #2.
 */
@Entity(
    tableName = "lessons",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("categoryId")]
)
data class LessonEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val categoryId: Long,
    val title: String,
    val description: String? = null,
    val facilitatorName: String? = null,
    val place: String? = null,
    val isRecurring: Boolean = false,
    val recurrenceType: RecurrenceType = RecurrenceType.NONE,
    /**
     * CSV of ISO-8601 day-of-week ints (1=Monday..7=Sunday); used when
     * [recurrenceType] is [RecurrenceType.WEEKLY] or [RecurrenceType.CUSTOM_DAYS].
     */
    val recurrenceDaysOfWeek: String? = null,
    /** Epoch day (days since 1970-01-01) of the first occurrence / one-off date. */
    val startDate: Long,
    /** Epoch day; null means the recurrence has no defined end. */
    val endDate: Long? = null,
    /** Minutes since midnight; used for notifications & calendar display. */
    val startTime: Int? = null,
    val endTime: Int? = null,
    val createdAt: Long,
    val updatedAt: Long
)
