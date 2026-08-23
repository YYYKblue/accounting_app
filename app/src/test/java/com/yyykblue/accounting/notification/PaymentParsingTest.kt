package com.yyykblue.accounting.notification

import com.yyykblue.accounting.model.TransactionSource
import com.yyykblue.accounting.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class PaymentParsingTest {
    private val registry = PaymentParserRegistry()

    @Test
    fun `parses alipay expense with category`() {
        val parsed = registry.parse(
            PaymentNotification(
                packageName = "com.eg.android.AlipayGphone",
                title = "麦当劳 付款成功",
                text = "你已支付35.00元",
                postedAt = 1_700_000_000_000,
                notificationKey = "alipay-1",
            ),
        )

        assertNotNull(parsed)
        assertEquals(3_500L, parsed?.amountCents)
        assertEquals(TransactionType.EXPENSE, parsed?.type)
        assertEquals("餐饮", parsed?.category)
        assertEquals(TransactionSource.ALIPAY, parsed?.source)
        assertEquals("麦当劳", parsed?.merchant)
    }

    @Test
    fun `parses bank income`() {
        val parsed = registry.parse(
            PaymentNotification(
                packageName = "com.example.bank.mobile",
                title = "交易提醒",
                text = "您的账户工资入账人民币5,000.00元",
                postedAt = 1_700_000_000_000,
            ),
        )

        assertEquals(500_000L, parsed?.amountCents)
        assertEquals(TransactionType.INCOME, parsed?.type)
        assertEquals("工资", parsed?.category)

        val accepted = registry.parse(
            PaymentNotification(
                packageName = "com.example.bank.mobile",
                title = "交易提醒",
                text = "您的账户工资入账人民币5000.00",
                postedAt = 1_700_000_000_000,
            ),
        )
        assertEquals(500_000L, accepted?.amountCents)
        assertEquals(TransactionType.INCOME, accepted?.type)
        assertEquals("工资", accepted?.category)
    }

    @Test
    fun `ignores unrelated or ambiguous notifications`() {
        val chat = PaymentNotification(
            packageName = "com.tencent.mm",
            title = "好友",
            text = "今晚8点见",
            postedAt = 1_700_000_000_000,
        )
        val marketing = PaymentNotification(
            packageName = "com.eg.android.AlipayGphone",
            title = "优惠提醒",
            text = "满100元减20元",
            postedAt = 1_700_000_000_000,
        )

        assertNull(registry.parse(chat))
        assertNull(registry.parse(marketing))
    }

    @Test
    fun `fingerprint is stable for identical notification`() {
        val notification = PaymentNotification(
            packageName = "com.tencent.mm",
            title = "微信支付",
            text = "支付￥12.30",
            postedAt = 1_700_000_000_000,
            notificationKey = "same-key",
        )
        assertEquals(registry.parse(notification)?.fingerprint, registry.parse(notification)?.fingerprint)
    }
}
