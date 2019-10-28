package com.nemetz.ble2cloud.data

import android.bluetooth.BluetoothDevice


object SensorRepository {
    private var sensors: ArrayList<BluetoothDevice> = arrayListOf()

    private var connectedSensors: ArrayList<BluetoothDevice> = arrayListOf()

    fun addSensor(btDevice: BluetoothDevice){
        sensors.add(btDevice)
    }

    fun getSensor(position: Int): BluetoothDevice{
        return sensors[position]
    }

    fun clearSensors(){
        sensors.clear()
    }
}