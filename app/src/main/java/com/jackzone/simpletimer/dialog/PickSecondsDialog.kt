package com.jackzone.simpletimer.dialog

import com.jackzone.simpletimer.BaseActivity
import com.jackzone.simpletimer.R
import com.jackzone.simpletimer.extension.getFormattedSeconds
import com.jackzone.simpletimer.extension.hideKeyboard
import com.jackzone.simpletimer.helper.MINUTE_SECONDS
import com.jackzone.simpletimer.model.RadioItem
import java.util.*

class PickSecondsDialog(
    activity: BaseActivity,
    curSeconds: Int,
    cancelCallback: (() -> Unit)? = null,
    callback: (seconds: Int) -> Unit
) {
    init {
        activity.apply {
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
    }
}