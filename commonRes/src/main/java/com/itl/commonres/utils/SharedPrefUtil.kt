package com.itl.commonres.utils

import android.content.Context
import android.content.SharedPreferences
import javax.inject.Inject

class SharedPrefUtil @Inject constructor(mContext: Context) {

    private val USER_SP: String = "user_sp"

    private var sharedPreferences: SharedPreferences =
        mContext.getSharedPreferences(USER_SP, Context.MODE_PRIVATE)

    companion object{
        const val IS_FIRST_LAUNCH = "is_first_launch"
    }

    fun isFirstLaunch(): Boolean {
        return sharedPreferences.getBoolean(IS_FIRST_LAUNCH, true).also { isFirstLaunch ->
            if (isFirstLaunch) {
                sharedPreferences.edit().putBoolean(IS_FIRST_LAUNCH, false).apply()
            }
        }
    }

    fun saveString(type: String, value: String) {
        val editor = sharedPreferences.edit()
        editor.putString(type, value)
        editor.apply()
    }

    fun getString(type: String): String {
        return sharedPreferences.getString(type, "") ?: ""
    }

    fun saveBoolean(type: String, value: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean(type, value)
        editor.apply()
    }

    fun getBoolean(type: String): Boolean {
        return sharedPreferences.getBoolean(type, false)
    }

    fun saveInt(type: String, value: Int) {
        val editor = sharedPreferences.edit()
        editor.putInt(type, value)
        editor.apply()
    }

    fun getInt(type: String): Int {
        return sharedPreferences.getInt(type, 0)
    }

    fun saveFloat(type: String, value: Float) {
        val editor = sharedPreferences.edit()
        editor.putFloat(type, value)
        editor.apply()
    }

    fun getFloat(type: String): Float {
        return sharedPreferences.getFloat(type, 0f)
    }

    fun saveDouble(type: String, value: Double) {
        val editor = sharedPreferences.edit()
        editor.putLong(type, value.toRawBits())
        editor.apply()
    }

    fun getDouble(type: String): Double {
        return Double.fromBits(sharedPreferences.getLong(type, 0.0.toRawBits()))
    }

    fun saveLong(type: String, value: Long) {
        val editor = sharedPreferences.edit()
        editor.putLong(type, value)
        editor.apply()
    }

    fun getLong(type: String): Long {
        return sharedPreferences.getLong(type, 0)
    }

    fun clearKeyPreferences(key: String) {
        val editor = sharedPreferences.edit()
        editor.remove(key)
        editor.apply()
    }

}