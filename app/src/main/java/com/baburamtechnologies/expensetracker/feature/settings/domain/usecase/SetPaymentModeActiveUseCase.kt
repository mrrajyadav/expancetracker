package com.baburamtechnologies.expensetracker.feature.settings.domain.usecase

import com.baburamtechnologies.expensetracker.feature.settings.domain.repository.PaymentModeRepository
import javax.inject.Inject

class SetPaymentModeActiveUseCase @Inject constructor(
    private val repository: PaymentModeRepository
) {
    suspend operator fun invoke(id: String, isActive: Boolean) = repository.setPaymentModeActive(id, isActive)
}
