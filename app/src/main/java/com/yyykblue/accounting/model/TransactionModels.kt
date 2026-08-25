package com.yyykblue.accounting.model

enum class TransactionType {
    EXPENSE,
    INCOME,
    DEBT_REPAYMENT,
}

enum class PaymentMethod {
    BALANCE,
    CREDIT,
}

// Kept for compatibility with databases created by version 0.1.0.
// New entries are always MANUAL; the app no longer reads notifications.
enum class TransactionSource {
    MANUAL,
    ALIPAY,
    WECHAT,
    BANK,
}

object Categories {
    val expense = listOf("餐饮", "交通", "购物", "居住", "娱乐", "学习", "医疗", "人情", "其他")
    val income = listOf("工资", "奖金", "转账", "退款", "理财", "其他")

    fun forType(type: TransactionType): List<String> = when (type) {
        TransactionType.EXPENSE -> expense
        TransactionType.INCOME -> income
        TransactionType.DEBT_REPAYMENT -> listOf("借贷还款")
    }
}
