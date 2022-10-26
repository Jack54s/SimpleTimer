package com.jackzone.simpletimer.dialog

import android.app.Activity
import androidx.appcompat.app.AlertDialog
import com.jackzone.simpletimer.R
import com.jackzone.simpletimer.extension.setupDialogStuff
import kotlinx.android.synthetic.main.dialog_my_time_picker.view.*

class MyTimePickerDialog(val activity: Activity, val initialSeconds: Int, val callback: (result: Int) -> Unit) {

    private val view = activity.layoutInflater.inflate(R.layout.dialog_my_time_picker, null)

    init {
        view.apply {
//            val textColor = activity.getProperTextColor()
//            arrayOf(my_time_picker_hours, my_time_picker_minutes, my_time_picker_seconds).forEach {
//                it.textColor = textColor
//                it.selectedTextColor = textColor
//                it.dividerColor = textColor
//            }

            my_time_picker_hours.value = initialSeconds / 3600
            my_time_picker_minutes.value = (initialSeconds) / 60 % 60
            my_time_picker_seconds.value = initialSeconds % 60
        }

        AlertDialog.Builder(activity)
            .setPositiveButton(R.string.ok) { _, _ -> dialogConfirmed() }
            .setNegativeButton(R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(view, this)
            }
    }

    private fun dialogConfirmed() {
        view.apply {
            val hours = my_time_picker_hours.value
            val minutes = my_time_picker_minutes.value
            val seconds = my_time_picker_seconds.value
            callback(hours * 3600 + minutes * 60 + seconds)
        }
    }
}