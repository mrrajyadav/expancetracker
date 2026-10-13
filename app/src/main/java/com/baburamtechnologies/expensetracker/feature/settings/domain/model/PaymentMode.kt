package com.baburamtechnologies.expensetracker.feature.settings.domain.model

data class PaymentMode(
    val id: String,
    val userId: String,
    val name: String,
    val displayOrder: Int,
    val isSystem: Boolean,
    val isActive: Boolean,
    val createdAt: String,
    val updatedAt: String
)
