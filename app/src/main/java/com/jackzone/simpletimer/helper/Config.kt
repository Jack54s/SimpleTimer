package com.jackzone.simpletimer.helper

import android.content.Context
import android.media.RingtoneManager
import android.text.format.DateFormat
import com.jackzone.simpletimer.R
import com.jackzone.simpletimer.extension.gson.gson
import com.jackzone.simpletimer.model.Timer

class Config(val context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)

    companion object {

        private var config: Config? = null

        fun newInstance(context: Context): Config {
            config?.let { return it }
            return Config(context).apply {
                config = this
            }
        }
    }

    var appRunCount: Int
        get() = prefs.getInt(APP_RUN_COUNT, 0)
        set(appRunCount) = prefs.edit().putInt(APP_RUN_COUNT, appRunCount).apply()

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

    var use24HourFormat: Boolean
        get() = prefs.getBoolean(USE_24_HOUR_FORMAT, DateFormat.is24HourFormat(context))
        set(use24HourFormat) = prefs.edit().putBoolean(USE_24_HOUR_FORMAT, use24HourFormat).apply()

    var useEnglish: Boolean
        get() = prefs.getBoolean(USE_ENGLISH, false)
        set(useEnglish) = prefs.edit().putBoolean(USE_ENGLISH, useEnglish).apply()

    var preventPhoneFromSleeping: Boolean
        get() = prefs.getBoolean(PREVENT_PHONE_FROM_SLEEPING, true)
        set(preventPhoneFromSleeping) = prefs.edit().putBoolean(PREVENT_PHONE_FROM_SLEEPING, preventPhoneFromSleeping).apply()

    var timerMaxReminderSecs: Int
        get() = prefs.getInt(TIMER_MAX_REMINDER_SECS, DEFAULT_MAX_TIMER_REMINDER_SECS)
        set(timerMaxReminderSecs) = prefs.edit().putInt(TIMER_MAX_REMINDER_SECS, timerMaxReminderSecs).apply()

}