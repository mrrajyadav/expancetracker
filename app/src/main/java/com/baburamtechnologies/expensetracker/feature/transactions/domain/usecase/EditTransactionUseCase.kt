package com.baburamtechnologies.expensetracker.feature.transactions.domain.usecase

import com.baburamtechnologies.expensetracker.feature.transactions.domain.model.Transaction
import com.baburamtechnologies.expensetracker.feature.transactions.domain.repository.TransactionRepository
import javax.inject.Inject

class EditTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(transaction: Transaction) = repository.updateTransaction(transaction)
}
