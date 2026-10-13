package com.baburamtechnologies.expensetracker.feature.settings.data.local.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.baburamtechnologies.expensetracker.core.database.model.ProfileEntity
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "user_settings",
    foreignKeys = [
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class UserSettingsEntity(
    @PrimaryKey
    @ColumnInfo(name = "user_id")
    val userId: String,

    @ColumnInfo(name = "currency")
    val currency: String = "INR",

    @ColumnInfo(name = "theme")
    val theme: String = "SYSTEM",

    @ColumnInfo(name = "language")
    val language: String = "en",

    @ColumnInfo(name = "date_format")
    val dateFormat: String = "dd-MM-yyyy",

    @ColumnInfo(name = "financial_year")
    val financialYear: String = "APR_MAR",

    @ColumnInfo(name = "start_of_week")
    val startOfWeek: String = "MONDAY",

    @ColumnInfo(name = "updated_at")
    val updatedAt: String
)
