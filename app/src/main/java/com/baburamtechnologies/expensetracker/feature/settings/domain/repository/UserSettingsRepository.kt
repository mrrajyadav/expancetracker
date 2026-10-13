package com.baburamtechnologies.expensetracker.feature.settings.domain.repository

import com.baburamtechnologies.expensetracker.feature.settings.domain.model.UserSettings
import kotlinx.coroutines.flow.Flow

interface UserSettingsRepository {
    fun getUserSettings(): Flow<UserSettings?>
    suspend fun updateUserSettings(settings: UserSettings): Result<Unit>
    suspend fun initializeDefaultSettings(): Result<Unit>
}
