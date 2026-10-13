package com.baburamtechnologies.expensetracker.feature.settings.data.repository

import com.baburamtechnologies.expensetracker.core.security.SessionManager
import com.baburamtechnologies.expensetracker.feature.settings.data.local.UserSettingsLocalDataSource
import com.baburamtechnologies.expensetracker.feature.settings.data.mapper.toDomain
import com.baburamtechnologies.expensetracker.feature.settings.data.mapper.toEntity
import com.baburamtechnologies.expensetracker.feature.settings.domain.model.UserSettings
import com.baburamtechnologies.expensetracker.feature.settings.domain.repository.UserSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject

class UserSettingsRepositoryImpl @Inject constructor(
    private val localDataSource: UserSettingsLocalDataSource,
    private val sessionManager: SessionManager
) : UserSettingsRepository {

    override fun getUserSettings(): Flow<UserSettings?> {
        val userId = sessionManager.getCurrentUserId() ?: ""
        return localDataSource.getUserSettings(userId).map { it?.toDomain() }
    }

    override suspend fun updateUserSettings(settings: UserSettings): Result<Unit> {
        return runCatching {
            val now = Instant.now().toString()
            localDataSource.updateUserSettings(settings.toEntity().copy(
                updatedAt = now
            ))
        }
    }

    override suspend fun initializeDefaultSettings(): Result<Unit> {
        return runCatching {
            val userId = sessionManager.getCurrentUserId() ?: throw IllegalStateException("Not logged in")
            val now = Instant.now().toString()
            val defaults = UserSettings(
                userId = userId,
                currency = "INR",
                theme = "SYSTEM",
                language = "en",
                dateFormat = "dd-MM-yyyy",
                financialYear = "APR_MAR",
                startOfWeek = "MONDAY",
                updatedAt = now
            )
            localDataSource.insertUserSettings(defaults.toEntity())
        }
    }
}
