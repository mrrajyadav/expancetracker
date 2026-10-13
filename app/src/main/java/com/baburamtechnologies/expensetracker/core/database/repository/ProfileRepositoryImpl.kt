package com.baburamtechnologies.expensetracker.core.database.repository

import com.baburamtechnologies.expensetracker.core.database.ProfileLocalDataSource
import com.baburamtechnologies.expensetracker.core.database.model.ProfileEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(
    private val localDataSource: ProfileLocalDataSource
) : ProfileRepository {
    override fun getProfileById(id: String): Flow<ProfileEntity?> {
        return localDataSource.getProfileById(id)
    }

    override suspend fun syncProfile(profile: ProfileEntity): Result<Unit> {
        return runCatching {
            localDataSource.insertProfile(profile)
        }
    }
}
