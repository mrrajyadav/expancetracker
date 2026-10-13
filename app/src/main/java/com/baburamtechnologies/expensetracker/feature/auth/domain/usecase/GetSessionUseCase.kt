package com.baburamtechnologies.expensetracker.feature.auth.domain.usecase

import com.baburamtechnologies.expensetracker.feature.auth.domain.repository.AuthRepository
import javax.inject.Inject

class GetSessionUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    operator fun invoke() = repository.getSessionFlow()
}
