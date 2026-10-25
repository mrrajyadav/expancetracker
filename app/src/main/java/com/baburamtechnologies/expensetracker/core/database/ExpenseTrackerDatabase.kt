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
                // Create new table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `transactions_new` (
                        `id` TEXT NOT NULL, 
                        `user_id` TEXT NOT NULL, 
                        `amount` INTEGER NOT NULL, 
                        `type` TEXT NOT NULL, 
                        `category_level1_id` TEXT NOT NULL, 
                        `category_level2_id` TEXT NOT NULL, 
                        `category_level3_id` TEXT, 
                        `payment_mode_id` TEXT, 
                        `note` TEXT, 
                        `transaction_date` TEXT NOT NULL, 
                        `created_at` TEXT NOT NULL, 
                        `updated_at` TEXT NOT NULL, 
                        `is_deleted` INTEGER NOT NULL, 
                        PRIMARY KEY(`id`), 
                        FOREIGN KEY(`user_id`) REFERENCES `profiles`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT, 
                        FOREIGN KEY(`category_level1_id`) REFERENCES `categories`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT, 
                        FOREIGN KEY(`category_level2_id`) REFERENCES `categories`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT, 
                        FOREIGN KEY(`category_level3_id`) REFERENCES `categories`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL, 
                        FOREIGN KEY(`payment_mode_id`) REFERENCES `payment_modes`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL
                    )
                """.trimIndent())
                // Copy data and convert amount to paise (Long)
                db.execSQL("INSERT INTO `transactions_new` (`id`, `user_id`, `amount`, `type`, `category_level1_id`, `category_level2_id`, `category_level3_id`, `payment_mode_id`, `note`, `transaction_date`, `created_at`, `updated_at`, `is_deleted`) SELECT `id`, `user_id`, CAST(`amount` * 100 AS INTEGER), `type`, `category_level1_id`, `category_level2_id`, `category_level3_id`, `payment_mode_id`, `note`, `transaction_date`, `created_at`, `updated_at`, `is_deleted` FROM `transactions`")
                // Drop old table
                db.execSQL("DROP TABLE `transactions`")
                // Rename new table to old table
                db.execSQL("ALTER TABLE `transactions_new` RENAME TO `transactions`")
                // Recreate indices
                db.execSQL("CREATE INDEX IF NOT EXISTS `idx_transaction_user` ON `transactions` (`user_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `idx_transaction_date` ON `transactions` (`transaction_date`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `idx_transaction_cat_l1` ON `transactions` (`category_level1_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `idx_transaction_cat_l2` ON `transactions` (`category_level2_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `idx_transaction_cat_l3` ON `transactions` (`category_level3_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `idx_transaction_payment_mode` ON `transactions` (`payment_mode_id`)")
            }
        }
    }
}
