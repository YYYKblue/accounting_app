package com.yyykblue.accounting.notification

import com.yyykblue.accounting.model.TransactionSource

class AlipayParser : NotificationParser {
    override fun canParse(notification: PaymentNotification): Boolean =
        notification.packageName == "com.eg.android.AlipayGphone"

    override fun parse(notification: PaymentNotification): ParsedTransaction? =
        PaymentParsing.result(notification, TransactionSource.ALIPAY, "支付宝交易")
}

class WeChatParser : NotificationParser {
    override fun canParse(notification: PaymentNotification): Boolean =
        notification.packageName == "com.tencent.mm"

    override fun parse(notification: PaymentNotification): ParsedTransaction? {
        val trustedPaymentTitle = listOf("微信支付", "支付通知", "收款通知", "到账通知")
            .any(notification.title::contains)
        return if (trustedPaymentTitle) {
            PaymentParsing.result(notification, TransactionSource.WECHAT, "微信交易")
        } else {
            null
        }
    }
}

class BankParser : NotificationParser {
    private val supportedPackages = setOf(
        "cmb.pb",
        "com.chinamworld.main",
        "com.icbc",
        "com.android.bankabc",
        "com.bankcomm.Bankcomm",
        "com.ccb.longjiLife",
        "com.spdbccc.app",
        "com.cib.cibmb",
    )

    override fun canParse(notification: PaymentNotification): Boolean =
        notification.packageName in supportedPackages ||
            notification.packageName.contains("bank", ignoreCase = true)

    override fun parse(notification: PaymentNotification): ParsedTransaction? =
        PaymentParsing.result(notification, TransactionSource.BANK, "银行卡交易")
}

class PaymentParserRegistry(
    private val parsers: List<NotificationParser> = listOf(AlipayParser(), WeChatParser(), BankParser()),
) {
    fun parse(notification: PaymentNotification): ParsedTransaction? =
        parsers.firstOrNull { it.canParse(notification) }?.parse(notification)
}
