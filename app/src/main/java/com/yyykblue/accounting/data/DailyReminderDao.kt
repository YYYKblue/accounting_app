package com.yyykblue.accounting.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyReminderDao {
    @Query("SELECT * FROM daily_reminders ORDER BY id ASC")
    fun observeAll(): Flow<List<DailyReminderEntity>>

    @Insert
    suspend fun insert(reminder: DailyReminderEntity)

    @Query("UPDATE daily_reminders SET completedEpochDay = :completedEpochDay WHERE id = :id")
    suspend fun setCompletedDate(id: Long, completedEpochDay: Long?)

    @Delete
    suspend fun delete(reminder: DailyReminderEntity)
}
