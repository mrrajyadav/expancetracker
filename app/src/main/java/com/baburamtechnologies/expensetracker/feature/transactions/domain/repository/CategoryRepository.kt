package com.baburamtechnologies.expensetracker.feature.transactions.domain.repository

import com.baburamtechnologies.expensetracker.feature.transactions.domain.model.Category
import com.baburamtechnologies.expensetracker.feature.transactions.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun getCategoriesByTypeAndLevel(type: TransactionType, level: Int, includeInactive: Boolean = false): Flow<List<Category>>
    fun getCategoriesByParent(parentId: String, includeInactive: Boolean = false): Flow<List<Category>>
    suspend fun getCategoryById(id: String): Category?
    suspend fun addCategory(category: Category): Result<Unit>
    suspend fun updateCategory(category: Category): Result<Unit>
    suspend fun deleteCategory(id: String): Result<Unit>
    suspend fun initializeDefaultCategories(): Result<Unit>
}
