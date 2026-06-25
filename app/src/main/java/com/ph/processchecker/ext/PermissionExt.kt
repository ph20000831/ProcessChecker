package com.ph.processchecker.ext

import android.app.Activity
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.os.Process

/**
 * 是否具有PACKAGE_USAGE_STATS权限
 */
fun Context.hasUsageStatsPermission(): Boolean {
    val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    val mode = appOps.checkOpNoThrow(
        AppOpsManager.OPSTR_GET_USAGE_STATS,
        Process.myUid(),
        packageName
    )
    return mode == AppOpsManager.MODE_ALLOWED
}

private val handler = Handler(Looper.getMainLooper())
private const val checkInterval = 150L // 150ms间隔
private var permissionCheckRunnable: Runnable? = null

/**
 * 权限授予自动返回
 */
fun Context.startListenPermissionGranted(
    backActivity: Class<out Activity>,
    clear: Boolean = false
) {
    stopListenPermissionGranted()
    permissionCheckRunnable = object : Runnable {
        override fun run() {
            val isGranted = hasUsageStatsPermission()
            if (isGranted) {
                stopListenPermissionGranted()
                val intent = Intent(this@startListenPermissionGranted, backActivity).apply {
                    flags = if (clear) {
                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    } else {
                        Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                }
                startActivity(intent)
            } else {
                // 继续监听
                handler.postDelayed(this, checkInterval)
            }
        }
    }.also {
        handler.post(it)
    }
}

fun stopListenPermissionGranted() {
    permissionCheckRunnable?.let { handler.removeCallbacks(it) }
    permissionCheckRunnable = null
}