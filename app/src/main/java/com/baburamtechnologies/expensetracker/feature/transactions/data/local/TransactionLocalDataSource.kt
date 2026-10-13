package com.baburamtechnologies.expensetracker.feature.transactions.data.local

import com.baburamtechnologies.expensetracker.feature.transactions.data.local.model.TransactionEntity
import com.baburamtechnologies.expensetracker.feature.transactions.data.local.model.TransactionWithCategoryEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface TransactionLocalDataSource {
    fun getTransactionsByUserAndDate(userId: String, startDate: String, endDate: String): Flow<List<TransactionWithCategoryEntity>>
    fun getTransactionSummary(userId: String, startDate: String, endDate: String): Flow<TransactionSummaryResult?>
    fun getCategoryBreakdown(userId: String, startDate: String, endDate: String): Flow<List<CategorySummaryResult>>
    suspend fun insertTransaction(transaction: TransactionEntity)
    suspend fun updateTransaction(transaction: TransactionEntity)
    suspend fun getTransactionById(id: String): TransactionEntity?
    suspend fun getTransactionWithCategoryById(id: String): TransactionWithCategoryEntity?
    suspend fun softDeleteTransaction(id: String, updatedAt: String)
    suspend fun deleteTransaction(id: String)
    suspend fun getTransactionCountByCategory(userId: String, categoryId: String): Int
}

class TransactionLocalDataSourceImpl @Inject constructor(
    private val transactionDao: TransactionDao
) : TransactionLocalDataSource {

    override fun getTransactionsByUserAndDate(userId: String, startDate: String, endDate: String): Flow<List<TransactionWithCategoryEntity>> {
        return transactionDao.getTransactionsByUserAndDate(userId, startDate, endDate)
    }

    override fun getTransactionSummary(userId: String, startDate: String, endDate: String): Flow<TransactionSummaryResult?> {
        return transactionDao.getTransactionSummary(userId, startDate, endDate)
    }

    override fun getCategoryBreakdown(userId: String, startDate: String, endDate: String): Flow<List<CategorySummaryResult>> {
        return transactionDao.getCategoryBreakdown(userId, startDate, endDate)
    }

    override suspend fun insertTransaction(transaction: TransactionEntity) {
        transactionDao.insertTransaction(transaction)
    }

    override suspend fun updateTransaction(transaction: TransactionEntity) {
        transactionDao.updateTransaction(transaction)
    }

    override suspend fun getTransactionById(id: String): TransactionEntity? {
        return transactionDao.getTransactionById(id)
    }

    override suspend fun getTransactionWithCategoryById(id: String): TransactionWithCategoryEntity? {
        return transactionDao.getTransactionWithCategoryById(id)
    }

    override suspend fun softDeleteTransaction(id: String, updatedAt: String) {
        transactionDao.softDeleteTransaction(id, updatedAt)
    }

    override suspend fun deleteTransaction(id: String) {
        transactionDao.hardDeleteTransaction(id)
    }

    override suspend fun getTransactionCountByCategory(userId: String, categoryId: String): Int {
        return transactionDao.getTransactionCountByCategory(userId, categoryId)
    }
}
