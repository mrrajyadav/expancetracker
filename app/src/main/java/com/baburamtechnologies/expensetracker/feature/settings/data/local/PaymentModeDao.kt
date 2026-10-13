package com.baburamtechnologies.expensetracker.feature.settings.data.local

import androidx.room.*
import com.baburamtechnologies.expensetracker.feature.settings.data.local.model.PaymentModeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentModeDao {
    @Query("SELECT * FROM payment_modes WHERE user_id = :userId ORDER BY display_order ASC, name ASC")
    fun getAllPaymentModes(userId: String): Flow<List<PaymentModeEntity>>

    @Query("SELECT * FROM payment_modes WHERE user_id = :userId AND is_active = 1 ORDER BY display_order ASC, name ASC")
    fun getActivePaymentModes(userId: String): Flow<List<PaymentModeEntity>>

    @Query("SELECT COUNT(*) FROM transactions WHERE payment_mode_id = :paymentModeId AND is_deleted = 0")
    suspend fun getActiveTransactionCountForPaymentMode(paymentModeId: String): Int

    @Query("DELETE FROM payment_modes WHERE id = :id AND is_system = 0")
    suspend fun deletePaymentModeById(id: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaymentModes(paymentModes: List<PaymentModeEntity>)

    @Update
    suspend fun updatePaymentMode(paymentMode: PaymentModeEntity)

    @Query("SELECT * FROM payment_modes WHERE id = :id")
    suspend fun getPaymentModeById(id: String): PaymentModeEntity?

    @Query("UPDATE payment_modes SET is_active = :isActive WHERE id = :id")
    suspend fun setPaymentModeActive(id: String, isActive: Boolean)
}
