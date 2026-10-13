package com.baburamtechnologies.expensetracker.feature.transactions.domain.repository

import com.baburamtechnologies.expensetracker.feature.transactions.domain.model.CategorySummary
import com.baburamtechnologies.expensetracker.feature.transactions.domain.model.Transaction
import com.baburamtechnologies.expensetracker.feature.transactions.domain.model.TransactionSummary
import com.baburamtechnologies.expensetracker.feature.transactions.domain.model.TransactionWithCategory
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun getAllTransactions(startDate: String, endDate: String): Flow<List<TransactionWithCategory>>
    fun getTransactionSummary(startDate: String, endDate: String): Flow<TransactionSummary>
    fun getCategoryBreakdown(startDate: String, endDate: String): Flow<List<CategorySummary>>
    fun getTransactionCount(): Flow<Int>
    suspend fun getTransactionById(id: String): TransactionWithCategory?
    suspend fun addTransaction(transaction: Transaction): Result<Unit>
    suspend fun updateTransaction(transaction: Transaction): Result<Unit>
    suspend fun deleteTransaction(id: String): Result<Unit>
    suspend fun syncTransactions(): Result<Unit>
}
