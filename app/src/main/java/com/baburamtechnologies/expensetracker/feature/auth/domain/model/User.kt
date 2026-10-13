package com.baburamtechnologies.expensetracker.feature.auth.domain.model

data class User(
    val id: String,
    val email: String?,
    val phone: String? = null,
    val userMetadata: Map<String, Any> = emptyMap()
)
