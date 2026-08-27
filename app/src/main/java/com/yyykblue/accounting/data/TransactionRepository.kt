package com.yyykblue.accounting.data

import com.yyykblue.accounting.model.TransactionType
import kotlinx.coroutines.flow.Flow

class TransactionRepository(
    private val dao: TransactionDao,
    private val reminderDao: DailyReminderDao,
) {
    val transactions: Flow<List<TransactionEntity>> = dao.observeAll()
    val customCategories: Flow<List<CategoryEntity>> = dao.observeCategories()
    val merchants: Flow<List<MerchantEntity>> = dao.observeMerchants()
    val reminders: Flow<List<DailyReminderEntity>> = reminderDao.observeAll()

    suspend fun add(transaction: TransactionEntity): Boolean = dao.insert(transaction) != -1L

    suspend fun update(transaction: TransactionEntity) = dao.update(transaction)

    suspend fun delete(transaction: TransactionEntity) = dao.delete(transaction)

    suspend fun deleteAll() = dao.deleteAll()

    suspend fun rememberCategory(name: String, type: TransactionType) {
        dao.insertCategory(CategoryEntity(name = name, type = type))
    }

    suspend fun rememberMerchant(name: String) {
        dao.insertMerchant(MerchantEntity(name = name))
    }

    suspend fun addReminder(title: String) {
        reminderDao.insert(DailyReminderEntity(title = title))
    }

    suspend fun setReminderCompletedDate(id: Long, completedEpochDay: Long?) {
        reminderDao.setCompletedDate(id, completedEpochDay)
    }

    suspend fun deleteReminder(reminder: DailyReminderEntity) {
        reminderDao.delete(reminder)
    }
}
