package com.baburamtechnologies.expensetracker.feature.auth.domain.usecase

import com.baburamtechnologies.expensetracker.feature.auth.domain.repository.AuthRepository
import javax.inject.Inject

class GetCurrentUserUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke() = repository.getCurrentUser()
}
