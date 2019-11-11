package com.nemetz.ble2cloud.data

import android.bluetooth.BluetoothDevice


object SensorRepository {
    private var sensors: ArrayList<BluetoothDevice> = arrayListOf()

    fun addSensor(btDevice: BluetoothDevice){
        if (btDevice !in this.sensors) {
            this.sensors.add(btDevice)
        }
    }

    fun getSensor(position: Int): BluetoothDevice{
        return this.sensors[position]
    }

    fun clearSensors(){
        this.sensors.clear()
    }
}