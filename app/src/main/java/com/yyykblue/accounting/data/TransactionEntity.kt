package com.yyykblue.accounting.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.yyykblue.accounting.model.TransactionSource
import com.yyykblue.accounting.model.TransactionType

@Entity(
    tableName = "transactions",
    indices = [Index(value = ["fingerprint"], unique = true)],
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amountCents: Long,
    val type: TransactionType,
    val category: String,
    val merchant: String,
    val note: String,
    val timestamp: Long,
    val source: TransactionSource,
    val rawText: String? = null,
    val fingerprint: String? = null,
)
