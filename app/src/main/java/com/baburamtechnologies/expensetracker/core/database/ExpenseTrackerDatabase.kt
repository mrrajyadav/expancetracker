package com.baburamtechnologies.expensetracker.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.baburamtechnologies.expensetracker.core.database.model.ProfileEntity
import com.baburamtechnologies.expensetracker.feature.settings.data.local.PaymentModeDao
import com.baburamtechnologies.expensetracker.feature.settings.data.local.UserSettingsDao
import com.baburamtechnologies.expensetracker.feature.settings.data.local.model.PaymentModeEntity
import com.baburamtechnologies.expensetracker.feature.settings.data.local.model.UserSettingsEntity
import com.baburamtechnologies.expensetracker.feature.transactions.data.local.CategoryDao
import com.baburamtechnologies.expensetracker.feature.transactions.data.local.TransactionDao
import com.baburamtechnologies.expensetracker.feature.transactions.data.local.model.CategoryEntity
import com.baburamtechnologies.expensetracker.feature.transactions.data.local.model.TransactionEntity

@Database(
    entities = [
        ProfileEntity::class,
        TransactionEntity::class,
        CategoryEntity::class,
        PaymentModeEntity::class,
        UserSettingsEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class ExpenseTrackerDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun paymentModeDao(): PaymentModeDao
    abstract fun userSettingsDao(): UserSettingsDao
    abstract fun profileDao(): ProfileDao

    companion object {
        const val DATABASE_NAME = "expense_tracker_db"

        val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                // The schema is structurally the same (amount is still INTEGER/Long).
                // We avoid multiplying by 100 here to prevent data corruption for users
                // who already have their data stored in paise format. 
                // Any logical amount conversion should be handled at the repository/UI layer 
                // or via a separate explicit user-triggered data fixup.
            }
        }
    }
}
