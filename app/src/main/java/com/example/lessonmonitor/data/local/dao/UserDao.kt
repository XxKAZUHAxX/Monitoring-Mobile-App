package com.example.lessonmonitor.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.lessonmonitor.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM user WHERE id = ${UserEntity.SINGLETON_ID} LIMIT 1")
    fun get(): Flow<UserEntity?>

    /** One-shot read, e.g. for read-modify-write updates like toggling `biometricEnabled`. */
    @Query("SELECT * FROM user WHERE id = ${UserEntity.SINGLETON_ID} LIMIT 1")
    suspend fun getOnce(): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(user: UserEntity)

    @Update
    suspend fun update(user: UserEntity)
}
