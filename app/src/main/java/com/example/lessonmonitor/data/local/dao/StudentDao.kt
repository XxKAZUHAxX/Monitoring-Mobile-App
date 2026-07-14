package com.example.lessonmonitor.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.lessonmonitor.data.local.entity.StudentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {
    @Query("SELECT * FROM students ORDER BY name COLLATE NOCASE")
    fun getAll(): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students WHERE id = :studentId")
    fun getById(studentId: Long): Flow<StudentEntity?>

    @Query("SELECT * FROM students WHERE name LIKE '%' || :query || '%' COLLATE NOCASE")
    fun search(query: String): Flow<List<StudentEntity>>

    @Insert
    suspend fun insert(student: StudentEntity): Long

    /** Bulk-restore for the JSON backup snapshot (PLAN.md §7 milestone 13). Table is cleared first via `AppDatabase.clearAllTables()`. */
    @Insert
    suspend fun insertAll(students: List<StudentEntity>)

    @Update
    suspend fun update(student: StudentEntity)

    @Delete
    suspend fun delete(student: StudentEntity)
}
