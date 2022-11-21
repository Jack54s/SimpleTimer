package com.jackzone.simpletimer.extension

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import com.jackzone.simpletimer.R
import com.jackzone.simpletimer.helper.isOnMainThread

fun Activity.setupDialogStuff(view: View, dialog: AlertDialog.Builder, callback: ((dialog: AlertDialog) -> Unit)? = null) {
    dialog.create().apply {
        setView(view)
        show()
        val textColor = resources.getColor(R.color.color_text)
        getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(textColor)
        getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(textColor)

        val bgDrawable = when (Configuration.UI_MODE_NIGHT_YES) {
            (applicationContext.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) -> resources.getDrawable(R.drawable.black_dialog_background, theme)
            else -> resources.getDrawable(R.drawable.dialog_bg, theme).apply {
                mutate().colorFilter = BlendModeColorFilter(resources.getColor(R.color.color_bg), BlendMode.SRC_IN)
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