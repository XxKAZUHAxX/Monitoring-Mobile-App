package com.example.lessonmonitor.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Single local user profile row (Phase 1 is local-only, single-profile — see
 * PLAN.md §1 assumption #1). Deliberately does NOT store the password hash or
 * salt: those are security-sensitive and live in an encrypted DataStore
 * (Keystore-backed), wired up in the "User account" milestone. This table
 * only tracks non-sensitive profile state.
 */
@Serializable
@Entity(tableName = "user")
data class UserEntity(
    @PrimaryKey val id: Long = SINGLETON_ID,
    val biometricEnabled: Boolean = false,
    val createdAt: Long
) {
    companion object {
        /** Only one row ever exists; always addressed by this fixed id. */
        const val SINGLETON_ID = 1L
    }
}
