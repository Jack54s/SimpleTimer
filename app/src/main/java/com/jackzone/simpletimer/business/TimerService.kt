package com.jackzone.simpletimer.business

import android.content.Context
import com.jackzone.simpletimer.database.AppDatabase
import com.jackzone.simpletimer.helper.ensureBackgroundThread
import com.jackzone.simpletimer.model.Timer

class TimerService(val context: Context) {

    private val timerDao = AppDatabase.getInstance(context).TimerDao()

    fun getTimers(callback: (timers: List<Timer>) -> Unit) {
        ensureBackgroundThread {
            callback.invoke(timerDao.getTimers())
        }
    }

    fun getTimer(timerId: Int, callback: (timer: Timer) -> Unit) {
        ensureBackgroundThread {
            callback.invoke(timerDao.getTimer(timerId))
        }
    }

    fun insertOrUpdateTimer(timer: Timer, callback: () -> Unit = {}) {
        ensureBackgroundThread {
            timerDao.insertOrUpdateTimer(timer)
            callback.invoke()
        }
    }

    fun deleteTimer(id: Int, callback: () -> Unit = {}) {
        ensureBackgroundThread {
            timerDao.deleteTimer(id)
            callback.invoke()
        }
    }

    fun deleteTimers(timers: List<Timer>, callback: () -> Unit = {}) {
        ensureBackgroundThread {
            timerDao.deleteTimers(timers)
            callback.invoke()
        }
    }

}