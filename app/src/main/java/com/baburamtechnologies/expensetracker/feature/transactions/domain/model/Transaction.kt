package com.baburamtechnologies.expensetracker.feature.transactions.domain.model

data class Transaction(
    val id: String,
    val userId: String,
    val amount: Long,
    val type: TransactionType,
    val categoryLevel1Id: String,
    val categoryLevel2Id: String,
    val categoryLevel3Id: String?,
    val paymentModeId: String?,
    val note: String?,
    val date: String,
    val createdAt: String,
    val updatedAt: String
)

data class TransactionWithCategory(
    val transaction: Transaction,
    val categoryLevel1: Category,
    val categoryLevel2: Category,
    val categoryLevel3: Category?,
    val paymentMode: com.baburamtechnologies.expensetracker.feature.settings.domain.model.PaymentMode?
)

data class TransactionSummary(
    val totalIncome: Long,
    val totalExpense: Long,
    val totalBalance: Long
)

data class CategorySummary(
    val categoryId: String,
    val categoryName: String,
    val totalAmount: Long
)

enum class TransactionType {
    INCOME, EXPENSE
}

data class Category(
    val id: String,
    val parentId: String?,
    val type: TransactionType,
    val level: Int,
    val name: String,
    val displayOrder: Int,
    val isSystem: Boolean,
    val isActive: Boolean
)
