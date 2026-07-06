package com.ph.processchecker.module

import android.graphics.drawable.Drawable

data class AppInfo(
    val appName: String,
    val icon: Drawable,
    val packageName: String,
    val lastUseTime: Long = -1L,
)