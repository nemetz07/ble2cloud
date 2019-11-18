package com.nemetz.ble2cloud.receiver

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.nemetz.ble2cloud.event.CloseAppEvent
import org.greenrobot.eventbus.EventBus

class BluetoothStateChangedReceiver : BroadcastReceiver() {
    private val TAG = "BLUETOOTH_BR"

    override fun onReceive(brContext: Context?, brIntent: Intent?) {
        val action = brIntent?.action

        if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
            val state = brIntent?.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
            when (state) {
                BluetoothAdapter.STATE_OFF -> {
                    Log.d(TAG, "Bluetooth turned off!")
                    EventBus.getDefault().post(CloseAppEvent())
                }
            }
        }
    }

}