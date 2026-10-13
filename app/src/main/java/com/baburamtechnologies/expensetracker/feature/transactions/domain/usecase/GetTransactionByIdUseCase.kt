package com.baburamtechnologies.expensetracker.feature.transactions.domain.usecase

import com.baburamtechnologies.expensetracker.feature.transactions.domain.repository.TransactionRepository
import javax.inject.Inject

class GetTransactionByIdUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(id: String) = repository.getTransactionById(id)
}
