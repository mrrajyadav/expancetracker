package com.baburamtechnologies.expensetracker.core.di

import com.baburamtechnologies.expensetracker.feature.auth.data.local.AuthLocalDataSource
import com.baburamtechnologies.expensetracker.feature.auth.data.local.AuthLocalDataSourceImpl
import com.baburamtechnologies.expensetracker.feature.auth.data.remote.AuthRemoteDataSource
import com.baburamtechnologies.expensetracker.feature.auth.data.remote.AuthRemoteDataSourceImpl
import com.baburamtechnologies.expensetracker.feature.auth.data.repository.AuthRepositoryImpl
import com.baburamtechnologies.expensetracker.feature.auth.domain.repository.AuthRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {

    @Binds
    @Singleton
    abstract fun bindAuthRemoteDataSource(
        impl: AuthRemoteDataSourceImpl
    ): AuthRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindAuthLocalDataSource(
        impl: AuthLocalDataSourceImpl
    ): AuthLocalDataSource

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ): AuthRepository
}
