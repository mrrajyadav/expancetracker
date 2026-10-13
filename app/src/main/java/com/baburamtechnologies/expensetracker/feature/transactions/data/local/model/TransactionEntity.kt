package com.baburamtechnologies.expensetracker.feature.transactions.data.local.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.baburamtechnologies.expensetracker.core.database.model.ProfileEntity
import com.baburamtechnologies.expensetracker.feature.settings.data.local.model.PaymentModeEntity
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["category_level1_id"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["category_level2_id"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["category_level3_id"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = PaymentModeEntity::class,
            parentColumns = ["id"],
            childColumns = ["payment_mode_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["user_id"], name = "idx_transaction_user"),
        Index(value = ["transaction_date"], name = "idx_transaction_date"),
        Index(value = ["category_level1_id"], name = "idx_transaction_cat_l1"),
        Index(value = ["category_level2_id"], name = "idx_transaction_cat_l2"),
        Index(value = ["category_level3_id"], name = "idx_transaction_cat_l3"),
        Index(value = ["payment_mode_id"], name = "idx_transaction_payment_mode")
    ]
)
data class TransactionEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "user_id")
    val userId: String,

    @ColumnInfo(name = "amount")
    val amount: Long,

    @ColumnInfo(name = "type")
    val type: String, // INCOME, EXPENSE

    @ColumnInfo(name = "category_level1_id")
    val categoryLevel1Id: String,

    @ColumnInfo(name = "category_level2_id")
    val categoryLevel2Id: String,

    @ColumnInfo(name = "category_level3_id")
    val categoryLevel3Id: String?,

    @ColumnInfo(name = "payment_mode_id")
    val paymentModeId: String?,

    @ColumnInfo(name = "note")
    val note: String?,

    @ColumnInfo(name = "transaction_date")
    val transactionDate: String,

    @ColumnInfo(name = "created_at")
    val createdAt: String,

    @ColumnInfo(name = "updated_at")
    val updatedAt: String,

    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false
)
