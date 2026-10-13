package com.baburamtechnologies.expensetracker.core.di

import com.baburamtechnologies.expensetracker.core.database.ProfileLocalDataSource
import com.baburamtechnologies.expensetracker.core.database.ProfileLocalDataSourceImpl
import com.baburamtechnologies.expensetracker.core.database.repository.ProfileRepository
import com.baburamtechnologies.expensetracker.core.database.repository.ProfileRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {
    
    @Binds
    @Singleton
    abstract fun bindProfileLocalDataSource(
        impl: ProfileLocalDataSourceImpl
    ): ProfileLocalDataSource

    @Binds
    @Singleton
    abstract fun bindProfileRepository(
        impl: ProfileRepositoryImpl
    ): ProfileRepository
}
