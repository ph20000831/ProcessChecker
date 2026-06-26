package com.ph.processchecker.notify

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.ph.processchecker.ext.logD

class ForegroundService : Service() {
    companion object {
        private const val TAG = "ForegroundService"
        private const val EXTRA_COUNT = "extra_count"

        fun startForegroundService(context: Context, count: Int) {
            ContextCompat.startForegroundService(
                context,
                Intent(context, ForegroundService::class.java).apply {
                    putExtra(EXTRA_COUNT, count)
                }
            )
        }

        fun stopForegroundService(context: Context) {
            context.applicationContext.stopService(
                Intent(context.applicationContext, ForegroundService::class.java)
            )
        }
    }

    override fun onCreate() {
        super.onCreate()
        NotifyManager.createChannel(this)
    }

    @SuppressLint("InlinedApi")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val count = intent?.getIntExtra(EXTRA_COUNT, 0) ?: 0
        logD(TAG) { "onStartCommand: count = $count" }
        ServiceCompat.startForeground(
            this,
            NotifyManager.NOTIFICATION_ID,
            NotifyManager.buildNotification(this, count),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        )
        ProcessCheckWorker.schedulePeriodicCheck(this)
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

}
