package com.itl.commonres.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class InstallationReceiver : BroadcastReceiver() {

    private val TAG = "InstallationReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        val status =
            intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)
        val message = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)
        val sessionId =
            intent.getIntExtra(PackageInstaller.EXTRA_SESSION_ID, -1) // Get the session ID
        val packageName =
            intent.getStringExtra(PackageInstaller.EXTRA_PACKAGE_NAME) // Get the package name
        val exception =
            intent.getSerializableExtra(PackageInstaller.EXTRA_OTHER_PACKAGE_NAME) as? Exception // Get the exception

        when (status) {
            PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                // Show the user a dialog to confirm the installation
                val confirmIntent = intent.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)
                if (confirmIntent != null) {
                    confirmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // Add the flag here
                    context.startActivity(confirmIntent)
                } else {
                    Log.e(TAG, "confirmIntent is null")
                }
            }

            PackageInstaller.STATUS_SUCCESS -> {
                // Installation successful
                Log.d(
                    TAG,
                    "Installation successful (Session ID: $sessionId, Package Name: $packageName)"
                )
                Log.i("ApkInstaller", "STATUS_SUCCESS")
                val intent = Intent("ACTION_INSTALL_komal")
                intent.putExtra(Intent.EXTRA_PACKAGE_NAME, packageName)
                intent.putExtra("isInstalled", true)
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
            }

            else -> {
                // Installation failed
                val statusString = getStatusString(status)
                Log.e(
                    TAG,
                    "Installation failed (Session ID: $sessionId, Package Name: $packageName, Status: $statusString, Message: $message)",
                    exception
                )
                Log.i("ApkInstaller", "STATUS_FAILED")
                val intent = Intent("ACTION_INSTALL_komal")
                intent.putExtra(Intent.EXTRA_PACKAGE_NAME, packageName)
                intent.putExtra("isInstalled", false)
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
            }
        }
    }

    private fun getStatusString(status: Int): String {
        return when (status) {
            PackageInstaller.STATUS_FAILURE -> "STATUS_FAILURE"
            PackageInstaller.STATUS_FAILURE_ABORTED -> "STATUS_FAILURE_ABORTED"
            PackageInstaller.STATUS_FAILURE_BLOCKED -> "STATUS_FAILURE_BLOCKED"
            PackageInstaller.STATUS_FAILURE_CONFLICT -> "STATUS_FAILURE_CONFLICT"
            PackageInstaller.STATUS_FAILURE_INCOMPATIBLE -> "STATUS_FAILURE_INCOMPATIBLE"
            PackageInstaller.STATUS_FAILURE_INVALID -> "STATUS_FAILURE_INVALID"
            PackageInstaller.STATUS_FAILURE_STORAGE -> "STATUS_FAILURE_STORAGE"
            PackageInstaller.STATUS_PENDING_USER_ACTION -> "STATUS_PENDING_USER_ACTION"
            PackageInstaller.STATUS_SUCCESS -> "STATUS_SUCCESS"
            else -> "UNKNOWN_STATUS"
        }
    }
}