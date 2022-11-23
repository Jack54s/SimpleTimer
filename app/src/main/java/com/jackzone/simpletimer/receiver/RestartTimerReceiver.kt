package com.jackzone.simpletimer.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.jackzone.simpletimer.extension.hideTimerNotification
import com.jackzone.simpletimer.extension.secondsToMillis
import com.jackzone.simpletimer.helper.INVALID_TIMER_ID
import com.jackzone.simpletimer.helper.TIMER_ID
import com.jackzone.simpletimer.helper.TIMER_SECONDS
import com.jackzone.simpletimer.model.TimerEvent
import com.jackzone.simpletimer.service.startTimerService
import org.greenrobot.eventbus.EventBus

class RestartTimerReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val timerId = intent.getIntExtra(TIMER_ID, INVALID_TIMER_ID)
        val timerSeconds = intent.getIntExtra(TIMER_SECONDS, 300)
        context.hideTimerNotification(timerId)
        EventBus.getDefault().post(TimerEvent.Start(timerId, timerSeconds.secondsToMillis))
    }
}