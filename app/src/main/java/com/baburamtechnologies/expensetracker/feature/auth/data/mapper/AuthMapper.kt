package com.baburamtechnologies.expensetracker.feature.auth.data.mapper

import com.baburamtechnologies.expensetracker.feature.auth.domain.model.User
import com.google.firebase.auth.FirebaseUser

fun FirebaseUser.toDomainUser(): User = User(
    id = uid,
    email = email,
    phone = phoneNumber,
    userMetadata = emptyMap()
)
