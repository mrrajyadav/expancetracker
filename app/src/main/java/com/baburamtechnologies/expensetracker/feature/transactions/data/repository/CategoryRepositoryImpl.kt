package com.baburamtechnologies.expensetracker.feature.transactions.data.repository

import com.baburamtechnologies.expensetracker.core.database.MasterData
import com.baburamtechnologies.expensetracker.core.security.SessionManager
import com.baburamtechnologies.expensetracker.feature.transactions.data.local.CategoryLocalDataSource
import com.baburamtechnologies.expensetracker.feature.transactions.data.local.TransactionLocalDataSource
import com.baburamtechnologies.expensetracker.feature.transactions.data.mapper.toDomain
import com.baburamtechnologies.expensetracker.feature.transactions.data.mapper.toEntity
import com.baburamtechnologies.expensetracker.feature.transactions.domain.model.Category
import com.baburamtechnologies.expensetracker.feature.transactions.domain.model.TransactionType
import com.baburamtechnologies.expensetracker.feature.transactions.domain.repository.CategoryRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import java.time.Instant
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class CategoryRepositoryImpl @Inject constructor(
    private val localDataSource: CategoryLocalDataSource,
    private val transactionLocalDataSource: TransactionLocalDataSource,
    private val sessionManager: SessionManager
) : CategoryRepository {
    override fun getCategoriesByTypeAndLevel(type: TransactionType, level: Int, includeInactive: Boolean): Flow<List<Category>> {
        return sessionManager.userIdFlow.flatMapLatest { userId ->
            localDataSource.getCategoriesByTypeAndLevel(type.name, level, includeInactive, userId).map { entities ->
                entities.map { it.toDomain() }
            }
        }.onEach { categories ->
            if (categories.isEmpty() && level == 1) {
                // Seed EVERYTHING if level 1 is empty to ensure Level 2 is ready
                initializeDefaultCategories()
            }
        }
    }

    override fun getCategoriesByParent(parentId: String, includeInactive: Boolean): Flow<List<Category>> {
        return sessionManager.userIdFlow.flatMapLatest { userId ->
            localDataSource.getCategoriesByParent(parentId, includeInactive, userId).map { entities ->
                entities.map { it.toDomain() }
            }
        }.onEach { categories ->
            if (categories.isEmpty()) {
                // If Level 2 for a parent is empty, check if it's a system parent and seed
                val isSystemParent = MasterData.getSystemCategories().any { it.id == parentId }
                if (isSystemParent) {
                    initializeDefaultCategories()
                }
            }
        }
    }

    override suspend fun getCategoryById(id: String): Category? {
        val userId = sessionManager.getCurrentUserId()
        return localDataSource.getCategoryById(id, userId)?.toDomain()
    }

    override suspend fun addCategory(category: Category): Result<Unit> {
        return runCatching {
            val userId = sessionManager.getCurrentUserId()
            val now = Instant.now().toString()
            localDataSource.insertCategory(category.toEntity(createdAt = now, updatedAt = now, profileId = userId))
        }
    }

    override suspend fun updateCategory(category: Category): Result<Unit> {
        return runCatching {
            val userId = sessionManager.getCurrentUserId() ?: throw IllegalStateException("User session not found")
            val existing = localDataSource.getCategoryById(category.id, userId) 
                ?: throw IllegalArgumentException("Category not found or access denied")
            if (existing.isSystem) {
                throw IllegalArgumentException("Cannot modify system category")
            }
            val now = Instant.now().toString()
            localDataSource.updateCategory(category.toEntity(createdAt = existing.createdAt, updatedAt = now, profileId = userId))
        }
    }

    override suspend fun deleteCategory(id: String): Result<Unit> {
        return runCatching {
            val userId = sessionManager.getCurrentUserId() ?: throw IllegalStateException("User session not found")
            val category = localDataSource.getCategoryById(id, userId)
                ?: throw IllegalArgumentException("Category not found or access denied")
            if (category.isSystem) {
                throw IllegalArgumentException("Cannot delete system category")
            }
            val transactionCount = transactionLocalDataSource.getTransactionCountByCategory(userId, id)
            if (transactionCount > 0) {
                localDataSource.setCategoryActive(id, false, userId)
            } else {
                localDataSource.deleteCustomCategory(id, userId)
            }
        }
    }

    override suspend fun initializeDefaultCategories(): Result<Unit> {
        return runCatching {
            localDataSource.insertCategories(MasterData.getSystemCategories())
        }
    }
}
