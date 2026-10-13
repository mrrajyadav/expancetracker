package com.baburamtechnologies.expensetracker.core.backup.model

import kotlinx.serialization.Serializable

@Serializable
data class BackupMetadata(
    val backupId: String,
    val backupVersion: Int = 1,
    val appVersion: String,
    val schemaVersion: Int,
    val firebaseUid: String,
    val createdAt: String,
    val algorithm: String = "AES-256-GCM",
    val kdf: String = "PBKDF2WithHmacSHA256",
    val iterations: Int = 310000,
    val salt: String, // Base64 (for MasterKey derivation)
    val dbIv: String, // Base64 (for Database encryption)
    val keyIv: String, // Base64 (for CloudKey encryption)
    val encryptedCloudKey: String, // Base64 (CloudBackupKey encrypted with MasterKey)
    val dbFileName: String? = null, // Unique name of the database file on Drive
    val checksum: String, // SHA-256 of encrypted database file
    val metadataMac: String? = null // Base64 HMAC of the rest of the metadata
)
