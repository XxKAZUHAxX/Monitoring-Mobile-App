package com.example.lessonmonitor.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.lessonmonitor.data.local.entity.AttendanceSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceSessionDao {
    @Query("SELECT * FROM attendance_sessions WHERE lessonId = :lessonId ORDER BY sessionDate DESC")
    fun getForLesson(lessonId: Long): Flow<List<AttendanceSessionEntity>>

    @Query("SELECT * FROM attendance_sessions WHERE id = :sessionId")
    fun getById(sessionId: Long): Flow<AttendanceSessionEntity?>

    /** Backs the Student Detail cross-lesson attendance history (joined in-memory with lessons/records). */
    @Query("SELECT * FROM attendance_sessions")
    fun getAll(): Flow<List<AttendanceSessionEntity>>

    @Query("SELECT * FROM attendance_sessions WHERE lessonId = :lessonId AND sessionDate = :sessionDate LIMIT 1")
    suspend fun getByLessonAndDate(lessonId: Long, sessionDate: Long): AttendanceSessionEntity?

    /** Backs the Calendar screen (PLAN.md §4). */
    @Query("SELECT * FROM attendance_sessions WHERE sessionDate BETWEEN :startEpochDay AND :endEpochDay ORDER BY sessionDate")
    fun getInDateRange(startEpochDay: Long, endEpochDay: Long): Flow<List<AttendanceSessionEntity>>

    /** Ignore duplicates so the rolling-window generator (PLAN.md §3, roadblock #2) is idempotent. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(session: AttendanceSessionEntity): Long

    @Update
    suspend fun update(session: AttendanceSessionEntity)

    @Delete
    suspend fun delete(session: AttendanceSessionEntity)

    /** Impact count for the cascade-delete confirmation dialog when deleting a Lesson. */
    @Query("SELECT COUNT(*) FROM attendance_sessions WHERE lessonId = :lessonId")
    suspend fun countForLesson(lessonId: Long): Int

    /** Impact count for the cascade-delete confirmation dialog when deleting a Category. */
    @Query("SELECT COUNT(*) FROM attendance_sessions WHERE lessonId IN (SELECT id FROM lessons WHERE categoryId = :categoryId)")
    suspend fun countForCategory(categoryId: Long): Int
}
