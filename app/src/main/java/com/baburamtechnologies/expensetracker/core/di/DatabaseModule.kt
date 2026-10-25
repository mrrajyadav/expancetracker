package com.baburamtechnologies.expensetracker.core.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.baburamtechnologies.expensetracker.core.database.ExpenseTrackerDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): ExpenseTrackerDatabase = Room.databaseBuilder(
        context,
        ExpenseTrackerDatabase::class.java,
        ExpenseTrackerDatabase.DATABASE_NAME
    )
        .addMigrations(ExpenseTrackerDatabase.MIGRATION_1_2)
        .addCallback(object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
            }
        })
        .build()

    @Provides
    @Singleton
    fun provideTransactionDao(db: ExpenseTrackerDatabase) = db.transactionDao()

    @Provides
    @Singleton
    fun provideCategoryDao(db: ExpenseTrackerDatabase) = db.categoryDao()

    @Provides
    @Singleton
    fun providePaymentModeDao(db: ExpenseTrackerDatabase) = db.paymentModeDao()

    @Provides
    @Singleton
    fun provideUserSettingsDao(db: ExpenseTrackerDatabase) = db.userSettingsDao()

    @Provides
    @Singleton
    fun provideProfileDao(db: ExpenseTrackerDatabase) = db.profileDao()
}
