package com.baburamtechnologies.expensetracker.core.sync

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.baburamtechnologies.expensetracker.core.auth.CurrentUserProvider
import com.baburamtechnologies.expensetracker.core.backup.domain.BackupRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class BackupWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val backupRepository: BackupRepository,
    private val currentUserProvider: CurrentUserProvider
) : CoroutineWorker(appContext, workerParams) {

    private val tag = "BackupWorker"

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val userEmail = currentUserProvider.email() ?: run {
            Log.w(tag, "Backup skipped: No authenticated user email")
            return@withContext Result.failure()
        }
        val uid = currentUserProvider.userId() ?: run {
            Log.w(tag, "Backup skipped: No authenticated UID")
            return@withContext Result.failure()
        }

        Log.i(tag, "Starting automated backup for user: $userEmail")

        return@withContext try {
            val result = backupRepository.autoBackup(userEmail, uid)
            if (result.isSuccess) {
                Log.i(tag, "Automated backup completed successfully")
                Result.success()
            } else {
                val error = result.exceptionOrNull()
                if (error?.message == "Backup keys not set") {
                    Log.w(tag, "Backup skipped: Keys not yet configured by user")
                    Result.success() // Success because we can't do anything until user sets password
                } else {
                    Log.e(tag, "Automated backup failed: ${error?.message}")
                    Result.retry()
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Backup worker failed: ${e.message}", e)
            Result.retry()
        }
    }
}
