package com.baburamtechnologies.expensetracker.feature.auth.domain.usecase

import com.baburamtechnologies.expensetracker.feature.auth.domain.repository.AuthRepository
import com.google.firebase.auth.AuthCredential
import javax.inject.Inject

class LinkCredentialUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(credential: AuthCredential) = repository.linkCredential(credential)
}
