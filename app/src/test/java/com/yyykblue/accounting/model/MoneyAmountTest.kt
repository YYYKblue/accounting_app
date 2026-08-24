package com.yyykblue.accounting.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MoneyAmountTest {
    @Test
    fun `converts yuan input to integer cents`() {
        assertEquals(3_500L, MoneyAmount.parseCents("35"))
        assertEquals(3_550L, MoneyAmount.parseCents("35.5"))
        assertEquals(3_555L, MoneyAmount.parseCents("35.55"))
    }

    @Test
    fun `trims surrounding whitespace`() {
        assertEquals(12_300L, MoneyAmount.parseCents(" 123.00 "))
    }

    @Test
    fun `rejects zero and negative amounts`() {
        assertNull(MoneyAmount.parseCents("0"))
        assertNull(MoneyAmount.parseCents("-1"))
    }

    @Test
    fun `rejects malformed input and more than two decimals`() {
        assertNull(MoneyAmount.parseCents(""))
        assertNull(MoneyAmount.parseCents("abc"))
        assertNull(MoneyAmount.parseCents("1.001"))
    }
}
