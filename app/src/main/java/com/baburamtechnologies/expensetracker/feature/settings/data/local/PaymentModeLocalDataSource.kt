package com.baburamtechnologies.expensetracker.feature.settings.data.local

import com.baburamtechnologies.expensetracker.feature.settings.data.local.model.PaymentModeEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface PaymentModeLocalDataSource {
    fun getPaymentModes(userId: String): Flow<List<PaymentModeEntity>>
    fun getActivePaymentModes(userId: String): Flow<List<PaymentModeEntity>>
    fun getAllPaymentModes(userId: String): Flow<List<PaymentModeEntity>>
    suspend fun getActiveTransactionCount(id: String, userId: String): Int
    suspend fun deletePaymentMode(id: String, userId: String)
    suspend fun setPaymentModeActive(id: String, isActive: Boolean, userId: String)
    suspend fun insertPaymentModes(paymentModes: List<PaymentModeEntity>)
    suspend fun updatePaymentMode(paymentMode: PaymentModeEntity)
    suspend fun getPaymentModeById(id: String, userId: String): PaymentModeEntity?
}

class PaymentModeLocalDataSourceImpl @Inject constructor(
    private val paymentModeDao: PaymentModeDao
) : PaymentModeLocalDataSource {
    override fun getPaymentModes(userId: String) = paymentModeDao.getActivePaymentModes(userId)
    override fun getActivePaymentModes(userId: String) = paymentModeDao.getActivePaymentModes(userId)
    override fun getAllPaymentModes(userId: String) = paymentModeDao.getAllPaymentModes(userId)
    override suspend fun getActiveTransactionCount(id: String, userId: String) = paymentModeDao.getActiveTransactionCountForPaymentMode(id, userId)
    override suspend fun deletePaymentMode(id: String, userId: String) = paymentModeDao.deletePaymentModeById(id, userId)
    override suspend fun setPaymentModeActive(id: String, isActive: Boolean, userId: String) = paymentModeDao.setPaymentModeActive(id, isActive, userId)
    override suspend fun insertPaymentModes(paymentModes: List<PaymentModeEntity>) = paymentModeDao.insertPaymentModes(paymentModes)
    override suspend fun updatePaymentMode(paymentMode: PaymentModeEntity) = paymentModeDao.updatePaymentMode(
        id = paymentMode.id,
        userId = paymentMode.userId,
        name = paymentMode.name,
        displayOrder = paymentMode.displayOrder,
        isSystem = paymentMode.isSystem,
        isActive = paymentMode.isActive,
        updatedAt = paymentMode.updatedAt
    )
    override suspend fun getPaymentModeById(id: String, userId: String) = paymentModeDao.getPaymentModeById(id, userId)
}
