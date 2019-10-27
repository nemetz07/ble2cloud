package com.nemetz.ble2cloud.data

import android.bluetooth.BluetoothDevice


object DevicesRepository {
    private lateinit var devices: ArrayList<BluetoothDevice>

    fun setUp(){
        devices = arrayListOf()
    }

    fun addDevice(btDevice: BluetoothDevice){
        devices.add(btDevice)
    }

    fun getDevice(position: Int): BluetoothDevice{
        return devices[position]
    }

    fun clearDevices(){
        devices.clear()
    }
}