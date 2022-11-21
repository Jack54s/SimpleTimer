package com.jackzone.simpletimer.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.jackzone.simpletimer.helper.DEFAULT_MAX_TIMER_REMINDER_SECS

@Entity(tableName = "timers")
data class Timer(
    @PrimaryKey(autoGenerate = true) var id: Int?,
    var seconds: Int,
    val state: TimerState,
    var vibrate: Boolean,
    var soundUri: String,
    var soundTitle: String,
    var label: String,
    var maxReminderDuration: Int = DEFAULT_MAX_TIMER_REMINDER_SECS,
    var createdAt: Long,
    var channelId: String? = null,
)
