package com.baburamtechnologies.expensetracker.feature.settings.domain.usecase

import com.baburamtechnologies.expensetracker.feature.settings.domain.model.PaymentMode
import com.baburamtechnologies.expensetracker.feature.settings.domain.repository.PaymentModeRepository
import javax.inject.Inject

class UpdatePaymentModeUseCase @Inject constructor(
    private val repository: PaymentModeRepository
) {
    suspend operator fun invoke(paymentMode: PaymentMode) = repository.updatePaymentMode(paymentMode)
}
