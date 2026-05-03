package com.itl.commonres.permissions

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import com.itl.commonres.utils.Constants
import android.Manifest.permission.CAMERA as CAMERA_PERMISSION

sealed class PermissionsListSealedClass(
    val permissionsList: MutableList<String>,
    val permissionName: String
) {
    object STORAGE_AND_CAMERA :
        PermissionsListSealedClass(
            arrayListOf(
                CAMERA_PERMISSION,
                READ_EXTERNAL_STORAGE,
                WRITE_EXTERNAL_STORAGE
            ), Constants.Storage_and_Camera
        )

    object STORAGE :
        PermissionsListSealedClass(
            arrayListOf(READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE),
            Constants.Storage
        )

    object CAMERA : PermissionsListSealedClass(arrayListOf(CAMERA_PERMISSION), Constants.Camera)
    object EXCEPTION : PermissionsListSealedClass(arrayListOf(), "Error")

    companion object {
        fun from(permission: String) = when (permission) {
            Constants.Storage_and_Camera -> STORAGE_AND_CAMERA
            Constants.Storage -> STORAGE
            Constants.Camera -> CAMERA
            else -> EXCEPTION
        }
    }
}