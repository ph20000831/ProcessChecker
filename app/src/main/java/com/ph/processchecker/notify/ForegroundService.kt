package com.ph.processchecker.notify

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.content.ContextCompat

class ForegroundService : Service() {
    companion object {
        const val EXTRA_COUNT = "extra_count"

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

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val count = intent?.getIntExtra(EXTRA_COUNT, 0) ?: 0
        startForeground(
            NotifyManager.NOTIFICATION_ID,
            NotifyManager.buildNotification(this, count)
        )
        ProcessCheckWorker.schedulePeriodicCheck(this)
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

}
