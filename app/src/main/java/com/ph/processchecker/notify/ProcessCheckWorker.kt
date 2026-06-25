package com.ph.processchecker.notify

import android.content.Context
import android.content.Intent
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.ph.processchecker.ext.isSleepTime
import com.ph.processchecker.util.ProcessUtil
import java.util.concurrent.TimeUnit

class ProcessCheckWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        private const val WORK_NAME = "process_check_periodic"
        fun schedulePeriodicCheck(context: Context) {
            val request = PeriodicWorkRequestBuilder<ProcessCheckWorker>(30, TimeUnit.MINUTES)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }

    override suspend fun doWork(): Result {
        if (isSleepTime()) {
            ForegroundService.stopForegroundService(applicationContext)
            return Result.success()
        }
        return try {
            val count = ProcessUtil.getRunningProcess(applicationContext).size
            val intent = Intent(applicationContext, ForegroundService::class.java).apply {
                putExtra(ForegroundService.EXTRA_COUNT, count)
            }
            applicationContext.startService(intent)
            Result.success()
        } catch (_: Exception) {
            Result.success()
        }
    }

}
