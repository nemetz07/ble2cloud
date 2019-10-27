package com.nemetz.ble2cloud.connection

import android.bluetooth.BluetoothGattCallback
import android.content.Context
import com.nemetz.ble2cloud.data.DevicesRepository

class BLEConnection(val context: Context) {



    fun connect(position: Int, gattCallback: BluetoothGattCallback){
        val device = DevicesRepository.getDevice(position)

        device.connectGatt(context, false, gattCallback)
    }

    fun disconnect(position: Int){
        val device = DevicesRepository.getDevice(position)
    }
}