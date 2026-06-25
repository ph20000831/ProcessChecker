package com.ph.processchecker.ext

import android.Manifest
import android.app.Activity
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.os.Process
import androidx.core.content.ContextCompat

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

/**
 * 检查通知权限
 */
fun Context.hasNotificationPermission(): Boolean {
    return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        // 低版本需要用NotificationManagerCompat检查系统通知总开关
        androidx.core.app.NotificationManagerCompat.from(this).areNotificationsEnabled()
    }
}

private val handler = Handler(Looper.getMainLooper())
private var permissionCheckRunnable: Runnable? = null

/**
 * 权限授予自动返回
 */
fun Context.startListenPermissionGranted(
    backActivity: Class<out Activity>,
    permissionAction: () -> Boolean,
    clear: Boolean = false,
    checkInterval: Long = 300L
) {
    logD(this.javaClass.simpleName) { "startListen" }
    stopListenPermissionGranted()
    permissionCheckRunnable = object : Runnable {
        override fun run() {
            val isGranted = permissionAction()
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
                logD(this@startListenPermissionGranted.javaClass.simpleName) { "endListen" }
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