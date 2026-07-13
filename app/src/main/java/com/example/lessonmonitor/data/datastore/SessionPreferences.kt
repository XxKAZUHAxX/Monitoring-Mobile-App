package com.example.lessonmonitor.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Plain (unencrypted) DataStore holding only the transient `isLoggedIn`
 * session flag — deliberately separate from [CredentialStore], which holds
 * the actual sensitive hash/salt in encrypted storage (PLAN.md §6). Logging
 * out clears only this flag; the credential itself is untouched.
 */
@Singleton
class SessionPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private object Keys {
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
    }

    val isLoggedIn: Flow<Boolean> = dataStore.data.map { prefs -> prefs[Keys.IS_LOGGED_IN] ?: false }

    suspend fun setLoggedIn(loggedIn: Boolean) {
        dataStore.edit { prefs -> prefs[Keys.IS_LOGGED_IN] = loggedIn }
    }
}
