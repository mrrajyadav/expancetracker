package com.baburamtechnologies.expensetracker.feature.transactions.data.local.model

import androidx.room.Embedded
import androidx.room.Relation
import com.baburamtechnologies.expensetracker.feature.settings.data.local.model.PaymentModeEntity

data class TransactionWithCategoryEntity(
    @Embedded val transaction: TransactionEntity,
    
    @Relation(
        parentColumn = "category_level1_id",
        entityColumn = "id"
    )
    val categoryLevel1: CategoryEntity,
    
    @Relation(
        parentColumn = "category_level2_id",
        entityColumn = "id"
    )
    val categoryLevel2: CategoryEntity,
    
    @Relation(
        parentColumn = "category_level3_id",
        entityColumn = "id"
    )
    val categoryLevel3: CategoryEntity?,

    @Relation(
        parentColumn = "payment_mode_id",
        entityColumn = "id"
    )
    val paymentMode: PaymentModeEntity?
)
