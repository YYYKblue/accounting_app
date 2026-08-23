package com.yyykblue.accounting.notification

import com.yyykblue.accounting.model.TransactionSource
import com.yyykblue.accounting.model.TransactionType

data class PaymentNotification(
    val packageName: String,
    val title: String,
    val text: String,
    val postedAt: Long,
    val notificationKey: String = "",
) {
    val fullText: String = "$title $text".trim()
}

data class ParsedTransaction(
    val amountCents: Long,
    val type: TransactionType,
    val merchant: String,
    val category: String,
    val source: TransactionSource,
    val rawText: String,
    val timestamp: Long,
    val fingerprint: String,
)

interface NotificationParser {
    fun canParse(notification: PaymentNotification): Boolean
    fun parse(notification: PaymentNotification): ParsedTransaction?
}
