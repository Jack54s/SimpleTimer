package com.jackzone.simpletimer.helper

import androidx.room.TypeConverter
import com.jackzone.simpletimer.extension.gson.gson
import com.jackzone.simpletimer.model.StateWrapper
import com.jackzone.simpletimer.model.TimerState

class Converters {

    @TypeConverter
    fun jsonToTimerState(value: String) = gson.fromJson(value, StateWrapper::class.java).state

    @TypeConverter
    fun timerStateToJson(state: TimerState) = gson.toJson(StateWrapper(state))

}