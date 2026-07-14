package com.example.lessonmonitor.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.lessonmonitor.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY name COLLATE NOCASE")
    fun getAll(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :categoryId")
    fun getById(categoryId: Long): Flow<CategoryEntity?>

    @Query("SELECT * FROM categories WHERE name LIKE '%' || :query || '%' COLLATE NOCASE")
    fun search(query: String): Flow<List<CategoryEntity>>

    @Insert
    suspend fun insert(category: CategoryEntity): Long

    /** Bulk-restore for the JSON backup snapshot (PLAN.md §7 milestone 13). Table is cleared first via `AppDatabase.clearAllTables()`. */
    @Insert
    suspend fun insertAll(categories: List<CategoryEntity>)

    @Update
    suspend fun update(category: CategoryEntity)

    @Delete
    suspend fun delete(category: CategoryEntity)

    /** Impact count for the cascade-delete confirmation dialog (PLAN.md §1 assumption #3). */
    @Query("SELECT COUNT(*) FROM lessons WHERE categoryId = :categoryId")
    suspend fun countLessons(categoryId: Long): Int
}
