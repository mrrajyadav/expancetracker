package com.baburamtechnologies.expensetracker.feature.transactions.data.local.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "categories",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["parent_id"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["parent_id"], name = "idx_category_parent"),
        Index(value = ["type", "level"], name = "idx_category_type_level")
    ]
)
data class CategoryEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "profile_id")
    val profileId: String? = null,

    @ColumnInfo(name = "parent_id")
    val parentId: String?,

    @ColumnInfo(name = "type")
    val type: String, // INCOME, EXPENSE

    @ColumnInfo(name = "level")
    val level: Int, // 1, 2, 3

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
