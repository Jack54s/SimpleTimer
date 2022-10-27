package com.jackzone.simpletimer

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.room.util.CursorUtil
import androidx.room.util.CursorUtil.getColumnIndex
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jackzone.simpletimer.dialog.EditTimerDialog
import com.jackzone.simpletimer.extension.config
import com.jackzone.simpletimer.helper.Config
import com.jackzone.simpletimer.helper.PICK_AUDIO_FILE_INTENT_ID
import com.jackzone.simpletimer.helper.YOUR_ALARM_SOUNDS_MIN_ID
import com.jackzone.simpletimer.model.AlarmSound
import com.jackzone.simpletimer.model.Timer
import com.jackzone.simpletimer.model.TimerState
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.util.ArrayList

class MainActivity : BaseActivity() {

    private var currentEditAlarmDialog: EditTimerDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(tool_bar)
        addBtn.setOnClickListener {
            currentEditAlarmDialog = EditTimerDialog(this, Timer(
                null,
                config.timerSeconds,
                TimerState.Idle,
                config.timerVibrate,
                config.timerSoundUri,
                config.timerSoundTitle,
                config.timerLabel ?: "",
                System.currentTimeMillis(),
                config.timerChannelId
            )) {
                currentEditAlarmDialog = null
                refreshTimers()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            //TODO
            R.id.setting -> Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show()
        }
        return true
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
//        activity?.timerHelper?.getTimers { timers ->
//            activity?.runOnUiThread {
//                timerAdapter.submitList(timers) {
//                    getView()?.post {
//                        if (timerPositionToScrollTo != INVALID_POSITION && timerAdapter.itemCount > timerPositionToScrollTo) {
//                            view.timers_list.scrollToPosition(timerPositionToScrollTo)
//                            timerPositionToScrollTo = INVALID_POSITION
//                        } else if (scrollToLatest) {
//                            view.timers_list.scrollToPosition(timers.lastIndex)
//                        }
//                    }
//                }
//            }
//        }
    }
}