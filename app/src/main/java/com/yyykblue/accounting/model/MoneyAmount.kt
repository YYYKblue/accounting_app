package com.yyykblue.accounting.model

import java.math.BigDecimal
import java.math.RoundingMode

object MoneyAmount {
    fun parseCents(input: String): Long? = runCatching {
        BigDecimal(input.trim())
            .setScale(2, RoundingMode.UNNECESSARY)
            .movePointRight(2)
            .longValueExact()
            .takeIf { it > 0 }
    }.getOrNull()
}
