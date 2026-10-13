package com.baburamtechnologies.expensetracker.core.datastore

import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.security.SecureRandom
import javax.inject.Inject

object PreferenceKeys {
    fun wrappedCloudKey(uid: String) = stringPreferencesKey("wrapped_cloud_key_$uid")
    fun wrappedMasterKey(uid: String) = stringPreferencesKey("wrapped_master_key_$uid")
    fun autoBackupEnabled(uid: String) = booleanPreferencesKey("auto_backup_enabled_$uid")
    fun lastBackupTime(uid: String) = stringPreferencesKey("last_backup_time_$uid")
    fun userBackupSecret(uid: String) = stringPreferencesKey("user_backup_secret_$uid")

    val REMEMBERED_EMAIL = stringPreferencesKey("remembered_email")
    val IS_INITIAL_SETUP_COMPLETE = booleanPreferencesKey("is_initial_setup_complete")
}

class PreferenceManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    
    val rememberedEmail: Flow<String?> = dataStore.data.map { it[PreferenceKeys.REMEMBERED_EMAIL] }
    suspend fun setRememberedEmail(email: String?) {
        dataStore.edit { preferences ->
            if (email == null) {
                preferences.remove(PreferenceKeys.REMEMBERED_EMAIL)
            } else {
                preferences[PreferenceKeys.REMEMBERED_EMAIL] = email
            }
        }
    }

    fun wrappedCloudKey(uid: String): Flow<String?> = dataStore.data.map { it[PreferenceKeys.wrappedCloudKey(uid)] }
    fun wrappedMasterKey(uid: String): Flow<String?> = dataStore.data.map { it[PreferenceKeys.wrappedMasterKey(uid)] }
    
    suspend fun saveBackupKeys(uid: String, wrappedCloudKey: String, wrappedMasterKey: String) {
        dataStore.edit { 
            it[PreferenceKeys.wrappedCloudKey(uid)] = wrappedCloudKey 
            it[PreferenceKeys.wrappedMasterKey(uid)] = wrappedMasterKey
        }
    }

    fun autoBackupEnabled(uid: String): Flow<Boolean> = dataStore.data.map { it[PreferenceKeys.autoBackupEnabled(uid)] ?: false }
    suspend fun setAutoBackupEnabled(uid: String, enabled: Boolean) {
        dataStore.edit { it[PreferenceKeys.autoBackupEnabled(uid)] = enabled }
    }

    fun lastBackupTime(uid: String): Flow<String?> = dataStore.data.map { it[PreferenceKeys.lastBackupTime(uid)] }
    suspend fun saveLastBackupTime(uid: String, timestamp: String) {
        dataStore.edit { it[PreferenceKeys.lastBackupTime(uid)] = timestamp }
    }

    suspend fun getOrCreateUserBackupSecret(uid: String): String {
        val key = PreferenceKeys.userBackupSecret(uid)
        val currentSecret = dataStore.data.map { it[key] }.first()
        if (!currentSecret.isNullOrBlank()) {
            return currentSecret
        }
        val secretBytes = ByteArray(32)
        SecureRandom().nextBytes(secretBytes)
        val newSecret = Base64.encodeToString(secretBytes, Base64.NO_WRAP)
        dataStore.edit { it[key] = newSecret }
        return newSecret
    }

    val isInitialSetupComplete: Flow<Boolean> = dataStore.data.map { it[PreferenceKeys.IS_INITIAL_SETUP_COMPLETE] ?: false }
    suspend fun setInitialSetupComplete(complete: Boolean) {
        dataStore.edit { it[PreferenceKeys.IS_INITIAL_SETUP_COMPLETE] = complete }
    }

    suspend fun clearUserData(uid: String) {
        dataStore.edit { preferences ->
            preferences.remove(PreferenceKeys.wrappedCloudKey(uid))
            preferences.remove(PreferenceKeys.wrappedMasterKey(uid))
            preferences.remove(PreferenceKeys.autoBackupEnabled(uid))
            preferences.remove(PreferenceKeys.lastBackupTime(uid))
            preferences.remove(PreferenceKeys.userBackupSecret(uid))
        }
    }
}
