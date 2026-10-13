package com.baburamtechnologies.expensetracker.feature.transactions.domain.usecase

import com.baburamtechnologies.expensetracker.feature.transactions.domain.repository.CategoryRepository
import javax.inject.Inject

class GetCategoriesByParentUseCase @Inject constructor(
    private val repository: CategoryRepository
) {
    operator fun invoke(parentId: String, includeInactive: Boolean = false) = 
        repository.getCategoriesByParent(parentId, includeInactive)
}
