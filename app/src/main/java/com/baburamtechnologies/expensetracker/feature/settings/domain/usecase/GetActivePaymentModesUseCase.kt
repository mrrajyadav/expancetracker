package com.baburamtechnologies.expensetracker.feature.settings.domain.usecase

import com.baburamtechnologies.expensetracker.feature.settings.domain.repository.PaymentModeRepository
import javax.inject.Inject

class GetActivePaymentModesUseCase @Inject constructor(
    private val repository: PaymentModeRepository
) {
    operator fun invoke() = repository.getActivePaymentModes()
}
