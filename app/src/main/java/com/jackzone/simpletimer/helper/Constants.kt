package com.jackzone.simpletimer.helper

import android.os.Build
import android.os.Looper
import androidx.annotation.ChecksSdkIntAtLeast

// shared preferences
const val PREFS_KEY = "Prefs"
const val TIMER_SECONDS = "timer_seconds"
const val TIMER_VIBRATE = "timer_vibrate"
const val TIMER_SOUND_URI = "timer_sound_uri"
const val TIMER_SOUND_TITLE = "timer_sound_title"
const val TIMER_CHANNEL_ID = "timer_channel_id"
const val TIMER_LABEL = "timer_label"
const val TIMER_MAX_REMINDER_SECS = "timer_max_reminder_secs"
const val DEFAULT_MAX_TIMER_REMINDER_SECS = 60
const val TIMER_LAST_CONFIG = "timer_last_config"


const val PICK_AUDIO_FILE_INTENT_ID = 10001

const val LOWER_ALPHA = 0.25f
const val MEDIUM_ALPHA = 0.5f
const val HIGHER_ALPHA = 0.75f

const val SILENT = "silent"
const val YOUR_ALARM_SOUNDS = "your_alarm_sounds"

// permissions
const val PERMISSION_READ_STORAGE = 1
const val PERMISSION_WRITE_STORAGE = 2
const val PERMISSION_CAMERA = 3
const val PERMISSION_RECORD_AUDIO = 4
const val PERMISSION_READ_CONTACTS = 5
const val PERMISSION_WRITE_CONTACTS = 6
const val PERMISSION_READ_CALENDAR = 7
const val PERMISSION_WRITE_CALENDAR = 8
const val PERMISSION_CALL_PHONE = 9
const val PERMISSION_READ_CALL_LOG = 10
const val PERMISSION_WRITE_CALL_LOG = 11
const val PERMISSION_GET_ACCOUNTS = 12
const val PERMISSION_READ_SMS = 13
const val PERMISSION_SEND_SMS = 14
const val PERMISSION_READ_PHONE_STATE = 15
const val PERMISSION_MEDIA_LOCATION = 16
const val PERMISSION_POST_NOTIFICATIONS = 17
const val PERMISSION_READ_MEDIA_IMAGES = 18
const val PERMISSION_READ_MEDIA_VIDEO = 19
const val PERMISSION_READ_MEDIA_AUDIO = 20

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.Q)
fun isQPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

fun isOnMainThread() = Looper.myLooper() == Looper.getMainLooper()

fun ensureBackgroundThread(callback: () -> Unit) {
    if (isOnMainThread()) {
        Thread {
            callback()
        }.start()
    } else {
        callback()
    }
}

fun formatTime(showSeconds: Boolean, use24HourFormat: Boolean, hours: Int, minutes: Int, seconds: Int): String {
    val hoursFormat = if (use24HourFormat) "%02d" else "%01d"
    var format = "$hoursFormat:%02d"

    return if (showSeconds) {
        format += ":%02d"
        String.format(format, hours, minutes, seconds)
    } else {
        String.format(format, hours, minutes)
    }
}
