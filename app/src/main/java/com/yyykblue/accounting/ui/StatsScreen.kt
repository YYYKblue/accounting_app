package com.yyykblue.accounting.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yyykblue.accounting.model.TransactionType

@Composable
fun StatsScreen(state: AccountingUiState) {
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
            Text("${state.month.monthValue}月支出分析", fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text(state.expenseCents.asMoney(), fontSize = 34.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(28.dp))
        }
        if (categoryTotals.isEmpty()) {
            item { Text("有支出账单后，这里会显示分类占比。", color = MaterialTheme.colorScheme.onSurfaceVariant) }
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
