package com.baburamtechnologies.expensetracker.core.backup.domain

import com.baburamtechnologies.expensetracker.core.backup.model.BackupMetadata
import kotlinx.coroutines.flow.StateFlow

interface BackupRepository {
    val backupState: StateFlow<BackupState>
    val restoreState: StateFlow<RestoreState>

    suspend fun checkBackupAvailability(userEmail: String): Result<BackupMetadata?>
    suspend fun backup(userEmail: String, firebaseUid: String): Result<Unit>
    suspend fun restore(userEmail: String, firebaseUid: String): Result<Unit>
    suspend fun autoBackup(userEmail: String, firebaseUid: String): Result<Unit>

    suspend fun clearLocalData(): Result<Unit>
    suspend fun deleteUserData(firebaseUid: String): Result<Unit>
    suspend fun deleteAllCloudBackups(userEmail: String): Result<Unit>
}
