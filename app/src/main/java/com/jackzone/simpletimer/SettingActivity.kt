package com.jackzone.simpletimer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import com.jackzone.simpletimer.extension.config
import com.jackzone.simpletimer.extension.formatSecondsToTimeString
import com.jackzone.simpletimer.extension.showPickSecondsDialog
import com.jackzone.simpletimer.helper.DEFAULT_MAX_TIMER_REMINDER_SECS
import kotlinx.android.synthetic.main.activity_setting.*
import kotlin.system.exitProcess

class SettingActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        setSupportActionBar(setting_toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setupPreventPhoneFromSleeping()
        setupTimerMaxReminder()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupPreventPhoneFromSleeping() {
        setting_prevent_phone_from_sleeping.isChecked = config.preventPhoneFromSleeping
        setting_prevent_phone_from_sleeping_holder.setOnClickListener {
            setting_prevent_phone_from_sleeping.toggle()
            config.preventPhoneFromSleeping = setting_prevent_phone_from_sleeping.isChecked
        }
    }

    private fun setupTimerMaxReminder() {
        updateTimerMaxReminderText()
        setting_timer_max_reminder_holder.setOnClickListener {
            showPickSecondsDialog(config.timerMaxReminderSecs) {
                config.timerMaxReminderSecs = if (it != 0) it else DEFAULT_MAX_TIMER_REMINDER_SECS
                updateTimerMaxReminderText()
            }
        }
    }

    private fun updateTimerMaxReminderText() {
        setting_timer_max_reminder.text = formatSecondsToTimeString(config.timerMaxReminderSecs)
    }
}