package com.baburamtechnologies.expensetracker.core.di

import com.baburamtechnologies.expensetracker.core.di.qualifier.DefaultDispatcher
import com.baburamtechnologies.expensetracker.core.di.qualifier.IoDispatcher
import com.baburamtechnologies.expensetracker.core.di.qualifier.MainDispatcher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

@Module
@InstallIn(SingletonComponent::class)
object DispatcherModule {

    @IoDispatcher
    @Provides
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @DefaultDispatcher
    @Provides
    fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

    @MainDispatcher
    @Provides
    fun provideMainDispatcher(): CoroutineDispatcher = Dispatchers.Main
}
