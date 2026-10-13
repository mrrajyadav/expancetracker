package com.baburamtechnologies.expensetracker.core.database

import com.baburamtechnologies.expensetracker.core.database.model.ProfileEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface ProfileLocalDataSource {
    fun getProfileById(id: String): Flow<ProfileEntity?>
    suspend fun insertProfile(profile: ProfileEntity)
}

class ProfileLocalDataSourceImpl @Inject constructor(
    private val profileDao: ProfileDao
) : ProfileLocalDataSource {
    override fun getProfileById(id: String) = profileDao.getProfileById(id)
    override suspend fun insertProfile(profile: ProfileEntity) = profileDao.insertProfile(profile)
}
