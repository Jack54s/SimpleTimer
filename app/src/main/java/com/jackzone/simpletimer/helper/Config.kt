package com.jackzone.simpletimer.helper

import android.content.Context
import android.media.RingtoneManager
import com.jackzone.simpletimer.R
import com.jackzone.simpletimer.extension.gson.gson
import com.jackzone.simpletimer.model.Timer

class Config(val context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)

    companion object {
        fun newInstance(context: Context) = Config(context)
    }

    var timerSeconds: Int
        get() = prefs.getInt(TIMER_SECONDS, 300)
        set(lastTimerSeconds) = prefs.edit().putInt(TIMER_SECONDS, lastTimerSeconds).apply()

    var timerVibrate: Boolean
        get() = prefs.getBoolean(TIMER_VIBRATE, false)
        set(timerVibrate) = prefs.edit().putBoolean(TIMER_VIBRATE, timerVibrate).apply()

    var timerSoundUri: String
        get() = prefs.getString(TIMER_SOUND_URI, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM).toString())!!
        set(timerSoundUri) = prefs.edit().putString(TIMER_SOUND_URI, timerSoundUri).apply()

    var timerSoundTitle: String
        get() = prefs.getString(TIMER_SOUND_TITLE,
            RingtoneManager.getRingtone(context,
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))?.getTitle(context)
                    ?: context.getString(R.string.alarm))!!
        set(timerSoundTitle) = prefs.edit().putString(TIMER_SOUND_TITLE, timerSoundTitle).apply()

    var timerMaxReminderSecs: Int
        get() = prefs.getInt(TIMER_MAX_REMINDER_SECS, DEFAULT_MAX_TIMER_REMINDER_SECS)
        set(timerMaxReminderSecs) = prefs.edit().putInt(TIMER_MAX_REMINDER_SECS, timerMaxReminderSecs).apply()

    var timerLabel: String?
        get() = prefs.getString(TIMER_LABEL, null)
        set(label) = prefs.edit().putString(TIMER_LABEL, label).apply()

    var timerChannelId: String?
        get() = prefs.getString(TIMER_CHANNEL_ID, null)
        set(id) = prefs.edit().putString(TIMER_CHANNEL_ID, id).apply()

    var timerLastConfig: Timer?
        get() = prefs.getString(TIMER_LAST_CONFIG, null)?.let { lastAlarm ->
            gson.fromJson(lastAlarm, Timer::class.java)
        }
        set(alarm) = prefs.edit().putString(TIMER_LAST_CONFIG, gson.toJson(alarm)).apply()

    var yourAlarmSounds: String
        get() = prefs.getString(YOUR_ALARM_SOUNDS, "")!!
        set(yourAlarmSounds) = prefs.edit().putString(YOUR_ALARM_SOUNDS, yourAlarmSounds).apply()
}