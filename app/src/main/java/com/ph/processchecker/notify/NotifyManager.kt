package com.ph.processchecker.notify

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.ph.processchecker.R

object NotifyManager {
    const val CHANNEL_ID = "process_monitor_channel"
    const val NOTIFICATION_ID = 777444

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "进程监测",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "后台进程监测通知"
            }
            context.getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    fun buildNotification(context: Context, count: Int): Notification {
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("间隔30分钟监测中")
            .setContentText("当前正在运行${count}个进程")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
}
