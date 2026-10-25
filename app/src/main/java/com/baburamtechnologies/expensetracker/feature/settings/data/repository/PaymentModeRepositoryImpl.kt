package com.baburamtechnologies.expensetracker.feature.settings.data.repository

import com.baburamtechnologies.expensetracker.core.database.repository.ProfileRepository
import com.baburamtechnologies.expensetracker.core.security.SessionManager
import com.baburamtechnologies.expensetracker.feature.settings.data.local.PaymentModeLocalDataSource
import com.baburamtechnologies.expensetracker.feature.settings.data.mapper.toDomain
import com.baburamtechnologies.expensetracker.feature.settings.data.mapper.toEntity
import com.baburamtechnologies.expensetracker.feature.settings.domain.model.PaymentMode
import com.baburamtechnologies.expensetracker.feature.settings.domain.repository.PaymentModeRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

class PaymentModeRepositoryImpl @Inject constructor(
    private val localDataSource: PaymentModeLocalDataSource,
    private val sessionManager: SessionManager,
    private val profileRepository: ProfileRepository
) : PaymentModeRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getPaymentModes(): Flow<List<PaymentMode>> {
        return sessionManager.userIdFlow
            .flatMapLatest { userId ->
                if (userId != null) {
                    localDataSource.getAllPaymentModes(userId)
                        .map { entities -> entities.map { it.toDomain() } }
                        .onEach { if (it.isEmpty()) seedDefaultPaymentModes() }
                } else {
                    flowOf(emptyList())
                }
            }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getActivePaymentModes(): Flow<List<PaymentMode>> {
        return sessionManager.userIdFlow
            .flatMapLatest { userId ->
                if (userId != null) {
                    localDataSource.getActivePaymentModes(userId)
                        .map { entities -> entities.map { it.toDomain() } }
                } else {
                    flowOf(emptyList())
                }
            }
    }

    override suspend fun addCustomPaymentMode(name: String): Result<Unit> {
        return runCatching {
            val userId = sessionManager.getCurrentUserId() ?: throw IllegalStateException("Not logged in")
            val now = Instant.now().toString()
            val mode = PaymentMode(
                id = UUID.randomUUID().toString(),
                userId = userId,
                name = name,
                displayOrder = 0,
                isSystem = false,
                isActive = true,
                createdAt = now,
                updatedAt = now
            )
            localDataSource.insertPaymentModes(listOf(mode.toEntity()))
        }
    }

    override suspend fun updatePaymentMode(paymentMode: PaymentMode): Result<Unit> {
        return runCatching {
            val userId = sessionManager.getCurrentUserId() ?: throw IllegalStateException("Not logged in")
            if (paymentMode.userId != userId) throw IllegalStateException("Unauthorized")
            val now = Instant.now().toString()
            localDataSource.updatePaymentMode(paymentMode.toEntity().copy(
                updatedAt = now
            ))
        }
    }

    override suspend fun deletePaymentMode(id: String): Result<Unit> {
        return runCatching {
            val userId = sessionManager.getCurrentUserId() ?: throw IllegalStateException("Not logged in")
            val mode = localDataSource.getPaymentModeById(id, userId) ?: throw Exception("Payment mode not found")
            if (mode.isSystem) throw Exception("System payment modes cannot be deleted")
            
            val activeTransactionCount = localDataSource.getActiveTransactionCount(id, userId)
            if (activeTransactionCount > 0) {
                throw Exception("Cannot delete: $activeTransactionCount active transactions are linked to this mode. Archive it instead.")
            }
            
            localDataSource.deletePaymentMode(id, userId)
        }
    }

    override suspend fun setPaymentModeActive(id: String, isActive: Boolean): Result<Unit> {
        return runCatching {
            val userId = sessionManager.getCurrentUserId() ?: throw IllegalStateException("Not logged in")
            localDataSource.setPaymentModeActive(id, isActive, userId)
        }
    }

    override suspend fun getActiveTransactionCount(id: String): Int {
        val userId = sessionManager.getCurrentUserId() ?: return 0
        return localDataSource.getActiveTransactionCount(id, userId)
    }

    override suspend fun seedDefaultPaymentModes(): Result<Unit> {
        return runCatching {
            val userId = sessionManager.getCurrentUserId() ?: return@runCatching
            val now = Instant.now().toString()
            
            // Ensure Profile exists in DB to satisfy Foreign Key
            val profile = profileRepository.getProfileById(userId).first()
            if (profile == null) {
                // If profile is somehow missing (shouldn't happen with Google login sync),
                // we skip seeding and wait for profile to be present.
                return@runCatching
            }
            
            val defaultModes = listOf(
                createSystemMode(userId, "Cash", 0, now)
            )
            localDataSource.insertPaymentModes(defaultModes.map { it.toEntity() })
        }
    }

    private fun createSystemMode(userId: String, name: String, order: Int, now: String) = PaymentMode(
        id = "pm_${name.lowercase().replace(" ", "_")}_$userId",
        userId = userId,
        name = name,
        displayOrder = order,
        isSystem = true,
        isActive = true,
        createdAt = now,
        updatedAt = now
    )
}
