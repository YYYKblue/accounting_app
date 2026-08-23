package com.yyykblue.accounting.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yyykblue.accounting.data.TransactionEntity
import com.yyykblue.accounting.data.TransactionRepository
import com.yyykblue.accounting.model.TransactionSource
import com.yyykblue.accounting.model.TransactionType
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AccountingUiState(
    val month: YearMonth = YearMonth.now(),
    val transactions: List<TransactionEntity> = emptyList(),
) {
    val incomeCents: Long = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amountCents }
    val expenseCents: Long = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amountCents }
    val balanceCents: Long = incomeCents - expenseCents
}

class AccountingViewModel(private val repository: TransactionRepository) : ViewModel() {
    private val selectedMonth = MutableStateFlow(YearMonth.now())

    val uiState: StateFlow<AccountingUiState> = combine(
        repository.transactions,
        selectedMonth,
    ) { allTransactions, month ->
        AccountingUiState(
            month = month,
            transactions = allTransactions.filter { it.timestamp.toYearMonth() == month },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AccountingUiState(),
    )

    fun previousMonth() {
        selectedMonth.value = selectedMonth.value.minusMonths(1)
    }

    fun nextMonth() {
        selectedMonth.value = selectedMonth.value.plusMonths(1)
    }

    fun save(
        amount: String,
        type: TransactionType,
        category: String,
        merchant: String,
        note: String,
        editing: TransactionEntity?,
        onComplete: () -> Unit,
    ) {
        val amountCents = amount.toAmountCents() ?: return
        viewModelScope.launch {
            if (editing == null) {
                repository.add(
                    TransactionEntity(
                        amountCents = amountCents,
                        type = type,
                        category = category,
                        merchant = merchant.trim().ifBlank { if (type == TransactionType.EXPENSE) "日常支出" else "收入" },
                        note = note.trim(),
                        timestamp = System.currentTimeMillis(),
                        source = TransactionSource.MANUAL,
                    ),
                )
            } else {
                repository.update(
                    editing.copy(
                        amountCents = amountCents,
                        type = type,
                        category = category,
                        merchant = merchant.trim().ifBlank { editing.merchant },
                        note = note.trim(),
                    ),
                )
            }
            onComplete()
        }
    }

    fun delete(transaction: TransactionEntity) {
        viewModelScope.launch { repository.delete(transaction) }
    }

    class Factory(private val repository: TransactionRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            AccountingViewModel(repository) as T
    }
}

private fun Long.toYearMonth(): YearMonth =
    YearMonth.from(Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()))

private fun String.toAmountCents(): Long? = runCatching {
    BigDecimal(trim())
        .setScale(2, RoundingMode.UNNECESSARY)
        .movePointRight(2)
        .longValueExact()
        .takeIf { it > 0 }
}.getOrNull()
