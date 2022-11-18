package com.jackzone.simpletimer.extension

import android.app.Activity
import android.content.Context
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import com.jackzone.simpletimer.R
import com.jackzone.simpletimer.dialog.CustomIntervalPickerDialog
import com.jackzone.simpletimer.dialog.RadioGroupDialog
import com.jackzone.simpletimer.helper.MINUTE_SECONDS
import com.jackzone.simpletimer.helper.isOnMainThread
import com.jackzone.simpletimer.model.RadioItem
import java.util.*

fun Activity.setupDialogStuff(view: View, dialog: AlertDialog.Builder, callback: ((dialog: AlertDialog) -> Unit)? = null) {
    dialog.create().apply {
        setView(view)
        show()
        val textColor = resources.getColor(R.color.color_text)
        getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(textColor)
        getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(textColor)

        val bgDrawable = resources.let {
            it.getDrawable(R.drawable.dialog_bg, null).apply {
                mutate().colorFilter = BlendModeColorFilter(it.getColor(R.color.color_bg), BlendMode.SRC_IN)
                mutate().alpha = 255
            }
        }
        window?.setBackgroundDrawable(bgDrawable)

        callback?.invoke(this)
    }
}

fun Activity.hideKeyboard() {
    if (isOnMainThread()) {
        hideKeyboardSync()
    } else {
        Handler(Looper.getMainLooper()).post {
            hideKeyboardSync()
        }
    }
}

fun Activity.hideKeyboardSync() {
    val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow((currentFocus ?: View(this)).windowToken, 0)
    window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    currentFocus?.clearFocus()
}

fun Activity.showPickSecondsDialog(curSeconds: Int, cancelCallback: (() -> Unit)? = null, callback: (seconds: Int) -> Unit) {
    hideKeyboard()
    val seconds = TreeSet<Int>()
    seconds.apply {
        add(1 * MINUTE_SECONDS)
        add(5 * MINUTE_SECONDS)
        add(10 * MINUTE_SECONDS)
        add(30 * MINUTE_SECONDS)
        add(60 * MINUTE_SECONDS)
        add(curSeconds)
    }

    val items = ArrayList<RadioItem>(seconds.size + 1)
    seconds.mapIndexedTo(items) { index, value ->
        RadioItem(index, getFormattedSeconds(value), value)
    }

    var selectedIndex = 0
    seconds.forEachIndexed { index, value ->
        if (value == curSeconds) {
            selectedIndex = index
        }
    }

    items.add(RadioItem(-2, getString(R.string.custom)))

    RadioGroupDialog(this, items, selectedIndex, cancelCallback) { option ->
        when (option) {
            -2 -> {
                CustomIntervalPickerDialog(this) {
                    callback(it)
                }
            }
            else -> {
                callback(option as Int)
            }
        }
    }
}
