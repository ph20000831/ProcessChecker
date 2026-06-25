package com.ph.processchecker.ext

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

fun Long.getDate(pattern: String = "yyyy/MM/dd\nHH:mm:ss"): String {
    return SimpleDateFormat(pattern, Locale.getDefault()).format(this)
}

fun isSleepTime(): Boolean {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return hour in 0..7
}