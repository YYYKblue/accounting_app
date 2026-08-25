package com.yyykblue.accounting.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yyykblue.accounting.data.TransactionEntity
import com.yyykblue.accounting.model.PaymentMethod
import com.yyykblue.accounting.model.TransactionType

@Composable
fun HomeScreen(
    state: AccountingUiState,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onEdit: (TransactionEntity) -> Unit,
    onDelete: (TransactionEntity) -> Unit,
) {
    var pendingDelete by remember { mutableStateOf<TransactionEntity?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                IconButton(onClick = onPreviousMonth) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "上个月")
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${state.month.year}年${state.month.monthValue}月", fontWeight = FontWeight.SemiBold)
                    Text("本月收支差额 ${state.balanceCents.asMoney()}", style = MaterialTheme.typography.labelMedium)
                }
                IconButton(onClick = onNextMonth) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "下个月")
                }
            }
        }
        item {
            SummaryCard(state)
        }
        if (state.transactions.isEmpty()) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 64.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("这个月还没有账单", fontSize = 18.sp, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(6.dp))
                    Text("点下方“记一笔”开始手动记账", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            items(state.transactions, key = { it.id }) { transaction ->
                TransactionRow(
                    transaction = transaction,
                    onEdit = { onEdit(transaction) },
                    onDelete = { pendingDelete = transaction },
                )
            }
        }
    }

    pendingDelete?.let { transaction ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("删除这笔账？") },
            text = { Text("${transaction.merchant}  ${transaction.amountCents.asMoney()}") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(transaction)
                    pendingDelete = null
                }) { Text("删除") }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("取消") }
            },
        )
    }
}

@Composable
private fun SummaryCard(state: AccountingUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            SummaryValue("支出", state.expenseCents.asMoney(), Color.White)
            SummaryValue("收入", state.incomeCents.asMoney(), Color.White)
            SummaryValue("待还借贷", state.outstandingDebtCents.asMoney(), Color.White)
        }
    }
}

@Composable
private fun SummaryValue(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = color.copy(alpha = 0.8f), style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.height(4.dp))
        Text(value, color = color, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun TransactionRow(
    transaction: TransactionEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onEdit),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 14.dp, bottom = 14.dp, end = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(transaction.merchant, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(3.dp))
                Text(
                    buildString {
                        append(transaction.category)
                        if (transaction.type == TransactionType.EXPENSE) {
                            append(if (transaction.paymentMethod == PaymentMethod.CREDIT) " · 借贷" else " · 余额")
                        }
                        append(" · ${transaction.timestamp.asDateTime()}")
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (transaction.note.isNotBlank()) {
                    HorizontalDivider(Modifier.padding(vertical = 6.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    Text(transaction.note, style = MaterialTheme.typography.bodySmall)
                }
            }
            Text(
                text = (if (transaction.type == TransactionType.INCOME) "+" else "−") + transaction.amountCents.asMoney(),
                color = when (transaction.type) {
                    TransactionType.EXPENSE -> MaterialTheme.colorScheme.primary
                    TransactionType.INCOME -> MaterialTheme.colorScheme.secondary
                    TransactionType.DEBT_REPAYMENT -> MaterialTheme.colorScheme.onSurfaceVariant
                },
                fontWeight = FontWeight.Bold,
            )
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.DeleteOutline, contentDescription = "删除", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
