package com.jackzone.simpletimer.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.jackzone.simpletimer.MainActivity
import com.jackzone.simpletimer.R
import com.jackzone.simpletimer.extension.getFormattedDuration
import com.jackzone.simpletimer.extension.timerDb
import com.jackzone.simpletimer.helper.INVALID_TIMER_ID
import com.jackzone.simpletimer.helper.TIMER_ID
import com.jackzone.simpletimer.helper.TIMER_RUNNING_NOTIF_ID
import com.jackzone.simpletimer.helper.isOreoPlus
import com.jackzone.simpletimer.model.TimerEvent
import com.jackzone.simpletimer.model.TimerState
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class TimerService : Service() {
    private val bus = EventBus.getDefault()
    private var isStopping = false

    override fun onCreate() {
        super.onCreate()
        bus.register(this)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        isStopping = false
        updateNotification()
        startForeground(TIMER_RUNNING_NOTIF_ID, notification(getString(R.string.app_name), getString(R.string.timers_notification_msg), INVALID_TIMER_ID))
        return START_NOT_STICKY
    }

    private fun updateNotification() {
        timerDb.getTimers { timers ->
            val runningTimers = timers.filter { it.state is TimerState.Running }
            if (runningTimers.isNotEmpty()) {
                val firstTimer = runningTimers.first()
                val formattedDuration = (firstTimer.state as TimerState.Running).tick.getFormattedDuration()
                val contextText = when {
                    firstTimer.label.isNotEmpty() -> getString(R.string.timer_single_notification_label_msg, firstTimer.label)
                    else -> resources.getQuantityString(R.plurals.timer_notification_msg, runningTimers.size, runningTimers.size)
                }
                startForeground(TIMER_RUNNING_NOTIF_ID, notification(formattedDuration, contextText, firstTimer.id!!))
            } else {
                stopService()
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: TimerStopService) {
        stopService()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: TimerEvent.Refresh) {
        if (!isStopping) {
            updateNotification()
        }
    }

    private fun stopService() {
        isStopping = true
        if (isOreoPlus()) {
            stopForeground(true)
        } else {
            stopSelf()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bus.unregister(this)
    }

    private fun notification(title: String, contentText: String, firstRunningTimerId: Int): Notification {
        val channelId = "simple_timer"
        val label = getString(R.string.timer)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (isOreoPlus()) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            NotificationChannel(channelId, label, importance).apply {
                setSound(null, null)
                notificationManager.createNotificationChannel(this)
            }
        }

        val builder = NotificationCompat.Builder(this)
            .setContentTitle(title)
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_hourglass_vector)
            .setColor(getColor(R.color.color_text))
            .setPriority(Notification.PRIORITY_DEFAULT)
            .setSound(null)
            .setOngoing(true)
            .setAutoCancel(true)
            .setChannelId(channelId)

        if (firstRunningTimerId != INVALID_TIMER_ID) {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra(TIMER_ID, firstRunningTimerId)
            val pi = PendingIntent.getActivity(this, firstRunningTimerId, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            builder.setContentIntent(pi)
        }

        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        return builder.build()
    }
}

fun startTimerService(context: Context) {
    ContextCompat.startForegroundService(context, Intent(context, TimerService::class.java))
}

object TimerStopService
