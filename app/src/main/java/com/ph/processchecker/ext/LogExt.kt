package com.ph.processchecker.ext

import android.util.Log

var log = true

inline fun Any.logD(tag: String, messageBuilder: () -> String) {
    val currentTag = tag.ifBlank { this::class.simpleName }
    if (log) {
        Log.d(currentTag, messageBuilder())
    }
}

inline fun Any.logE(tag: String, messageBuilder: () -> String) {
    val currentTag = tag.ifBlank { this::class.simpleName }
    if (log) {
        Log.e(currentTag, messageBuilder())
    }
}
