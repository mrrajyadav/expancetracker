package com.baburamtechnologies.expensetracker.feature.transactions.domain.usecase

import com.baburamtechnologies.expensetracker.feature.transactions.domain.repository.CategoryRepository
import javax.inject.Inject

class DeleteCategoryUseCase @Inject constructor(
    private val repository: CategoryRepository
) {
    suspend operator fun invoke(id: String) = repository.deleteCategory(id)
}
