package com.baburamtechnologies.expensetracker.feature.transactions.domain.model

data class Transaction(
    val id: String,
    val userId: String,
    val amount: Double,
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
    val totalIncome: Double,
    val totalExpense: Double,
    val totalBalance: Double
)

data class CategorySummary(
    val categoryId: String,
    val categoryName: String,
    val totalAmount: Double
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
