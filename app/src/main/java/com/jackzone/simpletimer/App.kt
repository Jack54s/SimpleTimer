package com.jackzone.simpletimer

import android.app.Application
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.jackzone.simpletimer.extension.*
import com.jackzone.simpletimer.model.TimerEvent
import com.jackzone.simpletimer.model.TimerState
import com.jackzone.simpletimer.service.TimerStopService
import com.jackzone.simpletimer.service.startTimerService
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class App: Application(), LifecycleObserver {

    private var countDownTimers = mutableMapOf<Int, CountDownTimer>()

    override fun onCreate() {
        super.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        EventBus.getDefault().register(this)

//        checkUseEnglish()
    }

    override fun onTerminate() {
        EventBus.getDefault().unregister(this)
        super.onTerminate()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    private fun onAppBackgrounded() {
        timerDb.getTimers { timers ->
            if (timers.any { it.state is TimerState.Running }) {
                startTimerService(this)
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    private fun onAppForegrounded() {
        EventBus.getDefault().post(TimerStopService)
        timerDb.getTimers { timers ->
            val runningTimers = timers.filter { it.state is TimerState.Running }
            runningTimers.forEach { timer ->
                if (countDownTimers[timer.id] == null) {
                    EventBus.getDefault().post(TimerEvent.Start(timer.id!!, (timer.state as TimerState.Running).tick))
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: TimerEvent.Reset) {
        updateTimerState(event.timerId, TimerState.Idle)
        countDownTimers[event.timerId]?.cancel()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: TimerEvent.Delete) {
        countDownTimers[event.timerId]?.cancel()
        timerDb.deleteTimer(event.timerId) {
            EventBus.getDefault().post(TimerEvent.Refresh)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: TimerEvent.Start) {
        val countDownTimer = object : CountDownTimer(event.duration, 1000) {
            override fun onTick(tick: Long) {
                updateTimerState(event.timerId, TimerState.Running(event.duration, tick))
            }

            override fun onFinish() {
                EventBus.getDefault().post(TimerEvent.Finish(event.timerId, event.duration))
                EventBus.getDefault().post(TimerStopService)
            }
        }.start()
        countDownTimers[event.timerId] = countDownTimer
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: TimerEvent.Finish) {
        timerDb.getTimer(event.timerId) { timer ->
            val intent = Intent(this, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(this, event.timerId, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            val notification = getTimerNotification(timer, pendingIntent, false)
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            try {
                notificationManager.notify(event.timerId, notification)
            } catch (e: Exception) {
                showErrorToast(e)
            }

            updateTimerState(event.timerId, TimerState.Finished)
            Handler(Looper.getMainLooper()).postDelayed({
                hideTimerNotification(event.timerId)
            }, config.timerMaxReminderSecs * 1000L)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: TimerEvent.Pause) {
        timerDb.getTimer(event.timerId) { timer ->
            updateTimerState(event.timerId, TimerState.Paused(event.duration, (timer.state as TimerState.Running).tick))
            countDownTimers[event.timerId]?.cancel()
        }
    }

    private fun updateTimerState(timerId: Int, state: TimerState) {
        timerDb.getTimer(timerId) { timer ->
            val newTimer = timer.copy(state = state)
            timerDb.insertOrUpdateTimer(newTimer) {
                EventBus.getDefault().post(TimerEvent.Refresh)
            }
        }
    }
}