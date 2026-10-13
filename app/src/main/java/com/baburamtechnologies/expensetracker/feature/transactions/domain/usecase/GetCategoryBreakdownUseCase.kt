package com.baburamtechnologies.expensetracker.feature.transactions.domain.usecase

import com.baburamtechnologies.expensetracker.feature.transactions.domain.repository.TransactionRepository
import javax.inject.Inject

class GetCategoryBreakdownUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    operator fun invoke(startDate: String, endDate: String) = repository.getCategoryBreakdown(startDate, endDate)
}
