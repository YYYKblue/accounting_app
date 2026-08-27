package com.yyykblue.accounting.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [TransactionEntity::class, CategoryEntity::class, MerchantEntity::class, DailyReminderEntity::class],
    version = 3,
    exportSchema = true,
)
@TypeConverters(RoomConverters::class)
abstract class AccountingDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun dailyReminderDao(): DailyReminderDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE transactions ADD COLUMN paymentMethod TEXT NOT NULL DEFAULT 'BALANCE'",
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS custom_categories " +
                        "(name TEXT NOT NULL, type TEXT NOT NULL, PRIMARY KEY(name, type))",
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS merchants (name TEXT NOT NULL, PRIMARY KEY(name))",
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS daily_reminders " +
                        "(id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "title TEXT NOT NULL, completedEpochDay INTEGER)",
                )
            }
        }
    }
}
