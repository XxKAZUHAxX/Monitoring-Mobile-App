package com.example.lessonmonitor.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/** Global, reusable across lessons/categories — see PLAN.md §2. */
@Serializable
@Entity(tableName = "students")
data class StudentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val photoPath: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val notes: String? = null,
    val createdAt: Long,
    val updatedAt: Long
)
