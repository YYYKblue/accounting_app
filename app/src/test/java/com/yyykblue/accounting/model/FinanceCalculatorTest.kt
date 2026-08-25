package com.yyykblue.accounting.model

import com.yyykblue.accounting.data.TransactionEntity
import java.time.LocalDate
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Test

class FinanceCalculatorTest {
    @Test
    fun `repayment reduces debt but does not count as expense`() {
        val transactions = listOf(
            transaction(10_000, TransactionType.EXPENSE, PaymentMethod.CREDIT, LocalDate.of(2026, 8, 3)),
            transaction(2_000, TransactionType.EXPENSE, PaymentMethod.BALANCE, LocalDate.of(2026, 8, 4)),
            transaction(4_000, TransactionType.DEBT_REPAYMENT, PaymentMethod.BALANCE, LocalDate.of(2026, 8, 5)),
        )

        assertEquals(12_000L, FinanceCalculator.totalExpenses(transactions))
        assertEquals(6_000L, FinanceCalculator.outstandingDebt(transactions))
    }

    @Test
    fun `repayment cannot create a credit that cancels later borrowing`() {
        val transactions = listOf(
            transaction(1_000, TransactionType.EXPENSE, PaymentMethod.CREDIT, LocalDate.of(2026, 8, 1)),
            transaction(2_000, TransactionType.DEBT_REPAYMENT, PaymentMethod.BALANCE, LocalDate.of(2026, 8, 2)),
            transaction(500, TransactionType.EXPENSE, PaymentMethod.CREDIT, LocalDate.of(2026, 8, 3)),
        )

        assertEquals(500L, FinanceCalculator.outstandingDebt(transactions))
    }

    @Test
    fun `daily bars show the latest seven days and only expenses`() {
        val transactions = listOf(
            transaction(1_000, TransactionType.EXPENSE, PaymentMethod.BALANCE, LocalDate.of(2026, 8, 20)),
            transaction(2_000, TransactionType.EXPENSE, PaymentMethod.CREDIT, LocalDate.of(2026, 8, 26)),
            transaction(9_000, TransactionType.INCOME, PaymentMethod.BALANCE, LocalDate.of(2026, 8, 25)),
            transaction(500, TransactionType.DEBT_REPAYMENT, PaymentMethod.BALANCE, LocalDate.of(2026, 8, 24)),
            transaction(8_000, TransactionType.EXPENSE, PaymentMethod.BALANCE, LocalDate.of(2026, 8, 19)),
        )

        val bars = FinanceCalculator.spendingBars(
            transactions,
            SpendingPeriod.DAY,
            LocalDate.of(2026, 8, 26),
            ZoneOffset.UTC,
        )

        assertEquals(7, bars.size)
        assertEquals("8/20", bars.first().label)
        assertEquals("8/26", bars.last().label)
        assertEquals(3_000L, bars.sumOf { it.amountCents })
    }

    @Test
    fun `weekly bars exclude income and repayment`() {
        val transactions = listOf(
            transaction(1_000, TransactionType.EXPENSE, PaymentMethod.BALANCE, LocalDate.of(2026, 8, 17)),
            transaction(2_000, TransactionType.EXPENSE, PaymentMethod.CREDIT, LocalDate.of(2026, 8, 23)),
            transaction(9_000, TransactionType.INCOME, PaymentMethod.BALANCE, LocalDate.of(2026, 8, 20)),
            transaction(500, TransactionType.DEBT_REPAYMENT, PaymentMethod.BALANCE, LocalDate.of(2026, 8, 21)),
        )

        val bars = FinanceCalculator.spendingBars(
            transactions = transactions,
            period = SpendingPeriod.WEEK,
            today = LocalDate.of(2026, 8, 26),
            zoneId = ZoneOffset.UTC,
        )

        assertEquals(5, bars.size)
        assertEquals(3_000L, bars.sumOf { it.amountCents })
        assertEquals(3_000L, bars[bars.lastIndex - 1].amountCents)
        assertEquals(0L, bars.last().amountCents)
    }

    @Test
    fun `monthly and yearly bars aggregate expense periods`() {
        val transactions = listOf(
            transaction(1_000, TransactionType.EXPENSE, PaymentMethod.BALANCE, LocalDate.of(2025, 9, 1)),
            transaction(2_000, TransactionType.EXPENSE, PaymentMethod.BALANCE, LocalDate.of(2026, 8, 1)),
            transaction(3_000, TransactionType.DEBT_REPAYMENT, PaymentMethod.BALANCE, LocalDate.of(2026, 8, 2)),
        )

        val months = FinanceCalculator.spendingBars(
            transactions,
            SpendingPeriod.MONTH,
            LocalDate.of(2026, 8, 26),
            ZoneOffset.UTC,
        )
        val years = FinanceCalculator.spendingBars(
            transactions,
            SpendingPeriod.YEAR,
            LocalDate.of(2026, 8, 26),
            ZoneOffset.UTC,
        )

        assertEquals(1, months.size)
        assertEquals("26/8", months.single().label)
        assertEquals(2_000L, months.sumOf { it.amountCents })
        assertEquals(1, years.size)
        assertEquals("2026", years.single().label)
        assertEquals(2_000L, years.sumOf { it.amountCents })
    }

    @Test
    fun `monthly and yearly bars grow after their fixed starting periods`() {
        val months = FinanceCalculator.spendingBars(
            emptyList(),
            SpendingPeriod.MONTH,
            LocalDate.of(2026, 10, 1),
            ZoneOffset.UTC,
        )
        val years = FinanceCalculator.spendingBars(
            emptyList(),
            SpendingPeriod.YEAR,
            LocalDate.of(2028, 1, 1),
            ZoneOffset.UTC,
        )

        assertEquals(listOf("26/8", "26/9", "26/10"), months.map { it.label })
        assertEquals(listOf("2026", "2027", "2028"), years.map { it.label })
    }

    private fun transaction(
        amountCents: Long,
        type: TransactionType,
        paymentMethod: PaymentMethod,
        date: LocalDate,
    ) = TransactionEntity(
        amountCents = amountCents,
        type = type,
        category = "测试",
        merchant = "测试",
        note = "",
        timestamp = date.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli(),
        paymentMethod = paymentMethod,
    )
}
