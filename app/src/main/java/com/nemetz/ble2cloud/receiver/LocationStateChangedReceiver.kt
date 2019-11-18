package com.nemetz.ble2cloud.receiver

import android.content.BroadcastReceiver
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import com.nemetz.ble2cloud.event.CloseAppEvent
import org.greenrobot.eventbus.EventBus


//@Suppress("DEPRECATION")
class LocationStateChangedReceiver : BroadcastReceiver() {
    private val TAG = "LOCATION_BR"

    override fun onReceive(locContext: Context?, locIntent: Intent?) {
        val action = locIntent?.action

        if (action.equals(LocationManager.MODE_CHANGED_ACTION)) {
            val contentResolver: ContentResolver? = locContext?.contentResolver
            val locManager =
                locContext?.getSystemService(Context.LOCATION_SERVICE) as LocationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                if (!locManager.isLocationEnabled) {
                    Log.d(TAG, "Location turned off!")
                    EventBus.getDefault().post(CloseAppEvent())
                }
            } else {
                val mode: Int = Settings.Secure.getInt(
                    contentResolver,
                    Settings.Secure.LOCATION_MODE,
                    Settings.Secure.LOCATION_MODE_OFF
                )

                when (mode) {
                    Settings.Secure.LOCATION_MODE_OFF -> {
                        Log.d(TAG, "Location turned off!")
                        EventBus.getDefault().post(CloseAppEvent())
                    }
                }
            }
        }
    }

}