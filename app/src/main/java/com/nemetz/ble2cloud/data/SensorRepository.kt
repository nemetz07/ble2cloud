package com.nemetz.ble2cloud.data

import android.bluetooth.BluetoothDevice


object SensorRepository {
    private lateinit var sensors: ArrayList<BluetoothDevice>

    fun setUp(){
        sensors = arrayListOf()
    }

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