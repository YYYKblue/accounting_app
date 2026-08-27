package com.yyykblue.accounting

import android.app.Application
import androidx.room.Room
import com.yyykblue.accounting.data.AccountingDatabase
import com.yyykblue.accounting.data.TransactionRepository

class AccountingApplication : Application() {
    private val database by lazy {
        Room.databaseBuilder(
            applicationContext,
            AccountingDatabase::class.java,
            "accounting.db",
        ).addMigrations(
            AccountingDatabase.MIGRATION_1_2,
            AccountingDatabase.MIGRATION_2_3,
        ).build()
    }

    val repository by lazy {
        TransactionRepository(database.transactionDao(), database.dailyReminderDao())
    }
}
