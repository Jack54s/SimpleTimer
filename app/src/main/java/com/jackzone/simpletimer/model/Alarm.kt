package com.jackzone.simpletimer.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class Alarm(
    @PrimaryKey(autoGenerate = true) var id: Int?,
    var timeInMinutes: Int,
    var days: Int,
    var isEnabled: Boolean,
    var vibrate: Boolean,
    var soundTitle: String,
    var soundUri: String,
    var label: String
)
