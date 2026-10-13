package com.baburamtechnologies.expensetracker.feature.transactions.data.mapper

import com.baburamtechnologies.expensetracker.feature.transactions.data.local.CategorySummaryResult
import com.baburamtechnologies.expensetracker.feature.transactions.data.local.TransactionSummaryResult
import com.baburamtechnologies.expensetracker.feature.transactions.data.local.model.CategoryEntity
import com.baburamtechnologies.expensetracker.feature.transactions.data.local.model.TransactionEntity
import com.baburamtechnologies.expensetracker.feature.transactions.data.local.model.TransactionWithCategoryEntity
import com.baburamtechnologies.expensetracker.feature.transactions.domain.model.*

fun TransactionEntity.toDomain(): Transaction {
    return Transaction(
        id = id,
        userId = userId,
        amount = amount,
        type = TransactionType.valueOf(type),
        categoryLevel1Id = categoryLevel1Id,
        categoryLevel2Id = categoryLevel2Id,
        categoryLevel3Id = categoryLevel3Id,
        paymentModeId = paymentModeId,
        note = note,
        date = transactionDate,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun Transaction.toEntity(): TransactionEntity {
    return TransactionEntity(
        id = id,
        userId = userId,
        amount = amount,
        type = type.name,
        categoryLevel1Id = categoryLevel1Id,
        categoryLevel2Id = categoryLevel2Id,
        categoryLevel3Id = categoryLevel3Id,
        paymentModeId = paymentModeId,
        note = note,
        transactionDate = date,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isDeleted = false
    )
}

fun TransactionWithCategoryEntity.toDomain(): TransactionWithCategory {
    return TransactionWithCategory(
        transaction = transaction.toDomain(),
        categoryLevel1 = categoryLevel1.toDomain(),
        categoryLevel2 = categoryLevel2.toDomain(),
        categoryLevel3 = categoryLevel3?.toDomain(),
        paymentMode = paymentMode?.let {
            com.baburamtechnologies.expensetracker.feature.settings.domain.model.PaymentMode(
                id = it.id,
                userId = it.userId,
                name = it.name,
                displayOrder = it.displayOrder,
                isSystem = it.isSystem,
                isActive = it.isActive,
                createdAt = it.createdAt,
                updatedAt = it.updatedAt
            )
        }
    )
}

fun CategoryEntity.toDomain() = Category(
    id = id,
    parentId = parentId,
    type = TransactionType.valueOf(type),
    level = level,
    name = name,
    displayOrder = displayOrder,
    isSystem = isSystem,
    isActive = isActive
)

fun Category.toEntity(createdAt: String, updatedAt: String, profileId: String? = null) = CategoryEntity(
    id = id,
    profileId = profileId,
    parentId = parentId,
    type = type.name,
    level = level,
    name = name,
    displayOrder = displayOrder,
    isSystem = isSystem,
    isActive = isActive,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun TransactionSummaryResult.toDomain(): TransactionSummary {
    val income = totalIncome ?: 0L
    val expense = totalExpense ?: 0L
    return TransactionSummary(
        totalIncome = income,
        totalExpense = expense,
        totalBalance = income - expense
    )
}

fun CategorySummaryResult.toDomain(): CategorySummary {
    return CategorySummary(
        categoryId = categoryId,
        categoryName = categoryName,
        totalAmount = totalAmount
    )
}
