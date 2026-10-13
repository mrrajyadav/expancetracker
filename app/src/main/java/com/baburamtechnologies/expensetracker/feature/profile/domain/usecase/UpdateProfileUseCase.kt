package com.baburamtechnologies.expensetracker.feature.profile.domain.usecase

import com.baburamtechnologies.expensetracker.core.database.model.ProfileEntity
import com.baburamtechnologies.expensetracker.core.database.repository.ProfileRepository
import javax.inject.Inject

class UpdateProfileUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(profile: ProfileEntity): Result<Unit> {
        return repository.syncProfile(profile)
    }
}
