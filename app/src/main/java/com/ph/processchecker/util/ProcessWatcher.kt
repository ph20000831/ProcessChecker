package com.ph.processchecker.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.ph.processchecker.App
import com.ph.processchecker.ext.logD
import com.ph.processchecker.model.AppInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.milliseconds

object ProcessWatcher {
    private const val TAG = "ProcessWatcher"
    private var job: Job? = null

    fun startWatch(
        context: Context = App.instance,
        backActivity: Class<out Activity>,
        oldList: List<AppInfo>,
        targetApp: AppInfo,
        intervalMs: Long = 500L
    ) {
        stopWatch()
        job = CoroutineScope(Dispatchers.Default).launch {
            while (isActive) {
                val current = ProcessUtil.getRunningProcess(context)
                val targetGone = targetApp in oldList && targetApp !in current
                if (targetGone) {
                    withContext(Dispatchers.Main) {
                        val it = Intent(context, backActivity).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(it)
                    }
                    logD(TAG) { "监听到结束运行 $targetApp" }
                    break
                }
                delay(intervalMs.milliseconds)
            }
        }
    }

    fun stopWatch() {
        job?.cancel()
        job = null
    }
}