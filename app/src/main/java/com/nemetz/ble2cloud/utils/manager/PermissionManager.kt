package com.nemetz.ble2cloud.utils.manager

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.nemetz.ble2cloud.ui.dialog.PermissionDialogFragment


object PermissionManager {
    private const val TAG = "PERMISSION_MANAGER"

    const val REQUEST_COARSE_LOCATION = 1002
    const val OPEN_SETTINGS_FOR_PERMISSION = 1003
    const val LOCATION_PAERMISSION_DENIED = "LOCATION_PAERMISSION_DENIED"

    private fun askLocationPermission(context: Context) {
        ActivityCompat.requestPermissions(
            context as Activity,
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
            REQUEST_COARSE_LOCATION
        )
    }

    private fun canAskLocationPermission(context: Context): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(
            context as Activity,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    fun isLocationPermissionGranted(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun checkLocationPermission(context: Context): Boolean {
        if (isLocationPermissionGranted(context))
            return true

        if (!canAskLocationPermission(context) && SharedPreferencesManager.isLocationPermissionDenied(
                context
            )
        ) {
            val dialog = PermissionDialogFragment.newInstance()
            dialog.show((context as FragmentActivity).supportFragmentManager, TAG)
        } else {
            askLocationPermission(context)
        }

        return false
    }
}