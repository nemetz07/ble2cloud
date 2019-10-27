package com.nemetz.ble2cloud

import android.app.Application
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

val uiScope = CoroutineScope(Dispatchers.Main)
val ioScope = CoroutineScope(Dispatchers.IO)

class BLEApplication : Application() {
    val TAG = "BLE_APPLICATION"

    override fun onCreate() {
        super.onCreate()

        Log.d(TAG, "Application onCreate")
    }
}