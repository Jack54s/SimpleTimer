package com.jackzone.simpletimer.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.jackzone.simpletimer.extension.hideTimerNotification
import com.jackzone.simpletimer.helper.INVALID_TIMER_ID
import com.jackzone.simpletimer.helper.TIMER_ID
import com.jackzone.simpletimer.model.TimerEvent
import org.greenrobot.eventbus.EventBus

class HideTimerReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val timerId = intent.getIntExtra(TIMER_ID, INVALID_TIMER_ID)
        context.hideTimerNotification(timerId)
        EventBus.getDefault().post(TimerEvent.Reset(timerId))
    }
}