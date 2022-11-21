package com.jackzone.simpletimer.dialog

import android.media.AudioManager
import android.media.RingtoneManager
import androidx.appcompat.app.AlertDialog
import com.jackzone.simpletimer.BaseActivity
import com.jackzone.simpletimer.R
import com.jackzone.simpletimer.extension.*
import com.jackzone.simpletimer.helper.DEFAULT_MAX_TIMER_REMINDER_SECS
import com.jackzone.simpletimer.helper.PICK_AUDIO_FILE_INTENT_ID
import com.jackzone.simpletimer.model.AlarmSound
import com.jackzone.simpletimer.model.Timer
import kotlinx.android.synthetic.main.dialog_edit_timer.view.*

class EditTimerDialog(private val activity: BaseActivity, val timer: Timer, val callback: () -> Unit) {

    private val view = activity.layoutInflater.inflate(R.layout.dialog_edit_timer, null)
    private val timerDb = activity.timerDb
    private val tempTimer: Timer

    init {
        restoreLastAlarm()
        tempTimer = Timer(
            timer.id,
            timer.seconds,
            timer.state,
            timer.vibrate,
            timer.soundUri,
            timer.soundTitle,
            timer.label,
            timer.maxReminderDuration,
            timer.createdAt,
            timer.channelId
        )
        updateAlarmTime()

        view.apply {
            edit_timer_initial_time.text = tempTimer.seconds.getFormattedDuration()
            edit_timer_initial_time.setOnClickListener {
                changeDuration(tempTimer)
            }

            edit_timer_vibrate.isChecked = tempTimer.vibrate
            edit_timer_vibrate_holder.setOnClickListener {
                edit_timer_vibrate.toggle()
                tempTimer.channelId = null
            }

            edit_timer_sound.text = tempTimer.soundTitle
            edit_timer_sound.setOnClickListener {
                SelectAlarmSoundDialog(activity, tempTimer.soundUri, AudioManager.STREAM_ALARM, PICK_AUDIO_FILE_INTENT_ID,
                    RingtoneManager.TYPE_ALARM, true,
                    onAlarmPicked = { sound ->
                        if (sound != null) {
                            updateAlarmSound(sound)
                        }
                    },
                    onAlarmSoundDeleted = { sound ->
                        if (tempTimer.soundUri == sound.uri) {
                            val defaultAlarm = context.getDefaultAlarmSound(RingtoneManager.TYPE_ALARM)
                            updateAlarmSound(defaultAlarm)
                        }
                    })
            }

            edit_timer.setText(tempTimer.label)

            updateTimerMaxReminderText()
            edit_timer_reminder_duration_holder.setOnClickListener {
                PickSecondsDialog(activity, tempTimer.maxReminderDuration) {
                    tempTimer.maxReminderDuration = if (it != 0) it else DEFAULT_MAX_TIMER_REMINDER_SECS
                    updateTimerMaxReminderText()
                }
            }
        }
        AlertDialog.Builder(activity)
            .setPositiveButton(R.string.ok) { dialog, _ ->
                timer.seconds = tempTimer.seconds
                timer.vibrate = view.edit_timer_vibrate.isChecked
                timer.soundUri = tempTimer.soundUri
                timer.soundTitle = tempTimer.soundTitle
                timer.label = view.edit_timer.text.toString().trim()
                timer.maxReminderDuration = tempTimer.maxReminderDuration
                timer.channelId = tempTimer.channelId
                timerDb.insertOrUpdateTimer(timer) {
                    activity.config.timerLastConfig = timer
                    callback()
                    dialog.dismiss()
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(view, this)
            }
    }

    private fun changeDuration(timer: Timer) {
        MyTimePickerDialog(activity, timer.seconds) { seconds ->
            val timerSeconds = if (seconds <= 0) 10 else seconds
            timer.seconds = timerSeconds
            view.edit_timer_initial_time.text = timerSeconds.getFormattedDuration()
        }
    }

    private fun restoreLastAlarm() {
        if (timer.id == null) {
            activity.config.timerLastConfig?.let { lastConfig ->
                timer.seconds = lastConfig.seconds
                timer.vibrate = lastConfig.vibrate
                timer.soundTitle = lastConfig.soundTitle
                timer.soundUri = lastConfig.soundUri
            }
        }
    }

    private fun updateAlarmTime() {
        view.edit_timer_initial_time.text = activity.getFormattedTime(tempTimer.seconds * 60, false, true)
    }

    fun updateAlarmSound(alarmSound: AlarmSound) {
        tempTimer.soundTitle = alarmSound.title
        tempTimer.soundUri = alarmSound.uri
        tempTimer.channelId = null
        view.edit_timer_sound.text = alarmSound.title
    }

    private fun updateTimerMaxReminderText() {
        view.edit_timer_reminder_duration.text = activity.formatSecondsToTimeString(tempTimer.maxReminderDuration)
    }
}