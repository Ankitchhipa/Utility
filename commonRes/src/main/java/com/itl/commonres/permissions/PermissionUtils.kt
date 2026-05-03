package com.itl.commonres.permissions

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.itl.commonres.utils.CommonMethods
import com.itl.commonres.utils.PermissionInterface

class PermissionUtils(
    val context: Context,
    private val requestPermissionsInterface: RequestPermissionsInterface,
    val activity: Activity
) : PermissionInterface {

    private val requestPermissionList: MutableList<String> = mutableListOf()
    private val PERMISSIONS_REQUEST_CODE = 11
    private var permissionName = ""
    private var commonMethods = CommonMethods(context)

    fun addPermissionsToList(list: MutableList<String>) {
        requestPermissionList.clear()
        requestPermissionList.addAll(list)
    }

    fun setPermissionName(permissionName: String) {
        this.permissionName = permissionName
    }

    private fun requestMultiplePermissions(): PermissionStatus {
        val permissionResult = checkAndRequestPermissions()

        return when (permissionResult.finalStatus) {
            PermissionStatus.ALLOWED -> {

                PermissionStatus.ALLOWED
            }

            PermissionStatus.DENIED_PERMANENTLY -> {
                PermissionStatus.DENIED_PERMANENTLY
            }

            else -> {
                PermissionStatus.NOT_GIVEN
            }
        }
    }


    fun checkAndRequestPermissions(
        checkStatusOnly: Boolean = false
    ): PermissionResult {

        val permissionPreference = PermissionPreference(context)

        val permissionResult = PermissionResult()

        val permissionStatus: HashMap<String, PermissionStatus> = hashMapOf()

        requestPermissionList.forEach { permission ->
            if (hasPermissionAllowed(permission)) {
                permissionStatus[permission] = PermissionStatus.ALLOWED
            } else {
                val isShowRational = isNeededToShowRequestRational(permission)
                val isAskedPermissionBefore =
                    permissionPreference.isPermissionRequestedBefore(permission)

                when {
                    isShowRational -> {
                        permissionStatus[permission] = PermissionStatus.NOT_GIVEN
                    }

                    isAskedPermissionBefore && !isShowRational -> {
                        permissionStatus[permission] = PermissionStatus.DENIED_PERMANENTLY
                    }

                    else -> {
                        permissionStatus[permission] = PermissionStatus.NOT_GIVEN
                    }
                }
            }
        }

        permissionResult.permissionStatus = permissionStatus

        val isAnyPermissionDeniedPermanently =
            permissionStatus.values.any { it == PermissionStatus.DENIED_PERMANENTLY }

        if (isAnyPermissionDeniedPermanently) {
            permissionResult.finalStatus = PermissionStatus.DENIED_PERMANENTLY
            return permissionResult
        }

        val isAnyPermissionNotGiven =
            permissionStatus.values.any { it == PermissionStatus.NOT_GIVEN }

        if (isAnyPermissionNotGiven) {

            if (!checkStatusOnly) {
                val notGivenPermissionList =
                    permissionStatus.filter { it.value == PermissionStatus.NOT_GIVEN }.keys.toMutableList()

                requestPermissionsInterface.requestPermissions(
                    notGivenPermissionList,
                    PERMISSIONS_REQUEST_CODE
                )
            }

            permissionResult.finalStatus = PermissionStatus.NOT_GIVEN
            return permissionResult

        }

        permissionResult.finalStatus = PermissionStatus.ALLOWED
        return permissionResult
    }

    private fun hasPermissionAllowed(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context, permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun isNeededToShowRequestRational(permission: String): Boolean {
        return activity.shouldShowRequestPermissionRationale(permission)
    }

    private fun checkPermissionAndProceed(status: PermissionStatus) {
        when (status) {
            PermissionStatus.ALLOWED -> {
                requestPermissionsInterface.getPermissionResult(true)
            }

            PermissionStatus.NOT_GIVEN -> {
                requestPermissionsInterface.getPermissionResult(false)
            }

            PermissionStatus.DENIED_PERMANENTLY -> {
                commonMethods.showPermissionDialog(permissionName, this, false)
            }

            else -> {
                //Permission is requesting for first time or user denied permission before but not permanently
            }
        }
    }

    fun checkAndRequestMultiplePermissions() {
        checkPermissionAndProceed(requestMultiplePermissions())
    }

    fun onRequestPermissionResult(checkStatusOnly: Boolean = false) {
        val permissionResult = checkAndRequestPermissions(checkStatusOnly)
        checkPermissionAndProceed(permissionResult.finalStatus)
    }

    interface RequestPermissionsInterface {
        fun requestPermissions(permissionList: MutableList<String>, requestCode: Int)
        fun getPermissionResult(isPermissionGiven: Boolean)
    }

    override fun onPermissionClickOkay(isAllFilesAccess: Boolean, context: Context) {
        commonMethods.processPermission(isAllFilesAccess, context)
    }

    override fun onPermissionClickNotNow(context: Context) {

    }

}