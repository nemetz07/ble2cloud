package com.nemetz.ble2cloud.connection

import android.bluetooth.BluetoothGattCallback
import android.content.Context
import com.nemetz.ble2cloud.data.SensorRepository

class BLEConnection(val context: Context) {



    fun connect(position: Int, gattCallback: BluetoothGattCallback){
        val device = SensorRepository.getSensor(position)

        device.connectGatt(context, false, gattCallback)
    }

    fun disconnect(position: Int){
        val device = SensorRepository.getSensor(position)
    }
}