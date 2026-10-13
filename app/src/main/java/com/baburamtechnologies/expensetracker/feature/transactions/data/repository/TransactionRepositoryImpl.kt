package com.baburamtechnologies.expensetracker.feature.transactions.data.repository

import com.baburamtechnologies.expensetracker.core.di.qualifier.IoDispatcher
import com.baburamtechnologies.expensetracker.core.security.SessionManager
import com.baburamtechnologies.expensetracker.feature.transactions.data.local.TransactionLocalDataSource
import com.baburamtechnologies.expensetracker.feature.transactions.data.mapper.toDomain
import com.baburamtechnologies.expensetracker.feature.transactions.data.mapper.toEntity
import com.baburamtechnologies.expensetracker.feature.transactions.domain.model.*
import com.baburamtechnologies.expensetracker.feature.transactions.domain.repository.CategoryRepository
import com.baburamtechnologies.expensetracker.feature.transactions.domain.repository.TransactionRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.Instant
import javax.inject.Inject

class TransactionRepositoryImpl @Inject constructor(
    private val localDataSource: TransactionLocalDataSource,
    private val categoryRepository: CategoryRepository,
    private val sessionManager: SessionManager,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : TransactionRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getAllTransactions(startDate: String, endDate: String): Flow<List<TransactionWithCategory>> {
        return sessionManager.userIdFlow.flatMapLatest { userId ->
            if (userId == null) flowOf(emptyList())
            else localDataSource.getTransactionsByUserAndDate(userId, startDate, endDate).map { entities ->
                entities.map { it.toDomain() }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getTransactionSummary(startDate: String, endDate: String): Flow<TransactionSummary> {
        return sessionManager.userIdFlow.flatMapLatest { userId ->
            if (userId == null) flowOf(TransactionSummary(0.0, 0.0, 0.0))
            else localDataSource.getTransactionSummary(userId, startDate, endDate).map { result ->
                result?.toDomain() ?: TransactionSummary(0.0, 0.0, 0.0)
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getCategoryBreakdown(startDate: String, endDate: String): Flow<List<CategorySummary>> {
        return sessionManager.userIdFlow.flatMapLatest { userId ->
            if (userId == null) flowOf(emptyList())
            else localDataSource.getCategoryBreakdown(userId, startDate, endDate).map { entities ->
                entities.map { it.toDomain() }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getTransactionCount(): Flow<Int> {
        return sessionManager.userIdFlow.flatMapLatest { userId ->
            if (userId == null) flowOf(0)
            else localDataSource.getTransactionsByUserAndDate(userId, "1970-01-01", "2100-12-31").map { it.size }
        }
    }

    override suspend fun getTransactionById(id: String): TransactionWithCategory? = withContext(ioDispatcher) {
        localDataSource.getTransactionWithCategoryById(id)?.toDomain()
    }

    override suspend fun addTransaction(transaction: Transaction): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            validateTransaction(transaction)
            localDataSource.insertTransaction(transaction.toEntity())
        }
    }

    override suspend fun updateTransaction(transaction: Transaction): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            validateTransaction(transaction)
            val existing = localDataSource.getTransactionById(transaction.id)
                ?: throw IllegalArgumentException("Transaction not found")
            
            val updated = transaction.toEntity().copy(
                createdAt = existing.createdAt
            )
            localDataSource.updateTransaction(updated)
        }
    }

    override suspend fun deleteTransaction(id: String): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            val now = Instant.now().toString()
            localDataSource.softDeleteTransaction(id, now)
        }
    }

    override suspend fun syncTransactions(): Result<Unit> = withContext(ioDispatcher) {
        // Syncing via Google Drive backup now
        Result.success(Unit)
    }

    private suspend fun validateTransaction(transaction: Transaction) {
        if (transaction.amount <= 0) {
            throw IllegalArgumentException("Amount must be greater than zero")
        }
        
        val l1 = categoryRepository.getCategoryById(transaction.categoryLevel1Id)
            ?: throw IllegalArgumentException("Level 1 category not found")
        val l2 = categoryRepository.getCategoryById(transaction.categoryLevel2Id)
            ?: throw IllegalArgumentException("Level 2 category not found")
        
        if (l2.parentId != l1.id) {
            throw IllegalArgumentException("Level 2 category must be a child of Level 1")
        }
        
        if (transaction.categoryLevel3Id != null) {
            val l3 = categoryRepository.getCategoryById(transaction.categoryLevel3Id)
                ?: throw IllegalArgumentException("Level 3 category not found")
            if (l3.parentId != l2.id) {
                throw IllegalArgumentException("Level 3 category must be a child of Level 2")
            }
        }

        if (sessionManager.getCurrentUserId() == null) {
            throw IllegalStateException("User session not found")
        }
    }
}
