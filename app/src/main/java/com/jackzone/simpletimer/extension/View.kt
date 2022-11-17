package com.jackzone.simpletimer.extension

import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE

fun View.show() {
    visibility = VISIBLE
}

fun View.hide() {
    visibility = GONE
}

fun View.toggle(visible: Boolean) {
    if (visible) show()
    else hide()
}
