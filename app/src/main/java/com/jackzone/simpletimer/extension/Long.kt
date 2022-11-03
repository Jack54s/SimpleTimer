package com.jackzone.simpletimer.extension

import kotlin.math.roundToInt

fun Long.getFormattedDuration(forceShowHours: Boolean = false): String {
    return this.div(1000F).roundToInt().getFormattedDuration(forceShowHours)
}
