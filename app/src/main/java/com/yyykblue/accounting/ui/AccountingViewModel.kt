package com.yyykblue.accounting.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yyykblue.accounting.data.DailyReminderEntity
import com.yyykblue.accounting.data.TransactionEntity
import com.yyykblue.accounting.data.TransactionRepository
import com.yyykblue.accounting.model.Categories
import com.yyykblue.accounting.model.FinanceCalculator
import com.yyykblue.accounting.model.MoneyAmount
import com.yyykblue.accounting.model.PaymentMethod
import com.yyykblue.accounting.model.TransactionType
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
    val allTransactions: List<TransactionEntity> = emptyList(),
    val customCategories: Map<TransactionType, List<String>> = emptyMap(),
    val merchants: List<String> = emptyList(),
    val reminders: List<DailyReminderEntity> = emptyList(),
) {
    val incomeCents: Long = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amountCents }
    val expenseCents: Long = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amountCents }
    val balanceCents: Long = incomeCents - expenseCents
    val outstandingDebtCents: Long = FinanceCalculator.outstandingDebt(allTransactions)
}

class AccountingViewModel(private val repository: TransactionRepository) : ViewModel() {
    private val selectedMonth = MutableStateFlow(YearMonth.now())

    val uiState: StateFlow<AccountingUiState> = combine(
        repository.transactions,
        repository.customCategories,
        repository.merchants,
        repository.reminders,
        selectedMonth,
    ) { allTransactions, categoryEntities, merchantEntities, reminders, month ->
        AccountingUiState(
            month = month,
            transactions = allTransactions.filter { it.timestamp.toYearMonth() == month },
            allTransactions = allTransactions,
            customCategories = categoryEntities
                .groupBy { it.type }
                .mapValues { (_, values) -> values.map { it.name } },
            merchants = merchantEntities.map { it.name },
            reminders = reminders,
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
        paymentMethod: PaymentMethod,
        editing: TransactionEntity?,
        onComplete: () -> Unit,
    ) {
        val amountCents = MoneyAmount.parseCents(amount) ?: return
        val normalizedCategory = if (type == TransactionType.DEBT_REPAYMENT) {
            "借贷还款"
        } else {
            category.trim().ifBlank { Categories.forType(type).first() }
        }
        val normalizedMerchant = merchant.trim().ifBlank {
            when (type) {
                TransactionType.EXPENSE -> "日常支出"
                TransactionType.INCOME -> "收入"
                TransactionType.DEBT_REPAYMENT -> "借贷还款"
            }
        }
        val normalizedPaymentMethod =
            if (type == TransactionType.EXPENSE) paymentMethod else PaymentMethod.BALANCE
        viewModelScope.launch {
            if (editing == null) {
                repository.add(
                    TransactionEntity(
                        amountCents = amountCents,
                        type = type,
                        category = normalizedCategory,
                        merchant = normalizedMerchant,
                        note = note.trim(),
                        timestamp = System.currentTimeMillis(),
                        paymentMethod = normalizedPaymentMethod,
                    ),
                )
            } else {
                repository.update(
                    editing.copy(
                        amountCents = amountCents,
                        type = type,
                        category = normalizedCategory,
                        merchant = normalizedMerchant,
                        note = note.trim(),
                        paymentMethod = normalizedPaymentMethod,
                    ),
                )
            }
            if (type != TransactionType.DEBT_REPAYMENT && normalizedCategory !in Categories.forType(type)) {
                repository.rememberCategory(normalizedCategory, type)
            }
            if (merchant.isNotBlank()) repository.rememberMerchant(normalizedMerchant)
            onComplete()
        }
    }

    fun delete(transaction: TransactionEntity) {
        viewModelScope.launch { repository.delete(transaction) }
    }

    fun addReminder(title: String) {
        val normalizedTitle = title.trim()
        if (normalizedTitle.isEmpty()) return
        viewModelScope.launch { repository.addReminder(normalizedTitle) }
    }

    fun toggleReminder(reminder: DailyReminderEntity, todayEpochDay: Long) {
        val completedDate = if (reminder.completedEpochDay == todayEpochDay) null else todayEpochDay
        viewModelScope.launch {
            repository.setReminderCompletedDate(reminder.id, completedDate)
        }
    }

    fun deleteReminder(reminder: DailyReminderEntity) {
        viewModelScope.launch { repository.deleteReminder(reminder) }
    }

    class Factory(private val repository: TransactionRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            AccountingViewModel(repository) as T
    }
}

private fun Long.toYearMonth(): YearMonth =
    YearMonth.from(Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()))
