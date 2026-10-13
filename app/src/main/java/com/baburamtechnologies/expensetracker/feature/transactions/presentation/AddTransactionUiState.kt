package com.baburamtechnologies.expensetracker.feature.transactions.presentation

import com.baburamtechnologies.expensetracker.feature.settings.domain.model.PaymentMode
import com.baburamtechnologies.expensetracker.feature.transactions.domain.model.Category
import com.baburamtechnologies.expensetracker.feature.transactions.domain.model.TransactionType

data class AddTransactionUiState(
    val amount: String = "",
    val type: TransactionType = TransactionType.EXPENSE,
    val selectedLevel1Category: Category? = null,
    val selectedLevel2Category: Category? = null,
    val selectedLevel3Category: Category? = null,
    val selectedPaymentMode: PaymentMode? = null,
    val note: String = "",
    val dateMillis: Long = System.currentTimeMillis(),
    val showDatePicker: Boolean = false,
    val level1Categories: List<Category> = emptyList(),
    val level2Categories: List<Category> = emptyList(),
    val level3Categories: List<Category> = emptyList(),
    val paymentModes: List<PaymentMode> = emptyList(),
    val isConfirmationDialogOpen: Boolean = false,
    val showInvalidDateDialog: Boolean = false,
    val isLoading: Boolean = false,
    val isFormValid: Boolean = false,
    val isEditMode: Boolean = false,
    val originalCreatedAt: String? = null,
    val fieldErrors: Map<String, String?> = emptyMap(),
    val errorMessage: String? = null
) {
    companion object {
        const val NOTE_MAX_LENGTH = 200
        
        // Field keys for mapping errors
        const val FIELD_AMOUNT = "amount"
        const val FIELD_CATEGORY_L1 = "category_l1"
        const val FIELD_CATEGORY_L2 = "category_l2"
        const val FIELD_PAYMENT_MODE = "payment_mode"
    }
}
