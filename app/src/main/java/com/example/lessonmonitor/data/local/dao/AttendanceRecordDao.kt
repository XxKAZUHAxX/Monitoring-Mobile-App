package com.example.lessonmonitor.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.lessonmonitor.data.local.entity.AttendanceRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceRecordDao {
    @Query("SELECT * FROM attendance_records WHERE lessonId = :lessonId")
    fun getForLesson(lessonId: Long): Flow<List<AttendanceRecordEntity>>

    /** Full cross-lesson attendance history for a student (Student Detail screen). */
    @Query("SELECT * FROM attendance_records WHERE studentId = :studentId ORDER BY recordedAt DESC")
    fun getForStudent(studentId: Long): Flow<List<AttendanceRecordEntity>>

    /** Full table, for the JSON backup snapshot. */
    @Query("SELECT * FROM attendance_records")
    fun getAll(): Flow<List<AttendanceRecordEntity>>

    /** One row per (lessonId, studentId) — marking attendance again overwrites the prior value. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(record: AttendanceRecordEntity): Long

    @Delete
    suspend fun delete(record: AttendanceRecordEntity)

    /** Impact count for the cascade-delete confirmation dialog when deleting a Lesson. */
    @Query("SELECT COUNT(*) FROM attendance_records WHERE lessonId = :lessonId")
    suspend fun countForLesson(lessonId: Long): Int

    /** Impact count for the cascade-delete confirmation dialog when deleting a Category. */
    @Query(
        """
        SELECT COUNT(*) FROM attendance_records
        WHERE lessonId IN (
            SELECT id FROM lessons WHERE categoryId = :categoryId
        )
        """
    )
    suspend fun countForCategory(categoryId: Long): Int

    /** Impact count for the cascade-delete confirmation dialog when deleting a Student. */
    @Query("SELECT COUNT(*) FROM attendance_records WHERE studentId = :studentId")
    suspend fun countForStudent(studentId: Long): Int

    /** Statistics dashboard: PRESENT count for one student, vs. [countForStudent] as the total. */
    @Query("SELECT COUNT(*) FROM attendance_records WHERE studentId = :studentId AND status = 'PRESENT'")
    suspend fun countPresentForStudent(studentId: Long): Int

    /** Statistics dashboard: PRESENT count for one lesson, vs. [countForLesson] as the total. */
    @Query("SELECT COUNT(*) FROM attendance_records WHERE lessonId = :lessonId AND status = 'PRESENT'")
    suspend fun countPresentForLesson(lessonId: Long): Int

    /** Bulk-restore for the JSON backup snapshot. Table is cleared first via `AppDatabase.clearAllTables()`. */
    @Insert
    suspend fun insertAll(records: List<AttendanceRecordEntity>)
}
