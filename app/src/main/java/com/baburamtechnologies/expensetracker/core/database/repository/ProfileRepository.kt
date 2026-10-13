package com.baburamtechnologies.expensetracker.core.database.repository

import com.baburamtechnologies.expensetracker.core.database.model.ProfileEntity
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    fun getProfileById(id: String): Flow<ProfileEntity?>
    suspend fun syncProfile(profile: ProfileEntity): Result<Unit>
}
