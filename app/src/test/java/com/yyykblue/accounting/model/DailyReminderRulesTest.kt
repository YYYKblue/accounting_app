package com.yyykblue.accounting.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DailyReminderRulesTest {
    @Test
    fun `completion applies only to the day it was checked`() {
        val completedDay = 20_692L

        assertTrue(DailyReminderRules.isCompletedOn(completedDay, completedDay))
        assertFalse(DailyReminderRules.isCompletedOn(completedDay, completedDay + 1))
        assertFalse(DailyReminderRules.isCompletedOn(null, completedDay))
    }
}
