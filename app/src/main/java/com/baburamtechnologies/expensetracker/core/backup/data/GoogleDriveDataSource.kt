package com.baburamtechnologies.expensetracker.core.backup.data

import android.content.Context
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.FileContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleDriveDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var driveService: Drive? = null

    fun setupService(accountEmail: String) {
        if (accountEmail.isBlank()) {
            throw IllegalArgumentException("Account email cannot be blank")
        }
        
        val credential = GoogleAccountCredential.usingOAuth2(
            context, listOf(DriveScopes.DRIVE_APPDATA)
        )
        
        // Ensure the email is passed to the library correctly.
        // We set both properties to be safe.
        credential.selectedAccountName = accountEmail
        try {
            credential.selectedAccount = android.accounts.Account(accountEmail, "com.google")
        } catch (e: Exception) {
            // Ignore if Account object creation fails, fallback to name
        }

        driveService = Drive.Builder(
            NetHttpTransport(),
            GsonFactory(),
            credential
        ).setApplicationName("Expense Tracker")
            .build()
    }

    suspend fun uploadFile(localFile: File, driveFileName: String, mimeType: String): String? = withContext(Dispatchers.IO) {
        val service = driveService ?: throw IllegalStateException("Drive service not initialized")
        if (driveFileName.isBlank()) throw IllegalArgumentException("Drive file name cannot be blank")

        val mediaContent = FileContent(mimeType, localFile)
        val existingFileId = getFileIdByName(driveFileName)

        return@withContext if (existingFileId != null) {
            // For updates, we only need to provide content. 
            // Providing 'parents' in metadata here is known to cause errors.
            service.files().update(existingFileId, null, mediaContent).execute().id
        } else {
            val fileMetadata = com.google.api.services.drive.model.File().apply {
                name = driveFileName
                parents = listOf("appDataFolder")
            }
            service.files().create(fileMetadata, mediaContent).setFields("id").execute().id
        }
    }

    suspend fun downloadFile(driveFileId: String, localFile: File) = withContext(Dispatchers.IO) {
        val service = driveService ?: return@withContext
        localFile.outputStream().use { outputStream ->
            service.files().get(driveFileId).executeMediaAndDownloadTo(outputStream)
        }
    }

    suspend fun getFileIdByName(fileName: String): String? = withContext(Dispatchers.IO) {
        val service = driveService ?: return@withContext null
        val result = service.files().list()
            .setSpaces("appDataFolder")
            .setQ("name = '$fileName' and trashed = false")
            .setOrderBy("modifiedTime desc")
            .setFields("files(id, modifiedTime)")
            .execute()

        result.files.firstOrNull()?.id
    }

    suspend fun deleteFile(driveFileId: String) = withContext(Dispatchers.IO) {
        val service = driveService ?: return@withContext
        service.files().delete(driveFileId).execute()
    }

    suspend fun listBackupFiles(): List<com.google.api.services.drive.model.File> = withContext(Dispatchers.IO) {
        val service = driveService ?: return@withContext emptyList()
        val result = service.files().list()
            .setSpaces("appDataFolder")
            .setQ("trashed = false")
            .setFields("files(id, name, createdTime, size)")
            .execute()
        result.files ?: emptyList()
    }
}
