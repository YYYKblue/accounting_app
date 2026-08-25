package com.yyykblue.accounting.model

import com.yyykblue.accounting.data.TransactionEntity
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

enum class SpendingPeriod {
    DAY,
    WEEK,
    MONTH,
    YEAR,
}

data class SpendingBar(
    val label: String,
    val amountCents: Long,
)

object FinanceCalculator {
    fun totalExpenses(transactions: List<TransactionEntity>): Long =
        transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amountCents }

    fun creditPurchases(transactions: List<TransactionEntity>): Long =
        transactions
            .filter { it.type == TransactionType.EXPENSE && it.paymentMethod == PaymentMethod.CREDIT }
            .sumOf { it.amountCents }

    fun debtRepayments(transactions: List<TransactionEntity>): Long =
        transactions.filter { it.type == TransactionType.DEBT_REPAYMENT }.sumOf { it.amountCents }

    fun outstandingDebt(transactions: List<TransactionEntity>): Long =
        transactions
            .sortedWith(compareBy<TransactionEntity> { it.timestamp }.thenBy { it.id })
            .fold(0L) { debt, transaction ->
                when {
                    transaction.type == TransactionType.EXPENSE &&
                        transaction.paymentMethod == PaymentMethod.CREDIT -> debt + transaction.amountCents
                    transaction.type == TransactionType.DEBT_REPAYMENT ->
                        (debt - transaction.amountCents).coerceAtLeast(0)
                    else -> debt
                }
            }

    fun spendingBars(
        transactions: List<TransactionEntity>,
        period: SpendingPeriod,
        today: LocalDate = LocalDate.now(),
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): List<SpendingBar> {
        val expenses = transactions.filter { it.type == TransactionType.EXPENSE }
        return when (period) {
            SpendingPeriod.DAY -> dailyBars(expenses, today, zoneId)
            SpendingPeriod.WEEK -> weeklyBars(expenses, today, zoneId)
            SpendingPeriod.MONTH -> monthlyBars(expenses, today, zoneId)
            SpendingPeriod.YEAR -> yearlyBars(expenses, today, zoneId)
        }
    }

    private fun dailyBars(
        expenses: List<TransactionEntity>,
        today: LocalDate,
        zoneId: ZoneId,
    ): List<SpendingBar> = (6 downTo 0).map { daysAgo ->
        val date = today.minusDays(daysAgo.toLong())
        SpendingBar(
            label = "${date.monthValue}/${date.dayOfMonth}",
            amountCents = expenses
                .filter { it.timestamp.toLocalDate(zoneId) == date }
                .sumOf { it.amountCents },
        )
    }

    private fun weeklyBars(
        expenses: List<TransactionEntity>,
        today: LocalDate,
        zoneId: ZoneId,
    ): List<SpendingBar> {
        val currentMonday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        return (4 downTo 0).map { weeksAgo ->
            val start = currentMonday.minusWeeks(weeksAgo.toLong())
            val end = start.plusDays(6)
            SpendingBar(
                label = "${start.monthValue}/${start.dayOfMonth}",
                amountCents = expenses
                    .filter { it.timestamp.toLocalDate(zoneId) in start..end }
                    .sumOf { it.amountCents },
            )
        }
    }

    private fun monthlyBars(
        expenses: List<TransactionEntity>,
        today: LocalDate,
        zoneId: ZoneId,
    ): List<SpendingBar> {
        val firstMonth = YearMonth.of(2026, 8)
        val currentMonth = YearMonth.from(today)
        if (currentMonth < firstMonth) return emptyList()
        return generateSequence(firstMonth) { month ->
            month.plusMonths(1).takeIf { it <= currentMonth }
        }.map { month ->
            SpendingBar(
                label = "${month.year % 100}/${month.monthValue}",
                amountCents = expenses
                    .filter { YearMonth.from(it.timestamp.toLocalDate(zoneId)) == month }
                    .sumOf { it.amountCents },
            )
        }.toList()
    }

    private fun yearlyBars(
        expenses: List<TransactionEntity>,
        today: LocalDate,
        zoneId: ZoneId,
    ): List<SpendingBar> = (2026..today.year).map { year ->
        SpendingBar(
            label = year.toString(),
            amountCents = expenses
                .filter { it.timestamp.toLocalDate(zoneId).year == year }
                .sumOf { it.amountCents },
        )
    }
}

private fun Long.toLocalDate(zoneId: ZoneId): LocalDate =
    Instant.ofEpochMilli(this).atZone(zoneId).toLocalDate()
