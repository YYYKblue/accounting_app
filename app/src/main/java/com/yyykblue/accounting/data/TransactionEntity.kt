package com.yyykblue.accounting.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.yyykblue.accounting.model.PaymentMethod
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
    @ColumnInfo(defaultValue = "'BALANCE'")
    val paymentMethod: PaymentMethod = PaymentMethod.BALANCE,
    // Legacy columns remain so updates preserve databases created by 0.1.0.
    val source: TransactionSource = TransactionSource.MANUAL,
    val rawText: String? = null,
    val fingerprint: String? = null,
)

@Entity(
    tableName = "custom_categories",
    primaryKeys = ["name", "type"],
)
data class CategoryEntity(
    val name: String,
    val type: TransactionType,
)

@Entity(tableName = "merchants")
data class MerchantEntity(
    @PrimaryKey val name: String,
)
