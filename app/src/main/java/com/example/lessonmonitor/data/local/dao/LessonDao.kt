package com.example.lessonmonitor.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.lessonmonitor.data.local.entity.LessonEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LessonDao {
    @Query("SELECT * FROM lessons WHERE categoryId = :categoryId ORDER BY startDate")
    fun getAllByCategory(categoryId: Long): Flow<List<LessonEntity>>

    @Query("SELECT * FROM lessons ORDER BY startDate")
    fun getAll(): Flow<List<LessonEntity>>

    @Query("SELECT * FROM lessons WHERE id = :lessonId")
    fun getById(lessonId: Long): Flow<LessonEntity?>

    /** Used by the rolling-window session generator (PLAN.md §3, roadblock #2). */
    @Query("SELECT * FROM lessons WHERE isRecurring = 1")
    suspend fun getAllRecurring(): List<LessonEntity>

    @Query("SELECT * FROM lessons WHERE title LIKE '%' || :query || '%' COLLATE NOCASE")
    fun search(query: String): Flow<List<LessonEntity>>

    @Insert
    suspend fun insert(lesson: LessonEntity): Long

    @Update
    suspend fun update(lesson: LessonEntity)

    @Delete
    suspend fun delete(lesson: LessonEntity)

    /** Impact count for the cascade-delete confirmation dialog when deleting a Category. */
    @Query("SELECT COUNT(*) FROM lessons WHERE categoryId = :categoryId")
    suspend fun countByCategory(categoryId: Long): Int
}
