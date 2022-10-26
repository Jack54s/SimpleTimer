package com.jackzone.simpletimer.extension

import android.app.Activity
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.media.RingtoneManager
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.jackzone.simpletimer.R
import com.jackzone.simpletimer.helper.PERMISSION_READ_STORAGE
import com.jackzone.simpletimer.helper.SILENT
import com.jackzone.simpletimer.model.AlarmSound
import java.util.ArrayList

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