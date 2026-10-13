package com.baburamtechnologies.expensetracker.feature.settings.domain.model

data class UserSettings(
    val userId: String,
    val currency: String,
    val theme: String,
    val language: String,
    val dateFormat: String,
    val financialYear: String,
    val startOfWeek: String,
    val updatedAt: String
)
