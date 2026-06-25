package com.ph.processchecker.util.sp

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.ph.processchecker.App

open class BaseSp {
    companion object {
        private const val SP_NAME = "app_process_checker_sp" // SP 文件名
    }

    val sp: SharedPreferences by lazy {
        App.instance.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
    }

    // ====================== put 存储（edit 高阶函数） ======================
    fun putString(key: String, value: String?) = sp.edit { putString(key, value) }
    fun putInt(key: String, value: Int) = sp.edit { putInt(key, value) }
    fun putBoolean(key: String, value: Boolean) = sp.edit { putBoolean(key, value) }
    fun putFloat(key: String, value: Float) = sp.edit { putFloat(key, value) }
    fun putLong(key: String, value: Long) = sp.edit { putLong(key, value) }
    fun putStringSet(key: String, value: Set<String>?) = sp.edit { putStringSet(key, value) }

    // ====================== get 获取 ======================
    fun getString(key: String, defaultValue: String = ""): String =
        sp.getString(key, defaultValue) ?: defaultValue

    fun getInt(key: String, defaultValue: Int = 0): Int = sp.getInt(key, defaultValue)
    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean =
        sp.getBoolean(key, defaultValue)

    fun getFloat(key: String, defaultValue: Float = 0f): Float = sp.getFloat(key, defaultValue)
    fun getLong(key: String, defaultValue: Long = 0L): Long = sp.getLong(key, defaultValue)
    fun getStringSet(key: String, defaultValue: Set<String> = emptySet()): Set<String> =
        sp.getStringSet(key, defaultValue) ?: defaultValue

    // ====================== 工具方法 ======================
    fun remove(key: String) = sp.edit { remove(key) }
    fun clear() = sp.edit { clear() }
    fun contains(key: String): Boolean = sp.contains(key)

}