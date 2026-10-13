package com.baburamtechnologies.expensetracker.feature.settings.data.local

import com.baburamtechnologies.expensetracker.feature.settings.data.local.model.PaymentModeEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface PaymentModeLocalDataSource {
    fun getPaymentModes(userId: String): Flow<List<PaymentModeEntity>>
    fun getActivePaymentModes(userId: String): Flow<List<PaymentModeEntity>>
    fun getAllPaymentModes(userId: String): Flow<List<PaymentModeEntity>>
    suspend fun getActiveTransactionCount(id: String): Int
    suspend fun deletePaymentMode(id: String)
    suspend fun setPaymentModeActive(id: String, isActive: Boolean)
    suspend fun insertPaymentModes(paymentModes: List<PaymentModeEntity>)
    suspend fun updatePaymentMode(paymentMode: PaymentModeEntity)
    suspend fun getPaymentModeById(id: String): PaymentModeEntity?
}

class PaymentModeLocalDataSourceImpl @Inject constructor(
    private val paymentModeDao: PaymentModeDao
) : PaymentModeLocalDataSource {
    override fun getPaymentModes(userId: String) = paymentModeDao.getActivePaymentModes(userId)
    override fun getActivePaymentModes(userId: String) = paymentModeDao.getActivePaymentModes(userId)
    override fun getAllPaymentModes(userId: String) = paymentModeDao.getAllPaymentModes(userId)
    override suspend fun getActiveTransactionCount(id: String) = paymentModeDao.getActiveTransactionCountForPaymentMode(id)
    override suspend fun deletePaymentMode(id: String) = paymentModeDao.deletePaymentModeById(id)
    override suspend fun setPaymentModeActive(id: String, isActive: Boolean) = paymentModeDao.setPaymentModeActive(id, isActive)
    override suspend fun insertPaymentModes(paymentModes: List<PaymentModeEntity>) = paymentModeDao.insertPaymentModes(paymentModes)
    override suspend fun updatePaymentMode(paymentMode: PaymentModeEntity) = paymentModeDao.updatePaymentMode(paymentMode)
    override suspend fun getPaymentModeById(id: String) = paymentModeDao.getPaymentModeById(id)
}
