package com.baburamtechnologies.expensetracker.feature.settings.data.mapper

import com.baburamtechnologies.expensetracker.feature.settings.data.local.model.PaymentModeEntity
import com.baburamtechnologies.expensetracker.feature.settings.data.local.model.UserSettingsEntity
import com.baburamtechnologies.expensetracker.feature.settings.domain.model.PaymentMode
import com.baburamtechnologies.expensetracker.feature.settings.domain.model.UserSettings

fun PaymentModeEntity.toDomain() = PaymentMode(
    id = id,
    userId = userId,
    name = name,
    displayOrder = displayOrder,
    isSystem = isSystem,
    isActive = isActive,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun PaymentMode.toEntity() = PaymentModeEntity(
    id = id,
    userId = userId,
    name = name,
    displayOrder = displayOrder,
    isSystem = isSystem,
    isActive = isActive,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun UserSettingsEntity.toDomain() = UserSettings(
    userId = userId,
    currency = currency,
    theme = theme,
    language = language,
    dateFormat = dateFormat,
    financialYear = financialYear,
    startOfWeek = startOfWeek,
    updatedAt = updatedAt
)

fun UserSettings.toEntity() = UserSettingsEntity(
    userId = userId,
    currency = currency,
    theme = theme,
    language = language,
    dateFormat = dateFormat,
    financialYear = financialYear,
    startOfWeek = startOfWeek,
    updatedAt = updatedAt
)
