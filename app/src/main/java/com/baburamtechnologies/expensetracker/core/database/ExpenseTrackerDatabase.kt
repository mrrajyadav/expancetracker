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
                // Check the actual schema of the 'transactions' table in the old DB
                var isAmountReal = false
                db.query("PRAGMA table_info(transactions)").use { cursor ->
                    val nameIndex = cursor.getColumnIndex("name")
                    val typeIndex = cursor.getColumnIndex("type")
                    while (cursor.moveToNext()) {
                        val name = cursor.getString(nameIndex)
                        val type = cursor.getString(typeIndex)
                        if (name == "amount" && type.equals("REAL", ignoreCase = true)) {
                            isAmountReal = true
                            break
                        }
                    }
                }

                if (isAmountReal) {
                    // 1. Create a temporary table with the correct new schema (amount as INTEGER)
                    db.execSQL("""
                        CREATE TABLE IF NOT EXISTS transactions_new (
                            id TEXT NOT NULL PRIMARY KEY,
                            user_id TEXT NOT NULL,
                            amount INTEGER NOT NULL,
                            type TEXT NOT NULL,
                            category_level1_id TEXT NOT NULL,
                            category_level2_id TEXT NOT NULL,
                            category_level3_id TEXT,
                            payment_mode_id TEXT,
                            note TEXT,
                            transaction_date TEXT NOT NULL,
                            created_at TEXT NOT NULL,
                            updated_at TEXT NOT NULL,
                            is_deleted INTEGER NOT NULL DEFAULT 0,
                            FOREIGN KEY(payment_mode_id) REFERENCES payment_modes(id) ON UPDATE NO ACTION ON DELETE SET NULL
                        )
                    """)

                    // 2. Copy data, converting REAL rupees to INTEGER paise
                    db.execSQL("""
                        INSERT INTO transactions_new (
                            id, user_id, amount, type, category_level1_id, category_level2_id, 
                            category_level3_id, payment_mode_id, note, transaction_date, 
                            created_at, updated_at, is_deleted
                        )
                        SELECT 
                            id, user_id, CAST(ROUND(amount * 100) AS INTEGER), type, category_level1_id, category_level2_id, 
                            category_level3_id, payment_mode_id, note, transaction_date, 
                            created_at, updated_at, is_deleted
                        FROM transactions
                    """)

                    // 3. Replace the old table
                    db.execSQL("DROP TABLE transactions")
                    db.execSQL("ALTER TABLE transactions_new RENAME TO transactions")
                    
                    // 4. Re-create indices if any (assuming indices exist based on foreign keys or queries, 
                    // though Room would complain if we miss ones defined in the Entity. Let's check indices.)
                    db.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_payment_mode_id ON transactions(payment_mode_id)")
                }
            }
        }
    }
}
