package com.baburamtechnologies.expensetracker.feature.settings.domain.usecase

import com.baburamtechnologies.expensetracker.feature.settings.domain.repository.UserSettingsRepository
import javax.inject.Inject

class GetUserSettingsUseCase @Inject constructor(
    private val repository: UserSettingsRepository
) {
    operator fun invoke() = repository.getUserSettings()
}
