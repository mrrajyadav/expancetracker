package com.baburamtechnologies.expensetracker.feature.settings.data.local.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.baburamtechnologies.expensetracker.core.database.model.ProfileEntity

@Entity(
    tableName = "payment_modes",
    foreignKeys = [
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["user_id"], name = "idx_payment_mode_user"),
        Index(value = ["user_id", "name"], unique = true)
    ]
)
data class PaymentModeEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "user_id")
    val userId: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "display_order")
    val displayOrder: Int = 0,

    @ColumnInfo(name = "is_system")
    val isSystem: Boolean = false,

    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,

    @ColumnInfo(name = "created_at")
    val createdAt: String,

    @ColumnInfo(name = "updated_at")
    val updatedAt: String
)
