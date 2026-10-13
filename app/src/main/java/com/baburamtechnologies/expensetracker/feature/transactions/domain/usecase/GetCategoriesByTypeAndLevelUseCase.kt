package com.baburamtechnologies.expensetracker.feature.transactions.domain.usecase

import com.baburamtechnologies.expensetracker.feature.transactions.domain.model.TransactionType
import com.baburamtechnologies.expensetracker.feature.transactions.domain.repository.CategoryRepository
import javax.inject.Inject

class GetCategoriesByTypeAndLevelUseCase @Inject constructor(
    private val repository: CategoryRepository
) {
    operator fun invoke(type: TransactionType, level: Int, includeInactive: Boolean = false) = 
        repository.getCategoriesByTypeAndLevel(type, level, includeInactive)
}
