package com.jackzone.simpletimer.extension

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.text.SpannableString
import android.text.format.DateFormat
import android.text.style.RelativeSizeSpan
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.jackzone.simpletimer.BuildConfig.APPLICATION_ID
import com.jackzone.simpletimer.R
import com.jackzone.simpletimer.business.TimerService
import com.jackzone.simpletimer.helper.*
import com.jackzone.simpletimer.model.AlarmSound
import com.jackzone.simpletimer.model.Timer
import com.jackzone.simpletimer.receiver.HideTimerReceiver
import com.jackzone.simpletimer.receiver.RestartTimerReceiver

val Context.config: Config get() = Config.newInstance(applicationContext)
val Context.timerDb: TimerService get() = TimerService(this)

fun Context.getFormattedTime(passedSeconds: Int, showSeconds: Boolean, makeAmPmSmaller: Boolean): SpannableString {
    val use24HourFormat = DateFormat.is24HourFormat(this)
    val hours = (passedSeconds / 3600) % 24
    val minutes = (passedSeconds / 60) % 60
    val seconds = passedSeconds % 60

    return if (use24HourFormat) {
        val formattedTime = formatTime(showSeconds, use24HourFormat, hours, minutes, seconds)
        SpannableString(formattedTime)
    } else {
        val formattedTime = formatTo12HourFormat(showSeconds, hours, minutes, seconds)
        val spannableTime = SpannableString(formattedTime)
        val amPmMultiplier = if (makeAmPmSmaller) 0.4f else 1f
        spannableTime.setSpan(RelativeSizeSpan(amPmMultiplier), spannableTime.length - 3, spannableTime.length, 0)
        spannableTime
    }
}

fun Context.formatTo12HourFormat(showSeconds: Boolean, hours: Int, minutes: Int, seconds: Int): String {
    val appendable = getString(if (hours >= 12) R.string.p_m else R.string.a_m)
    val newHours = if (hours == 0 || hours == 12) 12 else hours % 12
    return "${formatTime(showSeconds, false, newHours, minutes, seconds)} $appendable"
}

fun Context.getDefaultAlarmTitle(type: Int): String {
    val alarmString = getString(R.string.alarm)
    return try {
        RingtoneManager.getRingtone(this, RingtoneManager.getDefaultUri(type))?.getTitle(this) ?: alarmString
    } catch (e: Exception) {
        alarmString
    }
}

fun Context.getDefaultAlarmSound(type: Int) = AlarmSound(0, getDefaultAlarmTitle(type), RingtoneManager.getDefaultUri(type).toString())

fun Context.checkAlarmsWithDeletedSoundUri(uri: String) {
    val defaultAlarmSound = getDefaultAlarmSound(RingtoneManager.TYPE_ALARM)
//    dbHelper.getAlarmsWithUri(uri).forEach {
//        it.soundTitle = defaultAlarmSound.title
//        it.soundUri = defaultAlarmSound.uri
//        dbHelper.updateAlarm(it)
//    }
}
fun Context.hasPermission(permId: Int) = ContextCompat.checkSelfPermission(this, getPermissionString(permId)) == PackageManager.PERMISSION_GRANTED

fun Context.getPermissionString(id: Int) = when (id) {
    PERMISSION_READ_STORAGE -> Manifest.permission.READ_EXTERNAL_STORAGE
    PERMISSION_WRITE_STORAGE -> Manifest.permission.WRITE_EXTERNAL_STORAGE
    PERMISSION_CAMERA -> Manifest.permission.CAMERA
    PERMISSION_RECORD_AUDIO -> Manifest.permission.RECORD_AUDIO
    PERMISSION_READ_CONTACTS -> Manifest.permission.READ_CONTACTS
    PERMISSION_WRITE_CONTACTS -> Manifest.permission.WRITE_CONTACTS
    PERMISSION_READ_CALENDAR -> Manifest.permission.READ_CALENDAR
    PERMISSION_WRITE_CALENDAR -> Manifest.permission.WRITE_CALENDAR
    PERMISSION_CALL_PHONE -> Manifest.permission.CALL_PHONE
    PERMISSION_READ_CALL_LOG -> Manifest.permission.READ_CALL_LOG
    PERMISSION_WRITE_CALL_LOG -> Manifest.permission.WRITE_CALL_LOG
    PERMISSION_GET_ACCOUNTS -> Manifest.permission.GET_ACCOUNTS
    PERMISSION_READ_SMS -> Manifest.permission.READ_SMS
    PERMISSION_SEND_SMS -> Manifest.permission.SEND_SMS
    PERMISSION_READ_PHONE_STATE -> Manifest.permission.READ_PHONE_STATE
    PERMISSION_MEDIA_LOCATION -> if (isQPlus()) Manifest.permission.ACCESS_MEDIA_LOCATION else ""
    PERMISSION_POST_NOTIFICATIONS -> Manifest.permission.POST_NOTIFICATIONS
    PERMISSION_READ_MEDIA_IMAGES -> Manifest.permission.READ_MEDIA_IMAGES
    PERMISSION_READ_MEDIA_VIDEO -> Manifest.permission.READ_MEDIA_VIDEO
    PERMISSION_READ_MEDIA_AUDIO -> Manifest.permission.READ_MEDIA_AUDIO
    else -> ""
}

