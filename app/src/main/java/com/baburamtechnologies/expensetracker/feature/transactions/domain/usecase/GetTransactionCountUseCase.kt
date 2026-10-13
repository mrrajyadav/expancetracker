package com.baburamtechnologies.expensetracker.feature.transactions.domain.usecase

import com.baburamtechnologies.expensetracker.feature.transactions.domain.repository.TransactionRepository
import javax.inject.Inject

class GetTransactionCountUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    operator fun invoke() = repository.getTransactionCount()
}
