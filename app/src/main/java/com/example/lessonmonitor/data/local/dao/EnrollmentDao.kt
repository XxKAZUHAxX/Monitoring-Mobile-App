package com.example.lessonmonitor.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.lessonmonitor.data.local.entity.EnrollmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EnrollmentDao {
    @Query("SELECT * FROM enrollments WHERE lessonId = :lessonId AND active = 1")
    fun getActiveForLesson(lessonId: Long): Flow<List<EnrollmentEntity>>

    /** Full table, for the JSON backup snapshot (PLAN.md §7 milestone 13). */
    @Query("SELECT * FROM enrollments")
    fun getAll(): Flow<List<EnrollmentEntity>>

    @Query("SELECT * FROM enrollments WHERE studentId = :studentId")
    fun getForStudent(studentId: Long): Flow<List<EnrollmentEntity>>

    @Query("SELECT * FROM enrollments WHERE lessonId = :lessonId AND studentId = :studentId LIMIT 1")
    suspend fun getByLessonAndStudent(lessonId: Long, studentId: Long): EnrollmentEntity?

    /** Re-enrolling reuses the existing row (unique index on lessonId+studentId) rather than duplicating. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(enrollment: EnrollmentEntity): Long

    /** Bulk-restore for the JSON backup snapshot (PLAN.md §7 milestone 13). Table is cleared first via `AppDatabase.clearAllTables()`. */
    @Insert
    suspend fun insertAll(enrollments: List<EnrollmentEntity>)

    @Update
    suspend fun update(enrollment: EnrollmentEntity)

    @Delete
    suspend fun delete(enrollment: EnrollmentEntity)

    /** Impact count for the cascade-delete confirmation dialog when deleting a Lesson. */
    @Query("SELECT COUNT(*) FROM enrollments WHERE lessonId = :lessonId")
    suspend fun countForLesson(lessonId: Long): Int

    /** Impact count for the cascade-delete confirmation dialog when deleting a Student. */
    @Query("SELECT COUNT(*) FROM enrollments WHERE studentId = :studentId")
    suspend fun countForStudent(studentId: Long): Int
}
