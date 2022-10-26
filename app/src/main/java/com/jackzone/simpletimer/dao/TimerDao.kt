package com.jackzone.simpletimer.dao

import androidx.room.*
import com.jackzone.simpletimer.model.Timer

@Dao
interface TimerDao {

    @Query("SELECT * FROM timers ORDER BY createdAt ASC")
    fun getTimers(): List<Timer>

    @Query("SELECT * FROM timers WHERE id=:id")
    fun getTimer(id: Int): Timer

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdateTimer(timer: Timer): Long

    @Query("DELETE FROM timers WHERE id=:id")
    fun deleteTimer(id: Int)

    @Delete
    fun deleteTimers(list: List<Timer>)
}
