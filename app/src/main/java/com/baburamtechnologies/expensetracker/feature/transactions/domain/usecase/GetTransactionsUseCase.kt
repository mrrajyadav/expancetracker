package com.baburamtechnologies.expensetracker.feature.transactions.domain.usecase

import com.baburamtechnologies.expensetracker.feature.transactions.domain.model.TransactionWithCategory
import com.baburamtechnologies.expensetracker.feature.transactions.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTransactionsUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    operator fun invoke(startDate: String, endDate: String): Flow<List<TransactionWithCategory>> = 
        repository.getAllTransactions(startDate, endDate)
}
