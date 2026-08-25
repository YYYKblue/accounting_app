package com.yyykblue.accounting.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yyykblue.accounting.model.FinanceCalculator
import com.yyykblue.accounting.model.SpendingBar
import com.yyykblue.accounting.model.SpendingPeriod
import com.yyykblue.accounting.model.TransactionType
import java.math.BigDecimal
import java.math.RoundingMode

@Composable
fun StatsScreen(state: AccountingUiState) {
    var period by remember { mutableStateOf(SpendingPeriod.WEEK) }
    val bars = FinanceCalculator.spendingBars(state.allTransactions, period)
    val categoryTotals = state.transactions
        .filter { it.type == TransactionType.EXPENSE }
        .groupBy { it.category }
        .mapValues { (_, items) -> items.sumOf { it.amountCents } }
        .entries
        .sortedByDescending { it.value }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(24.dp),
    ) {
        item {
            Text("消费统计", fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(14.dp))
            DebtCard(state)
            Spacer(Modifier.height(18.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PeriodChip("天", SpendingPeriod.DAY, period) { period = it }
                PeriodChip("周", SpendingPeriod.WEEK, period) { period = it }
                PeriodChip("月", SpendingPeriod.MONTH, period) { period = it }
                PeriodChip("年", SpendingPeriod.YEAR, period) { period = it }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                when (period) {
                    SpendingPeriod.DAY -> "最近 7 天消费"
                    SpendingPeriod.WEEK -> "最近 5 周消费"
                    SpendingPeriod.MONTH -> "2026 年 8 月起的月消费"
                    SpendingPeriod.YEAR -> "2026 年起的年消费"
                },
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                bars.sumOf { it.amountCents }.asMoney(),
                fontSize = 28.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(8.dp))
            SpendingBarChart(bars)
            Spacer(Modifier.height(28.dp))
            Text("${state.month.year}年${state.month.monthValue}月分类", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(14.dp))
        }
        if (categoryTotals.isEmpty()) {
            item { Text("本月有支出后，这里会显示分类占比。", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        } else {
            categoryTotals.forEach { (category, amount) ->
                item {
                    Column(Modifier.fillMaxWidth().padding(bottom = 20.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(category, fontWeight = FontWeight.Medium)
                            Text("${amount.asMoney()}  ${amount * 100 / state.expenseCents}%")
                        }
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { amount.toFloat() / state.expenseCents.toFloat() },
                            modifier = Modifier.fillMaxWidth().height(8.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DebtCard(state: AccountingUiState) {
    val credit = FinanceCalculator.creditPurchases(state.allTransactions)
    val repaid = FinanceCalculator.debtRepayments(state.allTransactions)
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("当前待还借贷", color = MaterialTheme.colorScheme.onPrimaryContainer)
            Text(
                state.outstandingDebtCents.asMoney(),
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "借贷消费 ${credit.asMoney()} · 已登记还款 ${repaid.asMoney()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

@Composable
private fun PeriodChip(
    title: String,
    value: SpendingPeriod,
    selected: SpendingPeriod,
    onSelect: (SpendingPeriod) -> Unit,
) {
    FilterChip(
        selected = value == selected,
        onClick = { onSelect(value) },
        label = { Text(title) },
    )
}

@Composable
private fun SpendingBarChart(bars: List<SpendingBar>) {
    val maximum = bars.maxOfOrNull { it.amountCents }?.coerceAtLeast(1) ?: 1
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .height(230.dp)
                .padding(horizontal = 12.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            bars.forEach { bar ->
                val fraction = (bar.amountCents.toFloat() / maximum.toFloat()).coerceIn(0.02f, 1f)
                Column(
                    modifier = Modifier.width(52.dp).fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(compactMoney(bar.amountCents), fontSize = 9.sp, maxLines = 1)
                    Spacer(Modifier.height(4.dp))
                    Box(
                        modifier = Modifier.weight(1f).width(28.dp),
                        contentAlignment = Alignment.BottomCenter,
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(fraction)
                                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                .background(MaterialTheme.colorScheme.primary),
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(bar.label, fontSize = 10.sp, maxLines = 1)
                }
            }
        }
    }
}

private fun compactMoney(cents: Long): String {
    if (cents == 0L) return "¥0"
    val yuan = BigDecimal.valueOf(cents, 2)
    return if (yuan >= BigDecimal(10_000)) {
        "¥" + yuan.divide(BigDecimal(10_000), 1, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString() + "万"
    } else {
        "¥" + yuan.setScale(0, RoundingMode.HALF_UP).toPlainString()
    }
}