fun Context.toast(id: Int, length: Int = Toast.LENGTH_SHORT) {
    toast(getString(id), length)
}

fun Context.toast(msg: String, length: Int = Toast.LENGTH_SHORT) {
    try {
        if (isOnMainThread()) {
            doToast(this, msg, length)
        } else {
            Handler(Looper.getMainLooper()).post {
                doToast(this, msg, length)
            }
        }
    } catch (e: Exception) {
    }
}

private fun doToast(context: Context, message: String, length: Int) {
    if (context is Activity) {
        if (!context.isFinishing && !context.isDestroyed) {
            Toast.makeText(context, message, length).show()
        }
    } else {
        Toast.makeText(context, message, length).show()
    }
}

fun Context.showErrorToast(msg: String, length: Int = Toast.LENGTH_LONG) {
    toast(String.format(getString(R.string.error), msg), length)
}

fun Context.showErrorToast(exception: Exception, length: Int = Toast.LENGTH_LONG) {
    showErrorToast(exception.toString(), length)
}

fun Context.grantReadUriPermission(uriString: String) {
    try {
        // ensure custom reminder sounds play well
        grantUriPermission("com.android.systemui", Uri.parse(uriString), Intent.FLAG_GRANT_READ_URI_PERMISSION)
    } catch (ignored: Exception) {
    }
}

