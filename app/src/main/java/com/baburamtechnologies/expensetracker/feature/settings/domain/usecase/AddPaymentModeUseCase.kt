package com.baburamtechnologies.expensetracker.feature.settings.domain.usecase

import com.baburamtechnologies.expensetracker.feature.settings.domain.repository.PaymentModeRepository
import javax.inject.Inject

class AddPaymentModeUseCase @Inject constructor(
    private val repository: PaymentModeRepository
) {
    suspend operator fun invoke(name: String) = repository.addCustomPaymentMode(name)
}
