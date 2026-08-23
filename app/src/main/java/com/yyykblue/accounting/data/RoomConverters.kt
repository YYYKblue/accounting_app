package com.yyykblue.accounting.data

import androidx.room.TypeConverter
import com.yyykblue.accounting.model.TransactionSource
import com.yyykblue.accounting.model.TransactionType

class RoomConverters {
    @TypeConverter
    fun transactionTypeToString(value: TransactionType): String = value.name

    @TypeConverter
    fun stringToTransactionType(value: String): TransactionType = TransactionType.valueOf(value)

    @TypeConverter
    fun transactionSourceToString(value: TransactionSource): String = value.name

    @TypeConverter
    fun stringToTransactionSource(value: String): TransactionSource = TransactionSource.valueOf(value)
}
