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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.yyykblue.accounting.model.MoneyAmount
import com.yyykblue.accounting.model.PaymentMethod
import com.yyykblue.accounting.model.TransactionType
import java.math.BigDecimal

@Composable
fun AddTransactionScreen(
    editing: TransactionEntity?,
    customCategories: Map<TransactionType, List<String>>,
    merchantSuggestions: List<String>,
    outstandingDebtCents: Long,
    onCancel: () -> Unit,
    onSave: (String, TransactionType, String, String, String, PaymentMethod) -> Unit,
) {
    key(editing?.id) {
        var type by remember { mutableStateOf(editing?.type ?: TransactionType.EXPENSE) }
        var paymentMethod by remember { mutableStateOf(editing?.paymentMethod ?: PaymentMethod.BALANCE) }
        var amount by remember {
            mutableStateOf(editing?.let { BigDecimal.valueOf(it.amountCents, 2).toPlainString() }.orEmpty())
        }
        var category by remember { mutableStateOf(editing?.category ?: Categories.forType(type).first()) }
        var merchant by remember { mutableStateOf(editing?.merchant.orEmpty()) }
        var note by remember { mutableStateOf(editing?.note.orEmpty()) }
        val amountValid = MoneyAmount.parseCents(amount) != null
        val availableCategories = (Categories.forType(type) + customCategories[type].orEmpty()).distinct()

        fun selectType(newType: TransactionType) {
            type = newType
            if (category !in (Categories.forType(newType) + customCategories[newType].orEmpty())) {
                category = Categories.forType(newType).first()
            }
            if (newType != TransactionType.EXPENSE) paymentMethod = PaymentMethod.BALANCE
        }

        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        ) {
            Text(if (editing == null) "记一笔" else "编辑账单", fontSize = 26.sp, fontWeight = FontWeight.Bold)
            if (editing != null) {
                Text(
                    "记录时间：${editing.timestamp.asDateTime()}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(20.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FilterChip(
                    selected = type == TransactionType.EXPENSE,
                    onClick = { selectType(TransactionType.EXPENSE) },
                    label = { Text("支出") },
                )
                FilterChip(
                    selected = type == TransactionType.INCOME,
                    onClick = { selectType(TransactionType.INCOME) },
                    label = { Text("收入") },
                )
                FilterChip(
                    selected = type == TransactionType.DEBT_REPAYMENT,
                    onClick = { selectType(TransactionType.DEBT_REPAYMENT) },
                    label = { Text("还借贷") },
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

            if (type == TransactionType.EXPENSE) {
                Spacer(Modifier.height(18.dp))
                Text("付款途径", fontWeight = FontWeight.Medium)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FilterChip(
                        selected = paymentMethod == PaymentMethod.BALANCE,
                        onClick = { paymentMethod = PaymentMethod.BALANCE },
                        label = { Text("余额") },
                    )
                    FilterChip(
                        selected = paymentMethod == PaymentMethod.CREDIT,
                        onClick = { paymentMethod = PaymentMethod.CREDIT },
                        label = { Text("借贷") },
                    )
                }
            } else if (type == TransactionType.DEBT_REPAYMENT) {
                Spacer(Modifier.height(14.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Text("当前待还借贷 ${outstandingDebtCents.asMoney()}", fontWeight = FontWeight.SemiBold)
                        Text(
                            "本次还款只减少待还总额，不计入消费和总支出。",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }

            Spacer(Modifier.height(18.dp))
            if (type == TransactionType.DEBT_REPAYMENT) {
                Text("分类：借贷还款", fontWeight = FontWeight.Medium)
            } else {
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it.take(20) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("分类（可自定义）") },
                    singleLine = true,
                )
                Spacer(Modifier.height(6.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    availableCategories.forEach { item ->
                        FilterChip(
                            selected = category == item,
                            onClick = { category = item },
                            label = { Text(item) },
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = merchant,
                onValueChange = { merchant = it.take(40) },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(
                        when (type) {
                            TransactionType.EXPENSE -> "商户/用途（可自定义）"
                            TransactionType.INCOME -> "收入来源（可自定义）"
                            TransactionType.DEBT_REPAYMENT -> "还款账户/用途（可选）"
                        },
                    )
                },
                singleLine = true,
            )
            if (merchantSuggestions.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    merchantSuggestions.take(8).forEach { item ->
                        FilterChip(
                            selected = merchant == item,
                            onClick = { merchant = item },
                            label = { Text(item) },
                        )
                    }
                }
            }
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
                    onClick = { onSave(amount, type, category, merchant, note, paymentMethod) },
                    enabled = amountValid,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("保存")
                }
            }
        }
    }
}
