package com.jackzone.simpletimer.dialog

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AlertDialog
import com.jackzone.simpletimer.R
import com.jackzone.simpletimer.extension.setupDialogStuff
import com.jackzone.simpletimer.model.RadioItem
import kotlinx.android.synthetic.main.dialog_radio_group.view.*

class RadioGroupDialog(
    private val activity: Activity,
    private val items: ArrayList<RadioItem>,
    private val checkedItemId: Int = -1,
    val cancelCallback: (() -> Unit)? = null,
    val callback: (newValue: Any) -> Unit
) {
    private var dialog: AlertDialog? = null
    private var wasInit = false
    private var selectedItemId = -1

    init {
        val view = activity.layoutInflater.inflate(R.layout.dialog_radio_group, null)
        view.dialog_radio_group.apply {
            for (i in 0 until items.size) {
                val radioButton = (activity.layoutInflater.inflate(R.layout.radio_button, null) as RadioButton).apply {
                    text = items[i].title
                    isChecked = items[i].id == checkedItemId
                    id = i
                    setOnClickListener { itemSelected(i) }
                }

                if (items[i].id == checkedItemId) {
                    selectedItemId = i
                }

                addView(radioButton, RadioGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
            }
        }

        val builder = AlertDialog.Builder(activity)
            .setOnCancelListener { cancelCallback?.invoke() }

        if (selectedItemId != -1) {
            builder.setPositiveButton(R.string.ok) { dialog, which -> itemSelected(selectedItemId) }
        }

        builder.apply {
            activity.setupDialogStuff(view, this) { alertDialog ->
                dialog = alertDialog
            }
        }

        if (selectedItemId != -1) {
            view.dialog_radio_holder.apply {
                viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        viewTreeObserver.removeOnGlobalLayoutListener(this)
                        scrollY = view.dialog_radio_group.findViewById<View>(selectedItemId).bottom - height
                    }
                })
            }
        }

        wasInit = true
    }

    private fun itemSelected(checkedId: Int) {
        if (wasInit) {
            callback(items[checkedId].value)
            dialog?.dismiss()
        }
    }
}
