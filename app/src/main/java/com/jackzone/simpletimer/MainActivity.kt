package com.jackzone.simpletimer

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.OpenableColumns
import android.view.WindowManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jackzone.simpletimer.adapter.TimerAdapter
import com.jackzone.simpletimer.dialog.EditTimerDialog
import com.jackzone.simpletimer.extension.config
import com.jackzone.simpletimer.extension.hideKeyboard
import com.jackzone.simpletimer.extension.timerDb
import com.jackzone.simpletimer.helper.DisabledItemChangeAnimator
import com.jackzone.simpletimer.helper.PICK_AUDIO_FILE_INTENT_ID
import com.jackzone.simpletimer.helper.YOUR_ALARM_SOUNDS_MIN_ID
import com.jackzone.simpletimer.model.AlarmSound
import com.jackzone.simpletimer.model.Timer
import com.jackzone.simpletimer.model.TimerEvent
import com.jackzone.simpletimer.model.TimerState
import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File
import java.util.ArrayList

class MainActivity : BaseActivity() {

    private val INVALID_POSITION = -1
    private var currentEditAlarmDialog: EditTimerDialog? = null
    private lateinit var timerAdapter: TimerAdapter
    private var timerPositionToScrollTo = INVALID_POSITION

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        config.appRunCount++
        EventBus.getDefault().register(this)
        setSupportActionBar(tool_bar)
        timer_list.itemAnimator = DisabledItemChangeAnimator()
        add_btn.setOnClickListener {
            hideKeyboard()
            openEditTimer(Timer(
                null,
                config.timerSeconds,
                TimerState.Idle,
                config.timerVibrate,
                config.timerSoundUri,
                config.timerSoundTitle,
                config.timerLabel ?: "",
                config.timerMaxReminderSecs,
                System.currentTimeMillis(),
                config.timerChannelId
            ))
        }
        setting.setOnClickListener {
            startActivity(Intent(this, SettingActivity::class.java))
        }
        timerAdapter = TimerAdapter(this, timer_list, ::openEditTimer)
        timer_list.adapter = timerAdapter
        refreshTimers()

        // the initial timer is created asynchronously at first launch, make sure we show it once created
        if (config?.appRunCount == 1) {
            Handler(Looper.getMainLooper()).postDelayed({
                refreshTimers()
            }, 1000)
        }
    }

    override fun onResume() {
        super.onResume()
        if (config.preventPhoneFromSleeping) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    override fun onPause() {
        super.onPause()
        if (config.preventPhoneFromSleeping) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        if (requestCode == PICK_AUDIO_FILE_INTENT_ID && resultCode == RESULT_OK && resultData != null) {
            storeNewAlarmSound(resultData)
        }
    }

    private fun storeNewAlarmSound(resultData: Intent) {
        val newAlarmSound = storeNewYourAlarmSound(resultData)
        currentEditAlarmDialog?.updateAlarmSound(newAlarmSound)
    }

    private fun storeNewYourAlarmSound(resultData: Intent): AlarmSound {
        val uri = resultData.data
        var filename = getFilenameFromUri(uri!!)
        if (filename.isEmpty()) {
            filename = getString(R.string.alarm)
        }

        val token = object : TypeToken<ArrayList<AlarmSound>>() {}.type
        val yourAlarmSounds = Gson().fromJson<ArrayList<AlarmSound>>(config.yourAlarmSounds, token)
            ?: ArrayList()
        val newAlarmSoundId = (yourAlarmSounds.maxByOrNull { it.id }?.id ?: YOUR_ALARM_SOUNDS_MIN_ID) + 1
        val newAlarmSound = AlarmSound(newAlarmSoundId, filename, uri.toString())
        if (yourAlarmSounds.firstOrNull { it.uri == uri.toString() } == null) {
            yourAlarmSounds.add(newAlarmSound)
        }

        config.yourAlarmSounds = Gson().toJson(yourAlarmSounds)

        val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        contentResolver.takePersistableUriPermission(uri, takeFlags)

        return newAlarmSound
    }

    private fun getFilenameFromUri(uri: Uri): String {
        return if (uri.scheme == "file") {
            File(uri.toString()).name
        } else {
            getFilenameFromContentUri(uri) ?: uri.lastPathSegment ?: ""
        }
    }

    @SuppressLint("Range")
    private fun getFilenameFromContentUri(uri: Uri): String? {
        val projection = arrayOf(
            OpenableColumns.DISPLAY_NAME
        )

        try {
            val cursor = contentResolver.query(uri, projection, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    return it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            }
        } catch (e: Exception) {
        }
        return null
    }

    private fun refreshTimers(scrollToLatest: Boolean = false) {
        timerDb.getTimers { timers ->
            runOnUiThread {
                timerAdapter.submitList(timers) {
                    timer_list.post {
                        if (timerPositionToScrollTo != INVALID_POSITION && timerAdapter.itemCount > timerPositionToScrollTo) {
                            timer_list.scrollToPosition(timerPositionToScrollTo)
                            timerPositionToScrollTo = INVALID_POSITION
                        } else if (scrollToLatest) {
                            timer_list.scrollToPosition(timers.lastIndex)
                        }
                    }
                }
            }
        }
    }

    private fun openEditTimer(timer: Timer) {
        currentEditAlarmDialog = EditTimerDialog(this, timer) {
            currentEditAlarmDialog = null
            refreshTimers()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: TimerEvent.Refresh) {
        refreshTimers()
    }
}