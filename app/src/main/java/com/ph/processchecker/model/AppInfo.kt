package com.ph.processchecker.model

import android.graphics.drawable.Drawable

data class AppInfo(
    val appName: String,
    val icon: Drawable,
    val packageName: String,
    val isRunning: Boolean = true,
    val lastUseTime: Long = -1L,
)