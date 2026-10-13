package com.baburamtechnologies.expensetracker.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

object PreferenceKeys {
    val WRAPPED_CLOUD_KEY = stringPreferencesKey("wrapped_cloud_key")
    val WRAPPED_MASTER_KEY = stringPreferencesKey("wrapped_master_key")
    val AUTO_BACKUP_ENABLED = booleanPreferencesKey("auto_backup_enabled")
    val LAST_BACKUP_TIME = stringPreferencesKey("last_backup_time")
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

    val wrappedCloudKey: Flow<String?> = dataStore.data.map { it[PreferenceKeys.WRAPPED_CLOUD_KEY] }
    val wrappedMasterKey: Flow<String?> = dataStore.data.map { it[PreferenceKeys.WRAPPED_MASTER_KEY] }
    
    suspend fun saveBackupKeys(wrappedCloudKey: String, wrappedMasterKey: String) {
        dataStore.edit { 
            it[PreferenceKeys.WRAPPED_CLOUD_KEY] = wrappedCloudKey 
            it[PreferenceKeys.WRAPPED_MASTER_KEY] = wrappedMasterKey
        }
    }

    val autoBackupEnabled: Flow<Boolean> = dataStore.data.map { it[PreferenceKeys.AUTO_BACKUP_ENABLED] ?: false }
    suspend fun setAutoBackupEnabled(enabled: Boolean) {
        dataStore.edit { it[PreferenceKeys.AUTO_BACKUP_ENABLED] = enabled }
    }

    val lastBackupTime: Flow<String?> = dataStore.data.map { it[PreferenceKeys.LAST_BACKUP_TIME] }
    suspend fun saveLastBackupTime(timestamp: String) {
        dataStore.edit { it[PreferenceKeys.LAST_BACKUP_TIME] = timestamp }
    }

    val isInitialSetupComplete: Flow<Boolean> = dataStore.data.map { it[PreferenceKeys.IS_INITIAL_SETUP_COMPLETE] ?: false }
    suspend fun setInitialSetupComplete(complete: Boolean) {
        dataStore.edit { it[PreferenceKeys.IS_INITIAL_SETUP_COMPLETE] = complete }
    }
}
