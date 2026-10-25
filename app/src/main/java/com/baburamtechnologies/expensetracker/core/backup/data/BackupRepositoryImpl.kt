@file:Suppress("DEPRECATION")
package com.baburamtechnologies.expensetracker.core.backup.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Base64
import android.util.Log
import androidx.room.withTransaction
import androidx.work.WorkManager
import com.baburamtechnologies.expensetracker.BuildConfig
import com.baburamtechnologies.expensetracker.core.backup.domain.BackupRepository
import com.baburamtechnologies.expensetracker.core.backup.domain.BackupState
import com.baburamtechnologies.expensetracker.core.backup.domain.RestoreState
import com.baburamtechnologies.expensetracker.core.backup.model.BackupMetadata
import com.baburamtechnologies.expensetracker.core.backup.model.UserBackupPayload
import com.baburamtechnologies.expensetracker.core.database.ExpenseTrackerDatabase
import com.baburamtechnologies.expensetracker.core.database.model.ProfileEntity
import com.baburamtechnologies.expensetracker.core.datastore.PreferenceManager
import com.baburamtechnologies.expensetracker.core.security.CryptoManager
import com.baburamtechnologies.expensetracker.feature.settings.data.local.model.PaymentModeEntity
import com.baburamtechnologies.expensetracker.feature.settings.data.local.model.UserSettingsEntity
import com.baburamtechnologies.expensetracker.feature.transactions.data.local.model.CategoryEntity
import com.baburamtechnologies.expensetracker.feature.transactions.data.local.model.TransactionEntity
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import java.io.File
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: ExpenseTrackerDatabase,
    private val driveDataSource: GoogleDriveDataSource,
    private val cryptoManager: CryptoManager,
    private val preferenceManager: PreferenceManager,
    private val backupScheduleManager: com.baburamtechnologies.expensetracker.core.sync.BackupScheduleManager
) : BackupRepository {

    private val tag = "BackupRepo"
    private val stableJson = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
        prettyPrint = false
    }

    private val _backupState = MutableStateFlow<BackupState>(BackupState.Idle)
    override val backupState: StateFlow<BackupState> = _backupState.asStateFlow()

    private val _restoreState = MutableStateFlow<RestoreState>(RestoreState.Idle)
    override val restoreState: StateFlow<RestoreState> = _restoreState.asStateFlow()

    private val encryptedFileName = "expense_backup.enc"
    private val metadataFileName = "backup_metadata.json"

    override suspend fun checkBackupAvailability(userEmail: String): Result<BackupMetadata?> = withContext(Dispatchers.IO) {
        try {
            driveDataSource.setupService(userEmail)
            val fileId = driveDataSource.getFileIdByName(metadataFileName) ?: return@withContext Result.success(null)
            
            val tempFile = File(context.cacheDir, "check_metadata.json")
            driveDataSource.downloadFile(fileId, tempFile)
            
            val metadata = Json.decodeFromString<BackupMetadata>(tempFile.readText())
            Result.success(metadata)
        } catch (e: UserRecoverableAuthIOException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun backup(userEmail: String, firebaseUid: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            _backupState.value = BackupState.Preparing
            
            if (userEmail.isBlank()) throw IllegalArgumentException("User email is required for backup")
            if (firebaseUid.isBlank()) throw IllegalArgumentException("User ID is required for backup")
            
            driveDataSource.setupService(userEmail)
            
            _backupState.value = BackupState.Snapshotting
            
            val payload = createUserBackupPayload(firebaseUid)
            val payloadBytes = stableJson.encodeToString(payload).toByteArray(Charsets.UTF_8)
            
            _backupState.value = BackupState.Encrypting
            val salt = cryptoManager.generateSalt()
            val userSecret = preferenceManager.getOrCreateUserBackupSecret(firebaseUid)
            val masterKey = cryptoManager.deriveMasterKey(userSecret, salt)
            val cloudKey = cryptoManager.generateRandomKey()
            
            val dbIv = cryptoManager.generateIv()
            val encryptedPayload = cryptoManager.encrypt(payloadBytes, cloudKey, dbIv)
            val checksum = cryptoManager.calculateChecksum(encryptedPayload)
            
            val keyIv = cryptoManager.generateIv()
            val encryptedCloudKey = cryptoManager.encrypt(cloudKey, masterKey.encoded, keyIv)
            
            val uniqueDbName = "db_${System.currentTimeMillis()}.enc"
            
            val metadata = BackupMetadata(
                backupId = UUID.randomUUID().toString(),
                appVersion = BuildConfig.VERSION_NAME,
                schemaVersion = database.openHelper.readableDatabase.version,
                firebaseUid = firebaseUid,
                createdAt = Instant.now().toString(),
                salt = Base64.encodeToString(salt, Base64.NO_WRAP),
                dbIv = Base64.encodeToString(dbIv, Base64.NO_WRAP),
                keyIv = Base64.encodeToString(keyIv, Base64.NO_WRAP),
                encryptedCloudKey = Base64.encodeToString(encryptedCloudKey, Base64.NO_WRAP),
                dbFileName = uniqueDbName,
                checksum = checksum
            )
            
            val mac = generateStableMetadataMac(metadata, masterKey.encoded)
            val signedMetadata = metadata.copy(metadataMac = mac)
            
            _backupState.value = BackupState.UploadingDatabase
            val encryptedDataFile = File(context.cacheDir, encryptedFileName)
            encryptedDataFile.writeBytes(encryptedPayload)
            driveDataSource.uploadFile(encryptedDataFile, uniqueDbName, "application/octet-stream")

            _backupState.value = BackupState.UploadingMetadata
            val metadataFile = File(context.cacheDir, metadataFileName)
            metadataFile.writeText(stableJson.encodeToString(signedMetadata))
            driveDataSource.uploadFile(metadataFile, metadataFileName, "application/json")
            
            // Backup the user secret using deterministic recovery key for seamless restore
            val recoverySalt = cryptoManager.generateSalt()
            val recoveryIv = cryptoManager.generateIv()
            val recoveryMasterKey = cryptoManager.deriveLegacyMasterKey(firebaseUid, recoverySalt, 310000)
            val recoveryEncryptedSecret = cryptoManager.encrypt(userSecret.toByteArray(Charsets.UTF_8), recoveryMasterKey.encoded, recoveryIv)
            val recoveryPayload = buildJsonObject {
                put("salt", Base64.encodeToString(recoverySalt, Base64.NO_WRAP))
                put("iv", Base64.encodeToString(recoveryIv, Base64.NO_WRAP))
                put("encryptedSecret", Base64.encodeToString(recoveryEncryptedSecret, Base64.NO_WRAP))
            }
            val recoveryFile = File(context.cacheDir, "recovery_metadata.json")
            recoveryFile.writeText(recoveryPayload.toString())
            driveDataSource.uploadFile(recoveryFile, "recovery_metadata.json", "application/json")
            
            val wrappedCloudKey = cryptoManager.wrapKey(cloudKey)
            val wrappedMasterKey = cryptoManager.wrapKey(masterKey.encoded)
            preferenceManager.saveBackupKeys(
                firebaseUid,
                Base64.encodeToString(wrappedCloudKey, Base64.NO_WRAP),
                Base64.encodeToString(wrappedMasterKey, Base64.NO_WRAP)
            )
            preferenceManager.saveLastBackupTime(firebaseUid, Instant.now().toString())

            cleanupOldBackups(uniqueDbName)

            _backupState.value = BackupState.Completed
            Result.success(Unit)
        } catch (e: UserRecoverableAuthIOException) {
            Log.e(tag, "Auth required", e)
            _backupState.value = BackupState.AuthorizationRequired(e.intent)
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(tag, "Backup failed: ${e.message}", e)
            _backupState.value = BackupState.Failed(e.message ?: "Unknown error")
            Result.failure(e)
        }
    }

    override suspend fun autoBackup(userEmail: String, firebaseUid: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (userEmail.isBlank() || firebaseUid.isBlank()) {
                return@withContext Result.failure(Exception("User credentials missing for auto backup"))
            }

            driveDataSource.setupService(userEmail)

            val metadataFileId = driveDataSource.getFileIdByName(metadataFileName)
            if (metadataFileId != null) {
                val tempMetadataFile = File(context.cacheDir, "metadata_auto_check.json")
                driveDataSource.downloadFile(metadataFileId, tempMetadataFile)
                val existingMetadata = stableJson.decodeFromString<BackupMetadata>(tempMetadataFile.readText())
                if (existingMetadata.firebaseUid != firebaseUid) {
                    Log.w(tag, "[AUTO_BACKUP] Skipped: Drive backup owner (${existingMetadata.firebaseUid}) does not match current user ($firebaseUid)")
                    return@withContext Result.failure(Exception("Drive backup metadata belongs to a different user"))
                }
            }
            
            val wrappedCloudKeyBase64 = preferenceManager.wrappedCloudKey(firebaseUid).first()
            val wrappedMasterKeyBase64 = preferenceManager.wrappedMasterKey(firebaseUid).first()
            
            val (cloudKey, masterKeyBytes) = if (wrappedCloudKeyBase64 != null && wrappedMasterKeyBase64 != null) {
                Pair(
                    cryptoManager.unwrapKey(Base64.decode(wrappedCloudKeyBase64, Base64.NO_WRAP)),
                    cryptoManager.unwrapKey(Base64.decode(wrappedMasterKeyBase64, Base64.NO_WRAP))
                )
            } else {
                if (metadataFileId == null) {
                    return@withContext Result.failure(Exception("No backup found on drive to perform auto-backup"))
                }
                
                val tempMetadataFile = File(context.cacheDir, "metadata_auto_init.json")
                driveDataSource.downloadFile(metadataFileId, tempMetadataFile)
                val existingMetadata = stableJson.decodeFromString<BackupMetadata>(tempMetadataFile.readText())
                
                val salt = Base64.decode(existingMetadata.salt, Base64.NO_WRAP)
                val userSecret = preferenceManager.getOrCreateUserBackupSecret(firebaseUid)
                val masterKey = cryptoManager.deriveMasterKey(userSecret, salt)
                
                val keyIv = Base64.decode(existingMetadata.keyIv, Base64.NO_WRAP)
                val encryptedCloudKey = Base64.decode(existingMetadata.encryptedCloudKey, Base64.NO_WRAP)
                val cloudKey = cryptoManager.decrypt(encryptedCloudKey, masterKey.encoded, keyIv)
                
                val wrappedCloudKey = cryptoManager.wrapKey(cloudKey)
                val wrappedMasterKey = cryptoManager.wrapKey(masterKey.encoded)
                preferenceManager.saveBackupKeys(
                    firebaseUid,
                    Base64.encodeToString(wrappedCloudKey, Base64.NO_WRAP),
                    Base64.encodeToString(wrappedMasterKey, Base64.NO_WRAP)
                )
                
                Pair(cloudKey, masterKey.encoded)
            }
            
            val payload = createUserBackupPayload(firebaseUid)
            val payloadBytes = stableJson.encodeToString(payload).toByteArray(Charsets.UTF_8)
            
            val dbIv = cryptoManager.generateIv()
            val encryptedPayload = cryptoManager.encrypt(payloadBytes, cloudKey, dbIv)
            val checksum = cryptoManager.calculateChecksum(encryptedPayload)
            
            val metaId = driveDataSource.getFileIdByName(metadataFileName) ?: return@withContext Result.failure(Exception("No metadata file found"))
            val tempMetadataFile = File(context.cacheDir, "metadata_auto.json")
            driveDataSource.downloadFile(metaId, tempMetadataFile)
            val existingMetadata = stableJson.decodeFromString<BackupMetadata>(tempMetadataFile.readText())
            
            val uniqueDbName = "db_${System.currentTimeMillis()}.enc"

            val newMetadata = existingMetadata.copy(
                createdAt = Instant.now().toString(),
                dbIv = Base64.encodeToString(dbIv, Base64.NO_WRAP),
                dbFileName = uniqueDbName,
                checksum = checksum,
                metadataMac = null
            )
            
            val mac = generateStableMetadataMac(newMetadata, masterKeyBytes)
            val signedMetadata = newMetadata.copy(metadataMac = mac)
            
            val newEncryptedDataFile = File(context.cacheDir, encryptedFileName)
            newEncryptedDataFile.writeBytes(encryptedPayload)
            driveDataSource.uploadFile(newEncryptedDataFile, uniqueDbName, "application/octet-stream")

            val newMetadataFile = File(context.cacheDir, metadataFileName)
            newMetadataFile.writeText(stableJson.encodeToString(signedMetadata))
            driveDataSource.uploadFile(newMetadataFile, metadataFileName, "application/json")
            
            preferenceManager.saveLastBackupTime(firebaseUid, Instant.now().toString())
            cleanupOldBackups(uniqueDbName)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(tag, "Auto backup failed", e)
            Result.failure(e)
        }
    }

    override suspend fun restore(userEmail: String, firebaseUid: String): Result<Unit> = withContext(Dispatchers.IO) {
        val tempRestoreFile = File(context.cacheDir, "restore_temp.bin")
        
        try {
            Log.i(tag, "[RESTORE] STAGE 1: Preparation & Task Cancellation")
            _restoreState.value = RestoreState.DownloadingMetadata
            
            try {
                WorkManager.getInstance(context).cancelUniqueWork("PeriodicBackup")
                WorkManager.getInstance(context).cancelUniqueWork("OneTimeBackup")
                Log.d(tag, "[RESTORE] Background backup tasks cancelled")
            } catch (e: Exception) {
                Log.w(tag, "[RESTORE] Non-critical: Failed to cancel tasks: ${e.message}")
            }

            if (userEmail.isBlank()) throw IllegalArgumentException("User email is required for restore")
            if (firebaseUid.isBlank()) throw IllegalArgumentException("User ID is required for restore")
            driveDataSource.setupService(userEmail)
            
            Log.i(tag, "[RESTORE] STAGE 2: Metadata Download")
            val metadataFileId = driveDataSource.getFileIdByName(metadataFileName)
            if (metadataFileId == null) {
                val error = "Backup metadata not found"
                _restoreState.value = RestoreState.Failed(error)
                return@withContext Result.failure(Exception(error))
            }
            
            val tempMetadataFile = File(context.cacheDir, metadataFileName)
            driveDataSource.downloadFile(metadataFileId, tempMetadataFile)
            val signedMetadata = stableJson.decodeFromString<BackupMetadata>(tempMetadataFile.readText())
            
            if (signedMetadata.firebaseUid != firebaseUid) {
                val error = "Backup belongs to a different user (${signedMetadata.firebaseUid})"
                _restoreState.value = RestoreState.Failed(error)
                return@withContext Result.failure(Exception(error))
            }

            // Attempt seamless secret recovery first
            val recoveryFileId = driveDataSource.getFileIdByName("recovery_metadata.json")
            if (recoveryFileId != null) {
                val tempRecoveryFile = File(context.cacheDir, "recovery_metadata.json")
                try {
                    driveDataSource.downloadFile(recoveryFileId, tempRecoveryFile)
                    val recoveryJson = stableJson.parseToJsonElement(tempRecoveryFile.readText()).jsonObject
                    val recSalt = Base64.decode(recoveryJson["salt"]!!.jsonPrimitive.content, Base64.NO_WRAP)
                    val recIv = Base64.decode(recoveryJson["iv"]!!.jsonPrimitive.content, Base64.NO_WRAP)
                    val recEncrypted = Base64.decode(recoveryJson["encryptedSecret"]!!.jsonPrimitive.content, Base64.NO_WRAP)
                    val recMasterKey = cryptoManager.deriveLegacyMasterKey(firebaseUid, recSalt, 310000)
                    val decryptedSecretBytes = cryptoManager.decrypt(recEncrypted, recMasterKey.encoded, recIv)
                    val recoveredSecret = String(decryptedSecretBytes, Charsets.UTF_8)
                    preferenceManager.saveUserBackupSecret(firebaseUid, recoveredSecret)
                    Log.i(tag, "[RESTORE] Seamless user secret recovery succeeded.")
                } catch (e: Exception) {
                    Log.w(tag, "[RESTORE] Failed to recover user secret seamlessly", e)
                } finally {
                    if (tempRecoveryFile.exists()) tempRecoveryFile.delete()
                }
            }

            // Schema & Version Compatibility Verification
            val currentSchemaVersion = database.openHelper.readableDatabase.version
            if (signedMetadata.schemaVersion > currentSchemaVersion) {
                val error = "Backup schema version (${signedMetadata.schemaVersion}) is higher than current app schema version ($currentSchemaVersion). Please update the app."
                _restoreState.value = RestoreState.Failed(error)
                return@withContext Result.failure(Exception(error))
            }

            _restoreState.value = RestoreState.VerifyingIntegrity
            
            val salt = Base64.decode(signedMetadata.salt, Base64.NO_WRAP)
            if (salt.size != 16) {
                val error = "Invalid backup salt recovered."
                _restoreState.value = RestoreState.Failed(error)
                return@withContext Result.failure(Exception(error))
            }
            
            val userSecret = preferenceManager.getOrCreateUserBackupSecret(firebaseUid)
            var masterKey = cryptoManager.deriveMasterKey(userSecret, salt, signedMetadata.iterations)
            val expectedMac = generateStableMetadataMac(signedMetadata, masterKey.encoded)
            
            if (signedMetadata.metadataMac != expectedMac) {
                Log.w(tag, "[RESTORE] Primary MAC check failed. Trying legacy master key derivation...")
                val legacyMasterKey = cryptoManager.deriveLegacyMasterKey(firebaseUid, salt, signedMetadata.iterations)
                val legacyExpectedMac = generateStableMetadataMac(signedMetadata, legacyMasterKey.encoded)
                if (signedMetadata.metadataMac == legacyExpectedMac) {
                    Log.i(tag, "[RESTORE] Legacy master key verified successfully! Migrating backup key...")
                    masterKey = legacyMasterKey
                } else {
                    val error = "Integrity check failed: Backup metadata MAC mismatch."
                    _restoreState.value = RestoreState.Failed(error)
                    return@withContext Result.failure(Exception(error))
                }
            }

            Log.i(tag, "[RESTORE] STAGE 3: Encrypted Backup Download")
            _restoreState.value = RestoreState.DownloadingDatabase
            
            val targetDataFile = signedMetadata.dbFileName ?: encryptedFileName
            val dataFileId = driveDataSource.getFileIdByName(targetDataFile)
            if (dataFileId == null) {
                val error = "Encrypted backup file not found ($targetDataFile)"
                _restoreState.value = RestoreState.Failed(error)
                return@withContext Result.failure(Exception(error))
            }
            
            val tempEncryptedFile = File(context.cacheDir, encryptedFileName)
            driveDataSource.downloadFile(dataFileId, tempEncryptedFile)
            val encryptedBytes = tempEncryptedFile.readBytes()
            
            Log.i(tag, "[RESTORE] STAGE 4: Decryption & Checksum Validation")
            _restoreState.value = RestoreState.Decrypting
            val calculatedChecksum = cryptoManager.calculateChecksum(encryptedBytes)
            
            if (calculatedChecksum != signedMetadata.checksum) {
                val error = "Data corruption detected (checksum mismatch)."
                _restoreState.value = RestoreState.Failed(error)
                return@withContext Result.failure(Exception(error))
            }

            val keyIv = Base64.decode(signedMetadata.keyIv, Base64.NO_WRAP)
            val encryptedCloudKey = Base64.decode(signedMetadata.encryptedCloudKey, Base64.NO_WRAP)
            val cloudKey = cryptoManager.decrypt(encryptedCloudKey, masterKey.encoded, keyIv)
            
            val dbIv = Base64.decode(signedMetadata.dbIv, Base64.NO_WRAP)
            val decryptedBytes = cryptoManager.decrypt(encryptedBytes, cloudKey, dbIv)
            
            Log.i(tag, "[RESTORE] STAGE 5: Payload Processing & Fail-Safe Rollback Capture")
            _restoreState.value = RestoreState.SwappingDatabase

            // Capture pre-restore snapshot to enable rollback on validation failure
            val preRestoreSnapshot = try {
                createUserBackupPayload(firebaseUid)
            } catch (e: Exception) {
                Log.w(tag, "[RESTORE] Pre-restore snapshot skipped: ${e.message}")
                null
            }

            try {
                val header = if (decryptedBytes.size >= 16) decryptedBytes.take(16).toByteArray() else byteArrayOf()
                if (header.contentEquals(CryptoManager.SQLITE_HEADER)) {
                    Log.i(tag, "[RESTORE] Processing legacy full SQLite DB backup...")
                    tempRestoreFile.writeBytes(decryptedBytes)
                    restoreLegacySqliteDb(tempRestoreFile, firebaseUid)
                } else {
                    Log.i(tag, "[RESTORE] Processing UserBackupPayload JSON backup...")
                    val payloadJson = String(decryptedBytes, Charsets.UTF_8)
                    val payload = stableJson.decodeFromString<UserBackupPayload>(payloadJson)
                    if (payload.firebaseUid != firebaseUid) {
                        val error = "Backup payload user mismatch (${payload.firebaseUid})"
                        _restoreState.value = RestoreState.Failed(error)
                        return@withContext Result.failure(Exception(error))
                    }
                    restoreUserPayload(payload)
                }

                // Post-restore integrity and query validation
                verifyDatabaseIntegrity()
                verifyEssentialQueries(firebaseUid)
                Log.i(tag, "[RESTORE] Post-restore integrity & query verification succeeded.")
            } catch (e: Exception) {
                Log.e(tag, "[RESTORE] Post-restore validation failed: ${e.message}. Triggering automatic rollback...", e)
                if (preRestoreSnapshot != null) {
                    try {
                        restoreUserPayload(preRestoreSnapshot)
                        Log.i(tag, "[RESTORE] Rollback to pre-restore snapshot completed successfully.")
                    } catch (rollbackErr: Exception) {
                        Log.e(tag, "[RESTORE] CRITICAL: Rollback failed: ${rollbackErr.message}", rollbackErr)
                    }
                }
                val error = "Restore verification failed: ${e.message}. System rolled back to previous state."
                _restoreState.value = RestoreState.Failed(error)
                return@withContext Result.failure(Exception(error, e))
            }

            Log.d(tag, "[RESTORE] Finalizing backup keys...")
            val wrappedCloudKey = cryptoManager.wrapKey(cloudKey)
            val wrappedMasterKey = cryptoManager.wrapKey(masterKey.encoded)
            preferenceManager.saveBackupKeys(
                firebaseUid,
                Base64.encodeToString(wrappedCloudKey, Base64.NO_WRAP),
                Base64.encodeToString(wrappedMasterKey, Base64.NO_WRAP)
            )

            Log.i(tag, "[RESTORE] COMPLETED SUCCESSFULLY")
            _restoreState.value = RestoreState.Completed
            Result.success(Unit)
        } catch (e: UserRecoverableAuthIOException) {
            Log.e(tag, "[RESTORE] AUTH REQUIRED", e)
            _restoreState.value = RestoreState.AuthorizationRequired(e.intent)
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(tag, "[RESTORE] FATAL ERROR: ${e.message}", e)
            _restoreState.value = RestoreState.Failed(e.message ?: "Unknown fatal error")
            Result.failure(e)
        } finally {
            if (tempRestoreFile.exists()) tempRestoreFile.delete()
            // Restore auto-backup scheduling if it was enabled (for any exit path)
            try {
                val isAutoBackupEnabled = preferenceManager.autoBackupEnabled(firebaseUid).first()
                if (isAutoBackupEnabled) {
                    backupScheduleManager.schedulePeriodicBackup()
                    Log.d(tag, "[RESTORE] Periodic backup schedule restored")
                }
            } catch (e: Exception) {
                Log.w(tag, "[RESTORE] Failed to restore backup scheduling: ${e.message}")
            }
        }
    }

    override suspend fun clearLocalData(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            database.clearAllTables()
        }
    }

    override suspend fun deleteUserData(firebaseUid: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            database.withTransaction {
                database.transactionDao().deleteTransactionsForUser(firebaseUid)
                database.paymentModeDao().deletePaymentModesForUser(firebaseUid)
                database.userSettingsDao().deleteUserSettingsForUser(firebaseUid)
                database.categoryDao().deleteCategoriesForUser(firebaseUid)
                database.profileDao().deleteProfile(firebaseUid)
            }
            preferenceManager.clearUserData(firebaseUid)
            try {
                WorkManager.getInstance(context).cancelUniqueWork("PeriodicBackup")
                WorkManager.getInstance(context).cancelUniqueWork("OneTimeBackup")
            } catch (e: Exception) {
                Log.w(tag, "Failed to cancel work manager tasks during account deletion: ${e.message}")
            }
            Unit
        }
    }

    override suspend fun deleteAllCloudBackups(userEmail: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            if (userEmail.isNotBlank()) {
                driveDataSource.setupService(userEmail)
                val files = driveDataSource.listBackupFiles()
                files.forEach { file ->
                    Log.d(tag, "Deleting cloud backup file: ${file.name} (${file.id})")
                    driveDataSource.deleteFile(file.id)
                }
            }
            Unit
        }
    }

    private fun checkpointAndFlushWal() {
        try {
            val writableDb = database.openHelper.writableDatabase
            val cursor = writableDb.query("PRAGMA wal_checkpoint(FULL)")
            if (cursor.moveToFirst()) {
                val busy = cursor.getInt(0)
                val log = cursor.getInt(1)
                val checkpointed = cursor.getInt(2)
                Log.d(tag, "[SNAPSHOT] WAL checkpoint FULL result: busy=$busy, log=$log, checkpointed=$checkpointed")
                if (busy != 0) {
                    Log.w(tag, "[SNAPSHOT] WAL checkpoint reported busy, executing TRUNCATE checkpoint...")
                    writableDb.query("PRAGMA wal_checkpoint(TRUNCATE)").close()
                }
            }
            cursor.close()
        } catch (e: Exception) {
            Log.e(tag, "[SNAPSHOT] WAL checkpoint failed: ${e.message}", e)
        }
    }

    private fun verifyDatabaseIntegrity() {
        val cursor = database.openHelper.writableDatabase.query("PRAGMA quick_check")
        var result = ""
        if (cursor.moveToFirst()) {
            result = cursor.getString(0)
        }
        cursor.close()
        if (!result.equals("ok", ignoreCase = true)) {
            throw IllegalStateException("SQLite quick_check failed with result: $result")
        }
    }

    private suspend fun verifyEssentialQueries(firebaseUid: String) {
        database.profileDao().getProfileByIdSync(firebaseUid)
        database.userSettingsDao().getUserSettingsForUser(firebaseUid)
        database.transactionDao().getAllTransactionsForUser(firebaseUid)
        database.categoryDao().getAllCategoriesForUser(firebaseUid)
        database.paymentModeDao().getAllPaymentModesForUser(firebaseUid)
    }

    private suspend fun createUserBackupPayload(firebaseUid: String): UserBackupPayload {
        checkpointAndFlushWal()
        return database.withTransaction {
            val profile = database.profileDao().getProfileByIdSync(firebaseUid)
            val transactions = database.transactionDao().getAllTransactionsForUser(firebaseUid)
            val categories = database.categoryDao().getAllCategoriesForUser(firebaseUid)
            val paymentModes = database.paymentModeDao().getAllPaymentModesForUser(firebaseUid)
            val userSettings = database.userSettingsDao().getUserSettingsForUser(firebaseUid)

            UserBackupPayload(
                firebaseUid = firebaseUid,
                profile = profile,
                transactions = transactions,
                categories = categories,
                paymentModes = paymentModes,
                userSettings = userSettings
            )
        }
    }

    private suspend fun restoreUserPayload(payload: UserBackupPayload) {
        val uid = payload.firebaseUid
        database.withTransaction {
            database.transactionDao().deleteTransactionsForUser(uid)
            database.paymentModeDao().deletePaymentModesForUser(uid)
            database.userSettingsDao().deleteUserSettingsForUser(uid)
            database.categoryDao().deleteCategoriesForUser(uid)
            database.profileDao().deleteProfile(uid)

            payload.profile?.let { database.profileDao().insertProfile(it) }
            payload.userSettings?.let { database.userSettingsDao().insertUserSettings(it) }
            if (payload.paymentModes.isNotEmpty()) {
                database.paymentModeDao().insertPaymentModes(payload.paymentModes)
            }
            if (payload.categories.isNotEmpty()) {
                database.categoryDao().insertCategories(payload.categories)
            }
            if (payload.transactions.isNotEmpty()) {
                database.transactionDao().insertTransactions(payload.transactions)
            }
        }
    }

    private suspend fun restoreLegacySqliteDb(tempDbFile: File, firebaseUid: String) {
        var sqliteDb: SQLiteDatabase? = null
        try {
            sqliteDb = SQLiteDatabase.openDatabase(tempDbFile.path, null, SQLiteDatabase.OPEN_READONLY)
            
            // Verify integrity of legacy SQLite file
            sqliteDb.rawQuery("PRAGMA quick_check", null).use { cursor ->
                var checkResult = ""
                if (cursor.moveToFirst()) {
                    checkResult = cursor.getString(0)
                }
                if (!checkResult.equals("ok", ignoreCase = true)) {
                    throw IllegalStateException("Legacy backup SQLite file failed integrity check: $checkResult")
                }
            }

            val profiles = mutableListOf<ProfileEntity>()
            sqliteDb.rawQuery("SELECT id, full_name, email, phone, created_at, updated_at FROM profiles WHERE id = ?", arrayOf(firebaseUid)).use { cursor ->
                while (cursor.moveToNext()) {
                    profiles.add(ProfileEntity(
                        id = cursor.getString(0),
                        fullName = cursor.getString(1),
                        email = cursor.getString(2),
                        phone = if (cursor.isNull(3)) null else cursor.getString(3),
                        createdAt = if (cursor.isNull(4)) null else cursor.getString(4),
                        updatedAt = cursor.getString(5)
                    ))
                }
            }

            var userSettings: UserSettingsEntity? = null
            sqliteDb.rawQuery("SELECT user_id, currency, theme, language, date_format, financial_year, start_of_week, updated_at FROM user_settings WHERE user_id = ?", arrayOf(firebaseUid)).use { cursor ->
                if (cursor.moveToNext()) {
                    userSettings = UserSettingsEntity(
                        userId = cursor.getString(0),
                        currency = cursor.getString(1),
                        theme = cursor.getString(2),
                        language = cursor.getString(3),
                        dateFormat = cursor.getString(4),
                        financialYear = cursor.getString(5),
                        startOfWeek = cursor.getString(6),
                        updatedAt = cursor.getString(7)
                    )
                }
            }

            val paymentModes = mutableListOf<PaymentModeEntity>()
            sqliteDb.rawQuery("SELECT id, user_id, name, display_order, is_system, is_active, created_at, updated_at FROM payment_modes WHERE user_id = ?", arrayOf(firebaseUid)).use { cursor ->
                while (cursor.moveToNext()) {
                    paymentModes.add(PaymentModeEntity(
                        id = cursor.getString(0),
                        userId = cursor.getString(1),
                        name = cursor.getString(2),
                        displayOrder = cursor.getInt(3),
                        isSystem = cursor.getInt(4) == 1,
                        isActive = cursor.getInt(5) == 1,
                        createdAt = cursor.getString(6),
                        updatedAt = cursor.getString(7)
                    ))
                }
            }

            val categories = mutableListOf<CategoryEntity>()
            sqliteDb.rawQuery("SELECT id, profile_id, parent_id, type, level, name, display_order, is_system, is_active, created_at, updated_at FROM categories WHERE profile_id IS NULL OR profile_id = ?", arrayOf(firebaseUid)).use { cursor ->
                while (cursor.moveToNext()) {
                    categories.add(CategoryEntity(
                        id = cursor.getString(0),
                        profileId = if (cursor.isNull(1)) null else cursor.getString(1),
                        parentId = if (cursor.isNull(2)) null else cursor.getString(2),
                        type = cursor.getString(3),
                        level = cursor.getInt(4),
                        name = cursor.getString(5),
                        displayOrder = cursor.getInt(6),
                        isSystem = cursor.getInt(7) == 1,
                        isActive = cursor.getInt(8) == 1,
                        createdAt = cursor.getString(9),
                        updatedAt = cursor.getString(10)
                    ))
                }
            }

            val transactions = mutableListOf<TransactionEntity>()
            sqliteDb.rawQuery("SELECT id, user_id, amount, type, category_level1_id, category_level2_id, category_level3_id, payment_mode_id, note, transaction_date, created_at, updated_at, is_deleted FROM transactions WHERE user_id = ?", arrayOf(firebaseUid)).use { cursor ->
                while (cursor.moveToNext()) {
                    val rawAmountType = cursor.getType(2)
                    val amountInPaise = if (rawAmountType == android.database.Cursor.FIELD_TYPE_FLOAT) {
                        Math.round(cursor.getDouble(2) * 100.0)
                    } else {
                        cursor.getLong(2)
                    }
                    transactions.add(TransactionEntity(
                        id = cursor.getString(0),
                        userId = cursor.getString(1),
                        amount = amountInPaise,
                        type = cursor.getString(3),
                        categoryLevel1Id = cursor.getString(4),
                        categoryLevel2Id = cursor.getString(5),
                        categoryLevel3Id = if (cursor.isNull(6)) null else cursor.getString(6),
                        paymentModeId = if (cursor.isNull(7)) null else cursor.getString(7),
                        note = if (cursor.isNull(8)) null else cursor.getString(8),
                        transactionDate = cursor.getString(9),
                        createdAt = cursor.getString(10),
                        updatedAt = cursor.getString(11),
                        isDeleted = cursor.getInt(12) == 1
                    ))
                }
            }

            val payload = UserBackupPayload(
                firebaseUid = firebaseUid,
                profile = profiles.firstOrNull(),
                transactions = transactions,
                categories = categories.filter { !it.isSystem },
                paymentModes = paymentModes,
                userSettings = userSettings
            )
            restoreUserPayload(payload)
        } finally {
            sqliteDb?.close()
        }
    }

    private suspend fun cleanupOldBackups(currentFileName: String) {
        try {
            val files = driveDataSource.listBackupFiles()
            val encFiles = files.filter { it.name.endsWith(".enc") && it.name != currentFileName && it.name != encryptedFileName }
                .sortedByDescending { it.createdTime?.value ?: 0L }

            // Keep the 2 most recent backups (excluding the current ones)
            if (encFiles.size > 2) {
                encFiles.drop(2).forEach { file ->
                    Log.d(tag, "Cleaning up old backup file: ${file.name}")
                    driveDataSource.deleteFile(file.id)
                }
            }
        } catch (e: Exception) {
            Log.w(tag, "Failed to cleanup old backups: ${e.message}")
        }
    }

    private fun generateStableMetadataMac(metadata: BackupMetadata, key: ByteArray): String {
        val jsonObject = stableJson.encodeToJsonElement(metadata).jsonObject
        val filteredMap = jsonObject.toMutableMap().apply { remove("metadataMac") }
        
        val sortedJson = buildJsonObject {
            filteredMap.keys.sorted().forEach { key ->
                put(key, filteredMap[key]!!)
            }
        }.toString()
        
        return cryptoManager.generateMetadataMac(sortedJson, key)
    }
}
