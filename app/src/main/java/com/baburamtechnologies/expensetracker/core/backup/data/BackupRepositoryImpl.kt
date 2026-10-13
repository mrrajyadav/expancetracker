@file:Suppress("DEPRECATION")
package com.baburamtechnologies.expensetracker.core.backup.data

import android.content.Context
import android.util.Base64
import com.baburamtechnologies.expensetracker.BuildConfig
import com.baburamtechnologies.expensetracker.core.backup.domain.BackupRepository
import com.baburamtechnologies.expensetracker.core.backup.domain.BackupState
import com.baburamtechnologies.expensetracker.core.backup.domain.RestoreState
import com.baburamtechnologies.expensetracker.core.backup.model.BackupMetadata
import com.baburamtechnologies.expensetracker.core.database.ExpenseTrackerDatabase
import com.baburamtechnologies.expensetracker.core.datastore.PreferenceManager
import com.baburamtechnologies.expensetracker.core.security.CryptoManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import androidx.work.WorkManager
import android.util.Log

@Singleton
class BackupRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: ExpenseTrackerDatabase,
    private val driveDataSource: GoogleDriveDataSource,
    private val cryptoManager: CryptoManager,
    private val preferenceManager: PreferenceManager
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

    private val dbName = ExpenseTrackerDatabase.DATABASE_NAME
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
            
            // Validate email
            if (userEmail.isBlank()) throw IllegalArgumentException("User email is required for backup")
            
            driveDataSource.setupService(userEmail)
            
            _backupState.value = BackupState.Snapshotting
            try {
                database.query("PRAGMA wal_checkpoint(FULL)", null).close()
            } catch (e: Exception) {
                // Ignore checkpoint errors, just proceed with copy
            }
            val dbFile = context.getDatabasePath(dbName)
            if (!dbFile.exists()) return@withContext Result.failure(Exception("Database file not found"))
            
            _backupState.value = BackupState.Encrypting
            val salt = cryptoManager.generateSalt()
            val masterKey = cryptoManager.deriveMasterKey(firebaseUid, salt)
            val cloudKey = cryptoManager.generateRandomKey()
            
            val dbBytes = dbFile.readBytes()
            val dbIv = cryptoManager.generateIv()
            val encryptedDb = cryptoManager.encrypt(dbBytes, cloudKey, dbIv)
            val checksum = cryptoManager.calculateChecksum(encryptedDb)
            
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
            val encryptedDbFile = File(context.cacheDir, encryptedFileName)
            encryptedDbFile.writeBytes(encryptedDb)
            driveDataSource.uploadFile(encryptedDbFile, uniqueDbName, "application/octet-stream")

            _backupState.value = BackupState.UploadingMetadata
            val metadataFile = File(context.cacheDir, metadataFileName)
            metadataFile.writeText(stableJson.encodeToString(signedMetadata))
            driveDataSource.uploadFile(metadataFile, metadataFileName, "application/json")
            
            val wrappedCloudKey = cryptoManager.wrapKey(cloudKey)
            val wrappedMasterKey = cryptoManager.wrapKey(masterKey.encoded)
            preferenceManager.saveBackupKeys(
                Base64.encodeToString(wrappedCloudKey, Base64.NO_WRAP),
                Base64.encodeToString(wrappedMasterKey, Base64.NO_WRAP)
            )
            preferenceManager.saveLastBackupTime(Instant.now().toString())

            cleanupOldBackups(uniqueDbName)

            _backupState.value = BackupState.Completed
            Result.success(Unit)
        } catch (e: UserRecoverableAuthIOException) {
            android.util.Log.e("BackupRepo", "Auth required", e)
            _backupState.value = BackupState.AuthorizationRequired(e.intent)
            Result.failure(e)
        } catch (e: Exception) {
            android.util.Log.e("BackupRepo", "Backup failed: ${e.message}", e)
            _backupState.value = BackupState.Failed(e.message ?: "Unknown error")
            Result.failure(e)
        }
    }

    override suspend fun autoBackup(userEmail: String, firebaseUid: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Setup drive service first
            driveDataSource.setupService(userEmail)
            
            // Check if we have wrapped keys in preferences
            val wrappedCloudKeyBase64 = preferenceManager.wrappedCloudKey.first()
            val wrappedMasterKeyBase64 = preferenceManager.wrappedMasterKey.first()
            
            val (cloudKey, masterKeyBytes) = if (wrappedCloudKeyBase64 != null && wrappedMasterKeyBase64 != null) {
                // Use existing keys
                Pair(
                    cryptoManager.unwrapKey(Base64.decode(wrappedCloudKeyBase64, Base64.NO_WRAP)),
                    cryptoManager.unwrapKey(Base64.decode(wrappedMasterKeyBase64, Base64.NO_WRAP))
                )
            } else {
                // If no keys in preferences, it might be the first auto-backup after password removal
                // or after a fresh install. We need to check if there is metadata on drive.
                val metadataFileId = driveDataSource.getFileIdByName(metadataFileName)
                if (metadataFileId == null) {
                    return@withContext Result.failure(Exception("No backup found on drive to perform auto-backup"))
                }
                
                val tempMetadataFile = File(context.cacheDir, "metadata_auto_init.json")
                driveDataSource.downloadFile(metadataFileId, tempMetadataFile)
                val existingMetadata = stableJson.decodeFromString<BackupMetadata>(tempMetadataFile.readText())
                
                val salt = Base64.decode(existingMetadata.salt, Base64.NO_WRAP)
                val masterKey = cryptoManager.deriveMasterKey(firebaseUid, salt)
                
                val keyIv = Base64.decode(existingMetadata.keyIv, Base64.NO_WRAP)
                val encryptedCloudKey = Base64.decode(existingMetadata.encryptedCloudKey, Base64.NO_WRAP)
                val cloudKey = cryptoManager.decrypt(encryptedCloudKey, masterKey.encoded, keyIv)
                
                // Save them for next time
                val wrappedCloudKey = cryptoManager.wrapKey(cloudKey)
                val wrappedMasterKey = cryptoManager.wrapKey(masterKey.encoded)
                preferenceManager.saveBackupKeys(
                    Base64.encodeToString(wrappedCloudKey, Base64.NO_WRAP),
                    Base64.encodeToString(wrappedMasterKey, Base64.NO_WRAP)
                )
                
                Pair(cloudKey, masterKey.encoded)
            }
            
            try {
                database.query("PRAGMA wal_checkpoint(FULL)", null).close()
            } catch (e: Exception) {
                // Ignore checkpoint errors
            }
            val dbFile = context.getDatabasePath(dbName)
            
            val dbIv = cryptoManager.generateIv()
            val encryptedDb = cryptoManager.encrypt(dbFile.readBytes(), cloudKey, dbIv)
            val checksum = cryptoManager.calculateChecksum(encryptedDb)
            
            val metadataFileId = driveDataSource.getFileIdByName(metadataFileName) ?: return@withContext Result.failure(Exception("No manual backup found"))
            val tempMetadataFile = File(context.cacheDir, "metadata_auto.json")
            driveDataSource.downloadFile(metadataFileId, tempMetadataFile)
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
            
            val newEncryptedDbFile = File(context.cacheDir, encryptedFileName)
            newEncryptedDbFile.writeBytes(encryptedDb)
            driveDataSource.uploadFile(newEncryptedDbFile, uniqueDbName, "application/octet-stream")

            val newMetadataFile = File(context.cacheDir, metadataFileName)
            newMetadataFile.writeText(stableJson.encodeToString(signedMetadata))
            driveDataSource.uploadFile(newMetadataFile, metadataFileName, "application/json")
            
            preferenceManager.saveLastBackupTime(Instant.now().toString())
            cleanupOldBackups(uniqueDbName)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun restore(userEmail: String, firebaseUid: String): Result<Unit> = withContext(Dispatchers.IO) {
        val rollbackFile = File(context.cacheDir, "backup_before_restore.db")
        val tempRestoreFile = File(context.cacheDir, "restore_test.db")
        
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
            
            _restoreState.value = RestoreState.VerifyingIntegrity
            
            // 1. Recover and validate salt
            val salt = Base64.decode(signedMetadata.salt, Base64.NO_WRAP)
            if (salt.size != 16) {
                val error = "Invalid backup salt recovered."
                _restoreState.value = RestoreState.Failed(error)
                return@withContext Result.failure(Exception(error))
            }
            Log.d(tag, "[RESTORE] Salt recovered successfully")
            
            // 2. Derive master key using RECOVERED salt and iterations
            val masterKey = cryptoManager.deriveMasterKey(firebaseUid, salt, signedMetadata.iterations)
            
            // 3. Verify integrity using STABLE canonical representation
            val expectedMac = generateStableMetadataMac(signedMetadata, masterKey.encoded)
            
            if (signedMetadata.metadataMac != expectedMac) {
                val error = "Integrity check failed: This backup might be protected by a password from an older version of the app. Please start fresh or use a new backup."
                _restoreState.value = RestoreState.Failed(error)
                return@withContext Result.failure(Exception(error))
            }

            if (signedMetadata.firebaseUid != firebaseUid) {
                val error = "Backup belongs to a different user"
                _restoreState.value = RestoreState.Failed(error)
                return@withContext Result.failure(Exception(error))
            }

            Log.i(tag, "[RESTORE] STAGE 4: Encrypted DB Download")
            _restoreState.value = RestoreState.DownloadingDatabase
            
            val targetDbFile = signedMetadata.dbFileName ?: encryptedFileName
            Log.d(tag, "[RESTORE] Target database file: $targetDbFile")
            
            val dataFileId = driveDataSource.getFileIdByName(targetDbFile)
            if (dataFileId == null) {
                val error = "Encrypted backup not found ($targetDbFile)"
                _restoreState.value = RestoreState.Failed(error)
                return@withContext Result.failure(Exception(error))
            }
            
            val tempEncryptedFile = File(context.cacheDir, encryptedFileName)
            driveDataSource.downloadFile(dataFileId, tempEncryptedFile)
            val encryptedBytes = tempEncryptedFile.readBytes()
            
            Log.i(tag, "[RESTORE] STAGE 5: Decryption & Checksum Validation")
            _restoreState.value = RestoreState.Decrypting
            val calculatedChecksum = cryptoManager.calculateChecksum(encryptedBytes)
            Log.d(tag, "[RESTORE] Expected checksum: ${signedMetadata.checksum}")
            Log.d(tag, "[RESTORE] Calculated checksum: $calculatedChecksum")
            
            if (calculatedChecksum != signedMetadata.checksum) {
                val error = "Database file corruption detected (checksum mismatch). Expected: ${signedMetadata.checksum}, Got: $calculatedChecksum"
                _restoreState.value = RestoreState.Failed(error)
                return@withContext Result.failure(Exception(error))
            }

            val keyIv = Base64.decode(signedMetadata.keyIv, Base64.NO_WRAP)
            val encryptedCloudKey = Base64.decode(signedMetadata.encryptedCloudKey, Base64.NO_WRAP)
            val cloudKey = cryptoManager.decrypt(encryptedCloudKey, masterKey.encoded, keyIv)
            
            val dbIv = Base64.decode(signedMetadata.dbIv, Base64.NO_WRAP)
            val decryptedDbBytes = cryptoManager.decrypt(encryptedBytes, cloudKey, dbIv)
            
            Log.i(tag, "[RESTORE] STAGE 6: SQLite Format Verification")
            _restoreState.value = RestoreState.ValidatingSchema
            tempRestoreFile.writeBytes(decryptedDbBytes)
            
            Log.d(tag, "[RESTORE] Decrypted size: ${decryptedDbBytes.size} bytes")
            val header = if (decryptedDbBytes.size >= 16) decryptedDbBytes.take(16).toByteArray() else byteArrayOf()
            
            // Safe logging of header hex (standard SQLite signature is not sensitive)
            val headerHex = header.joinToString("") { "%02x".format(it) }
            Log.d(tag, "[RESTORE] Decrypted header hex: $headerHex")

            if (!header.contentEquals(CryptoManager.SQLITE_HEADER)) {
                val error = "Decrypted data is not a valid SQLite database"
                _restoreState.value = RestoreState.Failed(error)
                return@withContext Result.failure(Exception(error))
            }

            Log.i(tag, "[RESTORE] STAGE 7: Atomic-style File Swap & Verification")
            _restoreState.value = RestoreState.SwappingDatabase
            
            // 1. Close the database
            Log.d(tag, "[RESTORE] Closing current database connection...")
            database.close()
            delay(1000) // Safety delay for connection cleanup
            
            val liveDbFile = context.getDatabasePath(dbName)
            val journalFile = File(liveDbFile.path + "-journal")
            val walFile = File(liveDbFile.path + "-wal")
            val shmFile = File(liveDbFile.path + "-shm")

            // 2. Create Rollback Point
            if (liveDbFile.exists()) {
                Log.d(tag, "[RESTORE] Creating rollback point...")
                if (!liveDbFile.renameTo(rollbackFile)) {
                    val error = "Failed to create rollback file. DB might be locked."
                    _restoreState.value = RestoreState.Failed(error)
                    return@withContext Result.failure(Exception(error))
                }
            }

            // 3. Clean up auxiliary files
            Log.d(tag, "[RESTORE] Cleaning old WAL/SHM files...")
            if (walFile.exists()) walFile.delete()
            if (shmFile.exists()) shmFile.delete()
            if (journalFile.exists()) journalFile.delete()

            // 4. Swap to new database
            Log.d(tag, "[RESTORE] Moving restored file to live path...")
            try {
                tempRestoreFile.copyTo(liveDbFile, overwrite = true)
                tempRestoreFile.delete()
            } catch (e: Exception) {
                Log.e(tag, "[RESTORE] SWAP FAILED: ${e.message}. Attempting rollback.")
                if (rollbackFile.exists()) rollbackFile.renameTo(liveDbFile)
                val error = "File system error during swap: ${e.message}"
                _restoreState.value = RestoreState.Failed(error)
                return@withContext Result.failure(Exception(error))
            }

            // 5. Verification - Lightweight
            if (!liveDbFile.exists() || liveDbFile.length() < 100) {
                Log.e(tag, "[RESTORE] VERIFICATION FAILED: Live DB invalid. Attempting rollback.")
                if (rollbackFile.exists()) {
                    liveDbFile.delete()
                    rollbackFile.renameTo(liveDbFile)
                }
                val error = "Restored database verification failed."
                _restoreState.value = RestoreState.Failed(error)
                return@withContext Result.failure(Exception(error))
            }

            // 6. Success - Finalize keys
            Log.d(tag, "[RESTORE] Finalizing backup keys...")
            val wrappedCloudKey = cryptoManager.wrapKey(cloudKey)
            val wrappedMasterKey = cryptoManager.wrapKey(masterKey.encoded)
            preferenceManager.saveBackupKeys(
                Base64.encodeToString(wrappedCloudKey, Base64.NO_WRAP),
                Base64.encodeToString(wrappedMasterKey, Base64.NO_WRAP)
            )

            if (rollbackFile.exists()) rollbackFile.delete()
            
            Log.i(tag, "[RESTORE] COMPLETED SUCCESSFULLY")
            _restoreState.value = RestoreState.Completed
            Result.success(Unit)
        } catch (e: UserRecoverableAuthIOException) {
            Log.e(tag, "[RESTORE] AUTH REQUIRED", e)
            _restoreState.value = RestoreState.AuthorizationRequired(e.intent)
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(tag, "[RESTORE] FATAL ERROR: ${e.message}", e)
            // Final safety rollback attempt
            if (rollbackFile.exists() && !context.getDatabasePath(dbName).exists()) {
                rollbackFile.renameTo(context.getDatabasePath(dbName))
            }
            _restoreState.value = RestoreState.Failed(e.message ?: "Unknown fatal error")
            Result.failure(e)
        } finally {
            if (tempRestoreFile.exists()) tempRestoreFile.delete()
        }
    }

    override suspend fun clearLocalData(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            database.clearAllTables()
        }
    }

    private suspend fun cleanupOldBackups(currentFileName: String) {
        try {
            val files = driveDataSource.listBackupFiles()
            files.filter { it.name.endsWith(".enc") && it.name != currentFileName && it.name != encryptedFileName }
                .forEach { file ->
                    Log.d(tag, "Cleaning up old backup file: ${file.name}")
                    driveDataSource.deleteFile(file.id)
                }
        } catch (e: Exception) {
            Log.w(tag, "Failed to cleanup old backups: ${e.message}")
        }
    }

    private fun generateStableMetadataMac(metadata: BackupMetadata, key: ByteArray): String {
        // Convert to JsonObject, remove MAC field, and sort keys for stability
        val jsonObject = stableJson.encodeToJsonElement(metadata).jsonObject
        val filteredMap = jsonObject.toMutableMap().apply { remove("metadataMac") }
        
        // Sort keys alphabetically to create a canonical string representation
        val sortedJson = buildJsonObject {
            filteredMap.keys.sorted().forEach { key ->
                put(key, filteredMap[key]!!)
            }
        }.toString()
        
        return cryptoManager.generateMetadataMac(sortedJson, key)
    }
}
