package com.nemetz.ble2cloud.utils.manager

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.location.LocationManager
import android.os.Build
import android.util.Log
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task


object BaseAccessManager {
    const val TAG = "BASE_ACCESS_MANAGER"

    const val BLUETOOTH = "bluetooth"
    const val LOCATION = "location"
    const val REQUEST_ENABLE_BT: Int = 1001
    const val REQUEST_ENABLE_LOCATION: Int = 1004

    fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            locationManager.isLocationEnabled
        } else {
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        }
    }

    fun requestLocation(context: Context) {
        val locationRequest =
            LocationRequest.create().setPriority(LocationRequest.PRIORITY_LOW_POWER)

        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

        val client: SettingsClient = LocationServices.getSettingsClient(context)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the createdBy a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    exception.startResolutionForResult(
                        (context as Activity),
                        REQUEST_ENABLE_LOCATION
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d(TAG, "Exception while enabling location: $sendEx")
                }
            }
        }
    }

    fun isBluetoothEnabled(context: Context): Boolean {
        return (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter.isEnabled
    }

    fun requestBluetooth(context: Context) {
        val btAdapter =
            (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter

        btAdapter.takeIf { !btAdapter.isEnabled }?.apply {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            (context as Activity).startActivityForResult(
                enableBtIntent,
                REQUEST_ENABLE_BT
            )
        }
    }
}