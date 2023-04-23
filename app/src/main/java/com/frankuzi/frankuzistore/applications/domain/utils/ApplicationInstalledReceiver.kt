package com.frankuzi.frankuzistore.applications.domain.utils

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log

class ApplicationInstalledReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {

        if (intent != null && context != null) {

            // Check this condition because the broadcast receiver
            // is getting triggered on some devices running above Oreo
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {

                val packageManager = context.packageManager
                val appUid = intent.getIntExtra(Intent.EXTRA_UID, 0)

                if (intent.action == "android.intent.action.PACKAGE_FULLY_REMOVED") {
                    Log.d("LOOOOOG", "PACKAGE_FULLY_REMOVED $appUid")
                } else {
                    val applicationInfo = packageManager?.getApplicationInfo(
                        packageManager.getNameForUid(appUid)!!, PackageManager.GET_META_DATA
                    )!!

                    val appName = packageManager.getApplicationLabel(applicationInfo).toString()
                    val appPackageName = applicationInfo.packageName

                    if (intent.action == "android.intent.action.PACKAGE_ADDED") {
                        Log.d("LOOOOOOG", "PACKAGE_ADDED $appPackageName , $appName")
                    } else if (intent.action == "android.intent.action.PACKAGE_REPLACED") {
                        Log.d("LOOOOOOG", "PACKAGE_REPLACED $appPackageName , $appName")
                    }
                }
            }
        }
    }
}