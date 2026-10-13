package com.baburamtechnologies.expensetracker.feature.transactions.data.local

import androidx.room.*
import com.baburamtechnologies.expensetracker.feature.transactions.data.local.model.TransactionEntity
import com.baburamtechnologies.expensetracker.feature.transactions.data.local.model.TransactionWithCategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Transaction
    @Query("SELECT * FROM transactions WHERE user_id = :userId AND is_deleted = 0 AND transaction_date BETWEEN :startDate AND :endDate ORDER BY transaction_date DESC")
    fun getTransactionsByUserAndDate(userId: String, startDate: String, endDate: String): Flow<List<TransactionWithCategoryEntity>>

    @Query("""
        SELECT 
            SUM(CASE WHEN type = 'INCOME' THEN amount ELSE 0 END) as totalIncome,
            SUM(CASE WHEN type = 'EXPENSE' THEN amount ELSE 0 END) as totalExpense
        FROM transactions 
        WHERE user_id = :userId AND is_deleted = 0 AND transaction_date BETWEEN :startDate AND :endDate
    """)
    fun getTransactionSummary(userId: String, startDate: String, endDate: String): Flow<TransactionSummaryResult?>

    @Query("""
        SELECT 
            t.category_level1_id as categoryId,
            c.name as categoryName,
            SUM(t.amount) as totalAmount
        FROM transactions t
        JOIN categories c ON t.category_level1_id = c.id
        WHERE t.user_id = :userId AND t.is_deleted = 0 AND t.type = 'EXPENSE' AND t.transaction_date BETWEEN :startDate AND :endDate
        GROUP BY t.category_level1_id
        ORDER BY totalAmount DESC
    """)
    fun getCategoryBreakdown(userId: String, startDate: String, endDate: String): Flow<List<CategorySummaryResult>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>)

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: String): TransactionEntity?

    @Transaction
    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionWithCategoryById(id: String): TransactionWithCategoryEntity?

    @Query("UPDATE transactions SET is_deleted = 1, updated_at = :updatedAt WHERE id = :id")
    suspend fun softDeleteTransaction(id: String, updatedAt: String)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun hardDeleteTransaction(id: String)

    @Query("SELECT COUNT(*) FROM transactions WHERE user_id = :userId AND (category_level1_id = :categoryId OR category_level2_id = :categoryId OR category_level3_id = :categoryId) AND is_deleted = 0")
    suspend fun getTransactionCountByCategory(userId: String, categoryId: String): Int
}

data class TransactionSummaryResult(
    val totalIncome: Double?,
    val totalExpense: Double?
)

data class CategorySummaryResult(
    val categoryId: String,
    val categoryName: String,
    val totalAmount: Double
)
