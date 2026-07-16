package com.example.lessonmonitor.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * A lesson is a single occurrence on a specific date. Recurring lessons have
 * been removed — each lesson stands alone. [facilitatorName] and [place] are
 * stored directly here (no more per-session overrides).
 */
@Serializable
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
    /** Epoch day (days since 1970-01-01) of the lesson date. */
    val startDate: Long,
    /** Minutes since midnight; used for notification scheduling. */
    val startTime: Int? = null,
    /** Drag-to-reorder position within a category. New lessons get max(existing)+1. */
    val sortOrder: Int = 0,
    val createdAt: Long,
    val updatedAt: Long
)
