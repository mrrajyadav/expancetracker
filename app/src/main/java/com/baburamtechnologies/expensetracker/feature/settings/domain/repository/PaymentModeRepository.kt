package com.baburamtechnologies.expensetracker.feature.settings.domain.repository

import com.baburamtechnologies.expensetracker.feature.settings.domain.model.PaymentMode
import kotlinx.coroutines.flow.Flow

interface PaymentModeRepository {
    fun getPaymentModes(): Flow<List<PaymentMode>>
    fun getActivePaymentModes(): Flow<List<PaymentMode>>
    suspend fun addCustomPaymentMode(name: String): Result<Unit>
    suspend fun updatePaymentMode(paymentMode: PaymentMode): Result<Unit>
    suspend fun deletePaymentMode(id: String): Result<Unit>
    suspend fun setPaymentModeActive(id: String, isActive: Boolean): Result<Unit>
    suspend fun getActiveTransactionCount(id: String): Int
    suspend fun seedDefaultPaymentModes(): Result<Unit>
}
