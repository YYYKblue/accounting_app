package com.yyykblue.accounting.notification

import com.yyykblue.accounting.model.TransactionSource
import com.yyykblue.accounting.model.TransactionType
import java.math.BigDecimal
import java.math.RoundingMode
import java.security.MessageDigest

object PaymentParsing {
    private val explicitAmount =
        Regex(
            "(?:人民币|RMB|CNY|[¥￥])\\s*((?:[0-9]{1,3}(?:,[0-9]{3})+|[0-9]+)(?:\\.[0-9]{1,2})?)|" +
                "((?:[0-9]{1,3}(?:,[0-9]{3})+|[0-9]+)(?:\\.[0-9]{1,2})?)\\s*元",
        )

    private val incomeWords = listOf("收入", "收款", "到账", "入账", "转入", "退款", "工资")
    private val expenseWords = listOf("支出", "付款", "支付", "消费", "扣款", "转出", "交易成功")

    fun amountCents(text: String): Long? {
        val value = explicitAmount.find(text)?.groupValues?.drop(1)?.firstOrNull { it.isNotBlank() }
            ?: return null
        return runCatching {
            BigDecimal(value.replace(",", ""))
                .setScale(2, RoundingMode.UNNECESSARY)
                .movePointRight(2)
                .longValueExact()
        }.getOrNull()?.takeIf { it > 0 }
    }

    fun type(text: String): TransactionType? {
        val incomeIndex = incomeWords.map(text::indexOf).filter { it >= 0 }.minOrNull()
        val expenseIndex = expenseWords.map(text::indexOf).filter { it >= 0 }.minOrNull()
        return when {
            incomeIndex == null && expenseIndex == null -> null
            incomeIndex != null && expenseIndex == null -> TransactionType.INCOME
            incomeIndex == null -> TransactionType.EXPENSE
            incomeIndex <= checkNotNull(expenseIndex) -> TransactionType.INCOME
            else -> TransactionType.EXPENSE
        }
    }

    fun inferCategory(text: String, type: TransactionType): String {
        if (type == TransactionType.INCOME) {
            return when {
                listOf("工资", "薪资", "薪酬").any(text::contains) -> "工资"
                listOf("退款", "退回").any(text::contains) -> "退款"
                listOf("奖金", "绩效").any(text::contains) -> "奖金"
                listOf("理财", "利息", "分红").any(text::contains) -> "理财"
                else -> "转账"
            }
        }
        return when {
            listOf("餐", "饭", "外卖", "咖啡", "奶茶", "麦当劳", "肯德基", "美团").any(text::contains) -> "餐饮"
            listOf("地铁", "公交", "打车", "滴滴", "铁路", "航空", "加油").any(text::contains) -> "交通"
            listOf("药", "医院", "诊所", "医疗").any(text::contains) -> "医疗"
            listOf("房租", "物业", "水费", "电费", "燃气").any(text::contains) -> "居住"
            listOf("电影", "游戏", "会员", "娱乐").any(text::contains) -> "娱乐"
            listOf("书", "课程", "培训", "教育").any(text::contains) -> "学习"
            listOf("红包", "礼物", "份子").any(text::contains) -> "人情"
            listOf("淘宝", "京东", "拼多多", "超市", "商城", "购物").any(text::contains) -> "购物"
            else -> "其他"
        }
    }

    fun merchant(title: String, fallback: String): String {
        val cleaned = title
            .replace(Regex("付款成功|支付成功|交易提醒|服务通知|收款通知|到账通知"), "")
            .trim(' ', '-', ':', '：')
        return cleaned.ifBlank { fallback }
    }

    fun fingerprint(notification: PaymentNotification, amountCents: Long, type: TransactionType): String {
        val minuteBucket = notification.postedAt / 60_000
        val stableInput = listOf(
            notification.packageName,
            notification.notificationKey,
            amountCents.toString(),
            type.name,
            notification.fullText.replace(Regex("\\s+"), " "),
            minuteBucket.toString(),
        ).joinToString("|")
        return MessageDigest.getInstance("SHA-256")
            .digest(stableInput.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }

    fun result(
        notification: PaymentNotification,
        source: TransactionSource,
        merchantFallback: String,
    ): ParsedTransaction? {
        val amount = amountCents(notification.fullText) ?: return null
        val type = type(notification.fullText) ?: return null
        return ParsedTransaction(
            amountCents = amount,
            type = type,
            merchant = merchant(notification.title, merchantFallback),
            category = inferCategory(notification.fullText, type),
            source = source,
            rawText = notification.fullText,
            timestamp = notification.postedAt,
            fingerprint = fingerprint(notification, amount, type),
        )
    }
}
