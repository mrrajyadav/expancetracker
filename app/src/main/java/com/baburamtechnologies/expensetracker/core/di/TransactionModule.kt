package com.baburamtechnologies.expensetracker.core.di

import com.baburamtechnologies.expensetracker.feature.transactions.data.local.CategoryLocalDataSource
import com.baburamtechnologies.expensetracker.feature.transactions.data.local.CategoryLocalDataSourceImpl
import com.baburamtechnologies.expensetracker.feature.transactions.data.local.TransactionLocalDataSource
import com.baburamtechnologies.expensetracker.feature.transactions.data.local.TransactionLocalDataSourceImpl
import com.baburamtechnologies.expensetracker.feature.transactions.data.repository.CategoryRepositoryImpl
import com.baburamtechnologies.expensetracker.feature.transactions.data.repository.TransactionRepositoryImpl
import com.baburamtechnologies.expensetracker.feature.transactions.domain.repository.CategoryRepository
import com.baburamtechnologies.expensetracker.feature.transactions.domain.repository.TransactionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TransactionModule {

    @Binds
    @Singleton
    abstract fun bindTransactionLocalDataSource(
        impl: TransactionLocalDataSourceImpl
    ): TransactionLocalDataSource

    @Binds
    @Singleton
    abstract fun bindTransactionRepository(
        impl: TransactionRepositoryImpl
    ): TransactionRepository

    @Binds
    @Singleton
    abstract fun bindCategoryLocalDataSource(
        impl: CategoryLocalDataSourceImpl
    ): CategoryLocalDataSource

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(
        impl: CategoryRepositoryImpl
    ): CategoryRepository
}
