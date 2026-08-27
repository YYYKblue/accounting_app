package com.yyykblue.accounting.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_reminders")
data class DailyReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val completedEpochDay: Long? = null,
)
