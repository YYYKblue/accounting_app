package com.yyykblue.accounting.notification

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.yyykblue.accounting.AccountingApplication
import com.yyykblue.accounting.data.TransactionEntity
import kotlinx.coroutines.launch

class PaymentNotificationListener : NotificationListenerService() {
    private val registry = PaymentParserRegistry()

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        val statusBarNotification = sbn ?: return
        val extras = statusBarNotification.notification.extras
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
        val textParts = listOfNotNull(
            extras.getCharSequence(Notification.EXTRA_TEXT)?.toString(),
            extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString(),
            extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString(),
        ).filter(String::isNotBlank).distinct()

        val parsed = registry.parse(
            PaymentNotification(
                packageName = statusBarNotification.packageName,
                title = title,
                text = textParts.joinToString(" "),
                postedAt = statusBarNotification.postTime,
                notificationKey = statusBarNotification.key,
            ),
        ) ?: return

        val application = applicationContext as AccountingApplication
        application.applicationScope.launch {
            application.repository.add(
                TransactionEntity(
                    amountCents = parsed.amountCents,
                    type = parsed.type,
                    category = parsed.category,
                    merchant = parsed.merchant,
                    note = "自动识别，请核对",
                    timestamp = parsed.timestamp,
                    source = parsed.source,
                    rawText = parsed.rawText,
                    fingerprint = parsed.fingerprint,
                ),
            )
        }
    }
}
