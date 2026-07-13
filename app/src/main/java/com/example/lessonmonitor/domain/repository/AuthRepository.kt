package com.example.lessonmonitor.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Phase-1 local-only auth surface (Feature A). This interface is the
 * Phase-2 swap point per PLAN.md §3 roadblock #6 — a future remote/cloud
 * auth data source could be introduced behind [com.example.lessonmonitor.data.repository.AuthRepositoryImpl]
 * without any ViewModel/UI changes, though true multi-user cloud auth is out
 * of scope for Phase 1 (see PLAN.md "Phase 2" section).
 */
interface AuthRepository {
    /** Whether the local session is currently active; persists across app restarts until logout. */
    fun isLoggedIn(): Flow<Boolean>

    /** Whether a local PIN/password has ever been created (drives Splash routing). */
    suspend fun hasCredential(): Boolean

    /** Hashes and stores the credential, creates the singleton [com.example.lessonmonitor.data.local.entity.UserEntity] row, and logs in. */
    suspend fun createCredential(password: String)

    suspend fun verifyPassword(password: String): Boolean

    suspend fun setLoggedIn(loggedIn: Boolean)

    /** Clears only the session flag; the credential hash/salt are untouched. */
    suspend fun logout()

    fun isBiometricEnabled(): Flow<Boolean>

    suspend fun setBiometricEnabled(enabled: Boolean)
}
