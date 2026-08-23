package com.yyykblue.accounting.model

enum class TransactionType {
    EXPENSE,
    INCOME,
}

enum class TransactionSource(val displayName: String) {
    MANUAL("手动"),
    ALIPAY("支付宝"),
    WECHAT("微信"),
    BANK("银行卡"),
}

object Categories {
    val expense = listOf("餐饮", "交通", "购物", "居住", "娱乐", "学习", "医疗", "人情", "其他")
    val income = listOf("工资", "奖金", "转账", "退款", "理财", "其他")

    fun forType(type: TransactionType): List<String> =
        if (type == TransactionType.EXPENSE) expense else income
}
