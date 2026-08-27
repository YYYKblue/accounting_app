package com.yyykblue.accounting.model

object DailyReminderRules {
    fun isCompletedOn(completedEpochDay: Long?, epochDay: Long): Boolean =
        completedEpochDay == epochDay
}
