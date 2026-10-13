package com.baburamtechnologies.expensetracker.core.di

import com.baburamtechnologies.expensetracker.core.backup.data.BackupRepositoryImpl
import com.baburamtechnologies.expensetracker.core.backup.domain.BackupRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BackupModule {

    @Binds
    @Singleton
    abstract fun bindBackupRepository(
        impl: BackupRepositoryImpl
    ): BackupRepository
}
