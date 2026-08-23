package com.yyykblue.accounting.ui

import java.math.BigDecimal
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

fun Long.asMoney(): String = "¥" + BigDecimal.valueOf(this, 2).toPlainString()

fun Long.asDateTime(): String = Instant.ofEpochMilli(this)
    .atZone(ZoneId.systemDefault())
    .format(DateTimeFormatter.ofPattern("M月d日 HH:mm", Locale.CHINA))
