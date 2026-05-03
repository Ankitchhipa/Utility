package com.itl.commonres.permissions

import android.content.Context

class PermissionPreference(context: Context) {
    companion object {
        private const val PERMISSION_PREFERENCE_FILE = "permissionPreference"
    }

    private val sharedPreference = context.getSharedPreferences(PERMISSION_PREFERENCE_FILE, Context.MODE_PRIVATE)

    fun isPermissionRequestedBefore(permission: String): Boolean {
        return sharedPreference.getBoolean(permission, false)
    }

    fun setPermissionRequested(permission: String) {
        val editor = sharedPreference.edit()
        editor.putBoolean(permission, true).apply()
    }
}