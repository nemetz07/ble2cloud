package com.nemetz.ble2cloud.utils.manager

import android.app.Activity
import android.content.Context


object SharedPreferencesManager {

    fun putLocationPermissionDenied(context: Context) {
        (context as Activity).getPreferences(Context.MODE_PRIVATE).edit().putBoolean(
            PermissionManager.LOCATION_PAERMISSION_DENIED, true
        ).apply()
    }

    fun isLocationPermissionDenied(context: Context): Boolean {
        return (context as Activity).getPreferences(Context.MODE_PRIVATE).getBoolean(
            PermissionManager.LOCATION_PAERMISSION_DENIED, false
        )
    }
}