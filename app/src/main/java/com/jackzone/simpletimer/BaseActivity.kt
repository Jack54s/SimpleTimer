package com.jackzone.simpletimer

import android.content.pm.PackageManager
import android.media.RingtoneManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.jackzone.simpletimer.extension.*
import com.jackzone.simpletimer.helper.PERMISSION_READ_STORAGE
import com.jackzone.simpletimer.helper.SILENT
import com.jackzone.simpletimer.model.AlarmSound
import java.util.ArrayList

abstract class BaseActivity : AppCompatActivity() {

    private var isAskingPermissions = false
    private var actionOnPermission: ((granted: Boolean) -> Unit)? = null

    private val GENERIC_PERM_HANDLER = 100

    override fun onDestroy() {
        super.onDestroy()
        actionOnPermission = null
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        isAskingPermissions = false
        if (requestCode == GENERIC_PERM_HANDLER && grantResults.isNotEmpty()) {
            actionOnPermission?.invoke(grantResults[0] == PackageManager.PERMISSION_GRANTED)
        }
    }

    fun getAlarmSounds(type: Int, callback: (ArrayList<AlarmSound>) -> Unit) {
        val alarms = ArrayList<AlarmSound>()
        val manager = RingtoneManager(this)
        manager.setType(type)

        try {
            val cursor = manager.cursor
            var curId = 1
            val silentAlarm = AlarmSound(curId++, getString(R.string.no_sound), SILENT)
            alarms.add(silentAlarm)

            while (cursor.moveToNext()) {
                val title = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX)
                var uri = cursor.getString(RingtoneManager.URI_COLUMN_INDEX)
                val id = cursor.getString(RingtoneManager.ID_COLUMN_INDEX)
                if (!uri.endsWith(id)) {
                    uri += "/$id"
                }

                val alarmSound = AlarmSound(curId++, title, uri)
                alarms.add(alarmSound)
            }
            callback(alarms)
        } catch (e: Exception) {
            if (e is SecurityException) {
                handlePermission(PERMISSION_READ_STORAGE) {
                    if (it) {
                        getAlarmSounds(type, callback)
                    } else {
                        showErrorToast(e)
                        callback(ArrayList())
                    }
                }
            } else {
                showErrorToast(e)
                callback(ArrayList())
            }
        }
    }

    fun handlePermission(permissionId: Int, callback: (granted: Boolean) -> Unit) {
        actionOnPermission = null
        if (hasPermission(permissionId)) {
            callback(true)
        } else {
            isAskingPermissions = true
            actionOnPermission = callback
            ActivityCompat.requestPermissions(this, arrayOf(getPermissionString(permissionId)), GENERIC_PERM_HANDLER)
        }
    }


}