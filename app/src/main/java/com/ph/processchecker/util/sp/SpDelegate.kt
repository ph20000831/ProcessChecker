package com.ph.processchecker.util.sp

import android.content.SharedPreferences
import androidx.core.content.edit
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * SP 泛型委托：自动根据属性类型读写 SharedPreferences
 * 支持：String/Int/Boolean/Float/Long/Set<String>
 */
class SpDelegate<T>(
    private val key: String,
    private val defaultValue: T,
    private val sp: SharedPreferences
) : ReadWriteProperty<Any?, T> {

    @Suppress("UNCHECKED_CAST")
    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return when (defaultValue) {
            is String -> sp.getString(key, defaultValue) as T
            is Int -> sp.getInt(key, defaultValue) as T
            is Boolean -> sp.getBoolean(key, defaultValue) as T
            is Float -> sp.getFloat(key, defaultValue) as T
            is Long -> sp.getLong(key, defaultValue) as T
            is Set<*> -> sp.getStringSet(key, defaultValue as Set<String>) as T
            else -> throw IllegalArgumentException("不支持的 SP 类型：${defaultValue!!::class.java}")
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        sp.edit {
            when (value) {
                is String -> putString(key, value)
                is Int -> putInt(key, value)
                is Boolean -> putBoolean(key, value)
                is Float -> putFloat(key, value)
                is Long -> putLong(key, value)
                is Set<*> -> putStringSet(key, value as Set<String>)
                else -> throw IllegalArgumentException("不支持的 SP 类型：${value!!::class.java}")
            }
        }
    }
}

/**
 * 快捷委托扩展函数
 * 使用：val userName by spDele("key", "默认值")
 */
fun <T> SharedPreferences.spDele(key: String, defaultValue: T): ReadWriteProperty<Any?, T> {
    return SpDelegate(key, defaultValue, this)
}