package com.example.lessonmonitor.data.repository

import com.example.lessonmonitor.data.datastore.CredentialStore
import com.example.lessonmonitor.data.datastore.SessionPreferences
import com.example.lessonmonitor.data.local.dao.UserDao
import com.example.lessonmonitor.data.local.entity.UserEntity
import com.example.lessonmonitor.domain.repository.AuthRepository
import com.example.lessonmonitor.util.PasswordHasher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val credentialStore: CredentialStore,
    private val sessionPreferences: SessionPreferences,
    private val userDao: UserDao
) : AuthRepository {

    override fun isLoggedIn(): Flow<Boolean> = sessionPreferences.isLoggedIn

    override suspend fun hasCredential(): Boolean = credentialStore.hasCredential()

    override suspend fun createCredential(password: String) {
        val salt = PasswordHasher.generateSalt()
        val hash = PasswordHasher.hash(password, salt)
        credentialStore.saveCredential(hash, salt)
        userDao.upsert(UserEntity(createdAt = System.currentTimeMillis()))
        sessionPreferences.setLoggedIn(true)
    }

    override suspend fun verifyPassword(password: String): Boolean {
        val hash = credentialStore.getHash() ?: return false
        val salt = credentialStore.getSalt() ?: return false
        return PasswordHasher.verify(password, salt, hash)
    }

    override suspend fun setLoggedIn(loggedIn: Boolean) {
        sessionPreferences.setLoggedIn(loggedIn)
    }

    override suspend fun logout() {
        sessionPreferences.setLoggedIn(false)
    }

    override fun isBiometricEnabled(): Flow<Boolean> =
        userDao.get().map { it?.biometricEnabled ?: false }

    override suspend fun setBiometricEnabled(enabled: Boolean) {
        val existing = userDao.getOnce()
        val updated = existing?.copy(biometricEnabled = enabled)
            ?: UserEntity(biometricEnabled = enabled, createdAt = System.currentTimeMillis())
        userDao.upsert(updated)
    }
}
