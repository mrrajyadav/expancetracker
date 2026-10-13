package com.baburamtechnologies.expensetracker.core.backup.model

import com.baburamtechnologies.expensetracker.core.database.model.ProfileEntity
import com.baburamtechnologies.expensetracker.feature.settings.data.local.model.PaymentModeEntity
import com.baburamtechnologies.expensetracker.feature.settings.data.local.model.UserSettingsEntity
import com.baburamtechnologies.expensetracker.feature.transactions.data.local.model.CategoryEntity
import com.baburamtechnologies.expensetracker.feature.transactions.data.local.model.TransactionEntity
import kotlinx.serialization.Serializable

@Serializable
data class UserBackupPayload(
    val firebaseUid: String,
    val profile: ProfileEntity? = null,
    val transactions: List<TransactionEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val paymentModes: List<PaymentModeEntity> = emptyList(),
    val userSettings: UserSettingsEntity? = null
)
