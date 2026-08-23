package com.yyykblue.accounting.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [TransactionEntity::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(RoomConverters::class)
abstract class AccountingDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
}
