package com.baburamtechnologies.expensetracker.feature.settings.data.local

import com.baburamtechnologies.expensetracker.feature.settings.data.local.model.UserSettingsEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface UserSettingsLocalDataSource {
    fun getUserSettings(userId: String): Flow<UserSettingsEntity?>
    suspend fun insertUserSettings(settings: UserSettingsEntity)
    suspend fun updateUserSettings(settings: UserSettingsEntity)
}

class UserSettingsLocalDataSourceImpl @Inject constructor(
    private val userSettingsDao: UserSettingsDao
) : UserSettingsLocalDataSource {
    override fun getUserSettings(userId: String) = userSettingsDao.getUserSettings(userId)
    override suspend fun insertUserSettings(settings: UserSettingsEntity) = userSettingsDao.insertUserSettings(settings)
    override suspend fun updateUserSettings(settings: UserSettingsEntity) = userSettingsDao.updateUserSettings(settings)
}
