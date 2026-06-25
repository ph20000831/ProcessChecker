package com.ph.processchecker.util

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import com.ph.processchecker.App
import com.ph.processchecker.ext.logD
import com.ph.processchecker.model.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ProcessUtil {
    private const val TAG = "ProcessUtil"
    val whiteList: MutableList<String> = mutableListOf()

    suspend fun getRunningProcess(context: Context = App.instance): List<AppInfo> =
        withContext(Dispatchers.IO) {
            val packageManager = context.packageManager
            val selfPackageName = context.packageName
            val lastUsageTimes = getLastUsageTimes(context)

            val allApps = getInstalledApplications(packageManager)
            val appInfoList = mutableListOf<AppInfo>()

            for (appInfo in allApps) {
                val packageName = appInfo.packageName
                if (packageName == selfPackageName) continue
                if (whiteList.contains(packageName)) continue
                if (isSystemApp(appInfo)) continue
                if (!appInfo.enabled) continue
                if ((appInfo.flags and ApplicationInfo.FLAG_STOPPED) != 0) continue
                try {
                    val appName = packageManager.getApplicationLabel(appInfo).toString()
                    val icon = appInfo.loadIcon(packageManager)
                    val lastUseTime = lastUsageTimes[packageName] ?: -1L

                    appInfoList.add(
                        AppInfo(
                            appName = appName,
                            icon = icon,
                            packageName = packageName,
                            lastUseTime = lastUseTime
                        )
                    )
                } catch (_: Exception) {
                    Log.w(TAG, "skip app: $packageName")
                }
            }

            Log.d(TAG, "running apps: \n${appInfoList.joinToString("\n") { it.appName }}")
            return@withContext appInfoList
        }

    @Suppress("DEPRECATION")
    private fun getInstalledApplications(packageManager: PackageManager): List<ApplicationInfo> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getInstalledApplications(PackageManager.ApplicationInfoFlags.of(0))
        } else {
            packageManager.getInstalledApplications(0)
        }
    }

    private fun getLastUsageTimes(context: Context): Map<String, Long> {
        return try {
            val usageStatsManager =
                context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                usageStatsManager.queryAndAggregateUsageStats(0, System.currentTimeMillis())
                    .mapValues { it.value.lastTimeUsed }
            } else {
                @Suppress("DEPRECATION")
                val stats = usageStatsManager.queryUsageStats(
                    UsageStatsManager.INTERVAL_DAILY,
                    0,
                    System.currentTimeMillis()
                ) ?: return emptyMap()
                stats.associate { it.packageName to it.lastTimeUsed }
            }
        } catch (_: SecurityException) {
            emptyMap()
        }
    }

    private fun isSystemApp(applicationInfo: ApplicationInfo): Boolean {
        return (applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0 ||
                (applicationInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
    }


}