package com.ph.processchecker.notify

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ph.processchecker.ext.logD
import com.ph.processchecker.util.ProcessUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action ?: return
        // 匹配开机广播
        if (action == Intent.ACTION_BOOT_COMPLETED) {
            logD(TAG) { "receive boot completed" }
            context?.let {
                CoroutineScope(Dispatchers.Default).launch {
                    val count = ProcessUtil.getRunningProcess(it).size
                    ForegroundService.startForegroundService(it, count)
                }
            }
        }
    }
}