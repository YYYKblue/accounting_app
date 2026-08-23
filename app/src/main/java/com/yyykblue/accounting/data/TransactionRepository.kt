package com.yyykblue.accounting.data

import kotlinx.coroutines.flow.Flow

class TransactionRepository(private val dao: TransactionDao) {
    val transactions: Flow<List<TransactionEntity>> = dao.observeAll()

    suspend fun add(transaction: TransactionEntity): Boolean = dao.insert(transaction) != -1L

    suspend fun update(transaction: TransactionEntity) = dao.update(transaction)

    suspend fun delete(transaction: TransactionEntity) = dao.delete(transaction)

    suspend fun deleteAll() = dao.deleteAll()
}
