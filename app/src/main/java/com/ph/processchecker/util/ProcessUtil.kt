package com.ph.processchecker.util

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.collection.LruCache
import com.ph.processchecker.App
import com.ph.processchecker.model.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ProcessUtil {
    val whiteList: MutableList<String> = mutableListOf()

    private val iconCache = object :
        LruCache<String, Drawable>((Runtime.getRuntime().maxMemory() / 1024).toInt() / 10) {
        override fun sizeOf(key: String, value: Drawable): Int {
            return if (value.intrinsicWidth > 0 && value.intrinsicHeight > 0) {
                // ARGB_8888 每个像素4字节，计算总字节再转KB
                val byteCount = value.intrinsicWidth * value.intrinsicHeight * 4
                byteCount / 1024
            } else {
                // 无法获取宽高默认占用1KB
                1
            }
        }
    }

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
                    val icon = iconCache[packageName] ?: appInfo.loadIcon(packageManager).also {
                        iconCache.put(packageName, it)
                    }
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
                }
            }

            return@withContext appInfoList
        }

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