fun Context.getHideTimerPendingIntent(timerId: Int): PendingIntent {
    val intent = Intent(this, HideTimerReceiver::class.java)
    intent.putExtra(TIMER_ID, timerId)
    return PendingIntent.getBroadcast(this, timerId, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
}

fun Context.getRestartTimerPendingIntent(timerId: Int, seconds: Int): PendingIntent {
    val intent = Intent(this, RestartTimerReceiver::class.java)
    intent.putExtra(TIMER_ID, timerId)
    intent.putExtra(TIMER_SECONDS, seconds)
    return PendingIntent.getBroadcast(this, timerId, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
}

fun Context.getTimerNotification(timer: Timer, pendingIntent: PendingIntent): Notification {
    var soundUri = timer.soundUri
    if (soundUri == SILENT) {
        soundUri = ""
    } else {
        grantReadUriPermission(soundUri)
    }

    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val channelId = timer.channelId ?: "simple_timer_channel_${soundUri}_${System.currentTimeMillis()}"
    timerDb.insertOrUpdateTimer(timer.copy(channelId = channelId))

    if (isOreoPlus()) {
        try {
            notificationManager.deleteNotificationChannel(channelId)
        } catch (e: Exception) {
        }

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setLegacyStreamType(AudioManager.STREAM_ALARM)
            .build()

        val name = getString(R.string.timer)
        val importance = NotificationManager.IMPORTANCE_HIGH
        NotificationChannel(channelId, name, importance).apply {
            setBypassDnd(true)
            enableLights(true)
            setSound(Uri.parse(soundUri), audioAttributes)

            if (!timer.vibrate) {
                vibrationPattern = longArrayOf(0L)
            }

            enableVibration(timer.vibrate)
            notificationManager.createNotificationChannel(this)
        }
    }

    val title = timer.label.ifEmpty {
        getString(R.string.timer)
    }

    val builder = NotificationCompat.Builder(this, channelId)
        .setContentTitle(title)
        .setContentText(getString(R.string.time_expired))
        .setSmallIcon(R.drawable.ic_hourglass_vector)
        .setContentIntent(pendingIntent)
        .setPriority(NotificationCompat.PRIORITY_MAX)
        .setDefaults(Notification.DEFAULT_LIGHTS)
        .setCategory(Notification.CATEGORY_EVENT)
        .setAutoCancel(true)
        .setSound(Uri.parse(soundUri), AudioManager.STREAM_ALARM)
        .addAction(
            R.drawable.ic_cross_vector,
            getString(R.string.dismiss),
            getHideTimerPendingIntent(timer.id!!)
        )
        .addAction(
            R.drawable.ic_reset_vector,
            getString(R.string.restart),
            getRestartTimerPendingIntent(timer.id!!, timer.seconds)
        )

    builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

    if (timer.vibrate) {
        val vibrateArray = LongArray(2) { 500 }
        builder.setVibrate(vibrateArray)
    }

    val notification = builder.build()
    notification.flags = notification.flags or Notification.FLAG_INSISTENT
    return notification
}

fun Context.getSilentTimerNotification(timer: Timer, pendingIntent: PendingIntent): Notification {
    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val name = getString(R.string.timer)
    if (isOreoPlus()) {
        NotificationChannel(SILENT, name, NotificationManager.IMPORTANCE_LOW).apply {
            setBypassDnd(false)
            enableLights(false)
            setSound(null, null)
            vibrationPattern = longArrayOf(0L)
            enableVibration(false)
            notificationManager.createNotificationChannel(this)
        }
    }

    return NotificationCompat.Builder(this, SILENT)
        .setContentTitle(timer.label.ifEmpty { name })
        .setContentText(getString(R.string.time_expired))
        .setSmallIcon(R.drawable.ic_hourglass_vector)
        .setContentIntent(pendingIntent)
        .setPriority(NotificationCompat.PRIORITY_MAX)
        .setDefaults(Notification.DEFAULT_LIGHTS)
        .setCategory(Notification.CATEGORY_EVENT)
        .setAutoCancel(true)
        .addAction(
            R.drawable.ic_cross_vector,
            getString(R.string.dismiss),
            getHideTimerPendingIntent(timer.id!!)
        )
        .addAction(
            R.drawable.ic_reset_vector,
            getString(R.string.restart),
            getRestartTimerPendingIntent(timer.id!!, timer.seconds)
        )
        .build()
}

fun Context.hideTimerNotification(id: Int) {
    val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    manager.cancel(id)
}

fun Context.formatSecondsToTimeString(totalSeconds: Int): String {
    val days = totalSeconds / DAY_SECONDS
    val hours = (totalSeconds % DAY_SECONDS) / HOUR_SECONDS
    val minutes = (totalSeconds % HOUR_SECONDS) / MINUTE_SECONDS
    val seconds = totalSeconds % MINUTE_SECONDS
    val timesString = StringBuilder()
    if (days > 0) {
        val daysString = String.format(resources.getQuantityString(R.plurals.days, days, days))
        timesString.append("$daysString, ")
    }

    if (hours > 0) {
        val hoursString = String.format(resources.getQuantityString(R.plurals.hours, hours, hours))
        timesString.append("$hoursString, ")
    }

    if (minutes > 0) {
        val minutesString = String.format(resources.getQuantityString(R.plurals.minutes, minutes, minutes))
        timesString.append("$minutesString, ")
    }

    if (seconds > 0) {
        val secondsString = String.format(resources.getQuantityString(R.plurals.seconds, seconds, seconds))
        timesString.append(secondsString)
    }

    var result = timesString.toString().trim().trimEnd(',')
    if (result.isEmpty()) {
        result = String.format(resources.getQuantityString(R.plurals.minutes, 0, 0))
    }
    return result
}

fun Context.getFormattedSeconds(seconds: Int) = when {
    seconds < 0 && seconds > -60 * 60 * 24 -> {
        val minutes = -seconds / 60
        getString(R.string.during_day_at).format(minutes / 60, minutes % 60)
    }
    seconds % YEAR_SECONDS == 0 -> {
        val base = R.plurals.by_years
        resources.getQuantityString(base, seconds / YEAR_SECONDS, seconds / YEAR_SECONDS)
    }
    seconds % MONTH_SECONDS == 0 -> {
        val base = R.plurals.by_months
        resources.getQuantityString(base, seconds / MONTH_SECONDS, seconds / MONTH_SECONDS)
    }
    seconds % WEEK_SECONDS == 0 -> {
        val base = R.plurals.by_weeks
        resources.getQuantityString(base, seconds / WEEK_SECONDS, seconds / WEEK_SECONDS)
    }
    seconds % DAY_SECONDS == 0 -> {
        val base = R.plurals.by_days
        resources.getQuantityString(base, seconds / DAY_SECONDS, seconds / DAY_SECONDS)
    }
    seconds % HOUR_SECONDS == 0 -> {
        val base = R.plurals.by_hours
        resources.getQuantityString(base, seconds / HOUR_SECONDS, seconds / HOUR_SECONDS)
    }
    seconds % MINUTE_SECONDS == 0 -> {
        val base = R.plurals.by_minutes
        resources.getQuantityString(base, seconds / MINUTE_SECONDS, seconds / MINUTE_SECONDS)
    }
    else -> {
        val base = R.plurals.by_seconds
        resources.getQuantityString(base, seconds, seconds)
    }
}

fun Context.getLaunchIntent() = packageManager.getLaunchIntentForPackage(APPLICATION_ID)

fun Context.hasNotification(id: Int): Boolean {
    val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    for (notification in manager.activeNotifications) {
        if (notification.id == id) return true
    }
    return false
}