package com.ph.processchecker.ext

import android.util.Log

var log = true

inline fun Any.logD(tag: String, messageBuilder: () -> String) {
    if (log) {
        Log.d(tag, messageBuilder())
    }
}

inline fun Any.logE(tag: String, messageBuilder: () -> String) {
    if (log) {
        Log.e(tag, messageBuilder())
    }
}
