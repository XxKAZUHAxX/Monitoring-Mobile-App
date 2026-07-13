package com.example.lessonmonitor.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val description: String? = null,
    val color: Int? = null,
    val icon: String? = null,
    val createdAt: Long,
    val updatedAt: Long
)
