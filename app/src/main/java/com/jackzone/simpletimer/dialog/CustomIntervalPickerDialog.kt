package com.jackzone.simpletimer.dialog

import android.app.Activity
import android.content.DialogInterface
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import com.jackzone.simpletimer.R
import com.jackzone.simpletimer.extension.beVisibleIf
import com.jackzone.simpletimer.extension.hideKeyboard
import com.jackzone.simpletimer.extension.setupDialogStuff
import com.jackzone.simpletimer.extension.showKeyboard
import com.jackzone.simpletimer.helper.DAY_SECONDS
import com.jackzone.simpletimer.helper.HOUR_SECONDS
import com.jackzone.simpletimer.helper.MINUTE_SECONDS
import kotlinx.android.synthetic.main.dialog_custom_interval_picker.view.*

class CustomIntervalPickerDialog(
    private val activity: Activity,
    private val selectedSeconds: Int = 0,
    val callback: (minutes: Int) -> Unit
) {
    private var dialog: AlertDialog? = null
    private var view = (activity.layoutInflater.inflate(R.layout.dialog_custom_interval_picker, null) as ViewGroup)

    init {
        AlertDialog.Builder(activity)
            .setPositiveButton(R.string.ok) { _, _ -> confirmReminder() }
            .setNegativeButton(R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(view, this) { alertDialog ->
                    dialog = alertDialog
                    alertDialog.showKeyboard(view.findViewById(R.id.dialog_custom_interval_value))
                }
            }

        view.apply {
            when {
                selectedSeconds == 0 -> dialog_radio_view.check(R.id.dialog_radio_minutes)
                selectedSeconds % DAY_SECONDS == 0 -> {
                    dialog_radio_view.check(R.id.dialog_radio_days)
                    dialog_custom_interval_value.setText((selectedSeconds / DAY_SECONDS).toString())
                }
                selectedSeconds % HOUR_SECONDS == 0 -> {
                    dialog_radio_view.check(R.id.dialog_radio_hours)
                    dialog_custom_interval_value.setText((selectedSeconds / HOUR_SECONDS).toString())
                }
                selectedSeconds % MINUTE_SECONDS == 0 -> {
                    dialog_radio_view.check(R.id.dialog_radio_minutes)
                    dialog_custom_interval_value.setText((selectedSeconds / MINUTE_SECONDS).toString())
                }
                else -> {
                    dialog_radio_view.check(R.id.dialog_radio_seconds)
                    dialog_custom_interval_value.setText(selectedSeconds.toString())
                }
            }

            dialog_custom_interval_value.setOnKeyListener(object : View.OnKeyListener {
                override fun onKey(v: View?, keyCode: Int, event: KeyEvent): Boolean {
                    if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                        dialog?.getButton(DialogInterface.BUTTON_POSITIVE)?.performClick()
                        return true
                    }

                    return false
                }
            })
        }
    }

    private fun confirmReminder() {
        val value = view.dialog_custom_interval_value.text.toString().trim()
        val multiplier = getMultiplier(view.dialog_radio_view.checkedRadioButtonId)
        val minutes = Integer.valueOf(value.ifEmpty { "0" })
        callback(minutes * multiplier)
        activity.hideKeyboard()
        dialog?.dismiss()
    }

    private fun getMultiplier(id: Int) = when (id) {
        R.id.dialog_radio_days -> DAY_SECONDS
        R.id.dialog_radio_hours -> HOUR_SECONDS
        R.id.dialog_radio_minutes -> MINUTE_SECONDS
        else -> 1
    }
}
