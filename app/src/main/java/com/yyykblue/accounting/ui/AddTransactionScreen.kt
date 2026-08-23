package com.yyykblue.accounting.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yyykblue.accounting.data.TransactionEntity
import com.yyykblue.accounting.model.Categories
import com.yyykblue.accounting.model.TransactionType
import java.math.BigDecimal

@Composable
fun AddTransactionScreen(
    editing: TransactionEntity?,
    onCancel: () -> Unit,
    onSave: (String, TransactionType, String, String, String) -> Unit,
) {
    key(editing?.id) {
        var type by remember { mutableStateOf(editing?.type ?: TransactionType.EXPENSE) }
        var amount by remember {
            mutableStateOf(editing?.let { BigDecimal.valueOf(it.amountCents, 2).toPlainString() }.orEmpty())
        }
        var category by remember { mutableStateOf(editing?.category ?: Categories.forType(type).first()) }
        var merchant by remember { mutableStateOf(editing?.merchant.orEmpty()) }
        var note by remember { mutableStateOf(editing?.note.orEmpty()) }
        val amountValid = amount.toBigDecimalOrNull()?.let { it > BigDecimal.ZERO && it.scale() <= 2 } == true

        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        ) {
            Text(if (editing == null) "记一笔" else "编辑账单", fontSize = 26.sp, fontWeight = FontWeight.Bold)
            if (editing != null) {
                Text(
                    "来源：${editing.source.displayName} · ${editing.timestamp.asDateTime()}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FilterChip(
                    selected = type == TransactionType.EXPENSE,
                    onClick = {
                        type = TransactionType.EXPENSE
                        if (category !in Categories.expense) category = Categories.expense.first()
                    },
                    label = { Text("支出") },
                )
                FilterChip(
                    selected = type == TransactionType.INCOME,
                    onClick = {
                        type = TransactionType.INCOME
                        if (category !in Categories.income) category = Categories.income.first()
                    },
                    label = { Text("收入") },
                )
            }
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = amount,
                onValueChange = { input ->
                    if (input.matches(Regex("\\d{0,9}(?:\\.\\d{0,2})?"))) amount = input
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("金额") },
                prefix = { Text("¥ ") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = amount.isNotEmpty() && !amountValid,
            )
            Spacer(Modifier.height(18.dp))
            Text("分类", fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(6.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Categories.forType(type).forEach { item ->
                    FilterChip(
                        selected = category == item,
                        onClick = { category = item },
                        label = { Text(item) },
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = merchant,
                onValueChange = { merchant = it.take(40) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(if (type == TransactionType.EXPENSE) "商户/用途（可选）" else "收入来源（可选）") },
                singleLine = true,
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = note,
                onValueChange = { note = it.take(100) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("备注（可选）") },
                minLines = 2,
            )
            Spacer(Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                    Text("取消")
                }
                Button(
                    onClick = { onSave(amount, type, category, merchant, note) },
                    enabled = amountValid,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("保存")
                }
            }
        }
    }
}
