package com.baburamtechnologies.expensetracker.core.di

import com.baburamtechnologies.expensetracker.feature.settings.data.local.PaymentModeLocalDataSource
import com.baburamtechnologies.expensetracker.feature.settings.data.local.PaymentModeLocalDataSourceImpl
import com.baburamtechnologies.expensetracker.feature.settings.data.local.UserSettingsLocalDataSource
import com.baburamtechnologies.expensetracker.feature.settings.data.local.UserSettingsLocalDataSourceImpl
import com.baburamtechnologies.expensetracker.feature.settings.data.repository.PaymentModeRepositoryImpl
import com.baburamtechnologies.expensetracker.feature.settings.data.repository.UserSettingsRepositoryImpl
import com.baburamtechnologies.expensetracker.feature.settings.domain.repository.PaymentModeRepository
import com.baburamtechnologies.expensetracker.feature.settings.domain.repository.UserSettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SettingsModule {

    @Binds
    @Singleton
    abstract fun bindPaymentModeLocalDataSource(
        impl: PaymentModeLocalDataSourceImpl
    ): PaymentModeLocalDataSource

    @Binds
    @Singleton
    abstract fun bindUserSettingsLocalDataSource(
        impl: UserSettingsLocalDataSourceImpl
    ): UserSettingsLocalDataSource

    @Binds
    @Singleton
    abstract fun bindPaymentModeRepository(
        impl: PaymentModeRepositoryImpl
    ): PaymentModeRepository

    @Binds
    @Singleton
    abstract fun bindUserSettingsRepository(
        impl: UserSettingsRepositoryImpl
    ): UserSettingsRepository
}
