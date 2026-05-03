package com.itl.commonres.utils

import android.content.Context

interface PermissionInterface {
    fun onPermissionClickOkay(isAllFilesAccess: Boolean,context: Context)
    fun onPermissionClickNotNow(context: Context)
}