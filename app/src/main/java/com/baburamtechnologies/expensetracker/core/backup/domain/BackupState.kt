package com.baburamtechnologies.expensetracker.core.backup.domain

import android.content.Intent

sealed class BackupState {
    object Idle : BackupState()
    object Preparing : BackupState()
    object Snapshotting : BackupState()
    object Encrypting : BackupState()
    object UploadingMetadata : BackupState()
    object UploadingDatabase : BackupState()
    object Verifying : BackupState()
    object Completed : BackupState()
    data class AuthorizationRequired(val intent: Intent) : BackupState()
    data class Failed(val message: String) : BackupState()
    object Cancelled : BackupState()
}

sealed class RestoreState {
    object Idle : RestoreState()
    object DownloadingMetadata : RestoreState()
    object VerifyingIntegrity : RestoreState()
    object DownloadingDatabase : RestoreState()
    object Decrypting : RestoreState()
    object ValidatingSchema : RestoreState()
    object SwappingDatabase : RestoreState()
    object Completed : RestoreState()
    data class AuthorizationRequired(val intent: Intent) : RestoreState()
    data class Failed(val message: String) : RestoreState()
}

val RestoreState.isActive: Boolean
    get() = this !is RestoreState.Idle && this !is RestoreState.Completed && this !is RestoreState.Failed

val RestoreState.isFailed: Boolean
    get() = this is RestoreState.Failed
