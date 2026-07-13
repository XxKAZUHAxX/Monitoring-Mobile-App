package com.example.lessonmonitor.data.datastore

import android.content.SharedPreferences
import androidx.core.content.edit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Holds the PBKDF2 password hash + salt in an [android.content.SharedPreferences]
 * instance that is backed by Android Keystore-encrypted storage (see
 * `di/DataStoreModule.kt`, which builds it via `EncryptedSharedPreferences`).
 * Never stores or exposes the plaintext password — see PLAN.md §6.
 */
@Singleton
class CredentialStore @Inject constructor(
    private val encryptedPrefs: SharedPreferences
) {
    fun hasCredential(): Boolean = encryptedPrefs.contains(KEY_HASH)

    fun saveCredential(hash: String, salt: String) {
        encryptedPrefs.edit {
            putString(KEY_HASH, hash)
            putString(KEY_SALT, salt)
        }
    }

    fun getHash(): String? = encryptedPrefs.getString(KEY_HASH, null)

    fun getSalt(): String? = encryptedPrefs.getString(KEY_SALT, null)

    private companion object {
        const val KEY_HASH = "password_hash"
        const val KEY_SALT = "password_salt"
    }
}
