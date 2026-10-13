package com.baburamtechnologies.expensetracker.feature.profile.domain.usecase

import com.baburamtechnologies.expensetracker.core.database.repository.ProfileRepository
import com.baburamtechnologies.expensetracker.core.security.SessionManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class GetProfileUseCase @Inject constructor(
    private val repository: ProfileRepository,
    private val sessionManager: SessionManager
) {
    operator fun invoke() = flowOf(sessionManager.getCurrentUserId())
        .flatMapLatest { userId ->
            if (userId != null) repository.getProfileById(userId)
            else flowOf(null)
        }
}
