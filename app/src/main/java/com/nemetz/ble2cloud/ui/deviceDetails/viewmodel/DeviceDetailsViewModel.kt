package com.nemetz.ble2cloud.ui.deviceDetails.viewmodel

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import com.nemetz.ble2cloud.event.ConnectionClosedEvent
import com.nemetz.ble2cloud.event.ConnectionEstablishedEvent
import org.greenrobot.eventbus.EventBus
import android.bluetooth.BluetoothGattDescriptor
import com.nemetz.ble2cloud.event.DeviceReadingEvent
import java.util.*


class DeviceDetailsViewModel : ViewModel() {
    private val TAG = "DEVICE_DETAIL_VIEWMODEL"

    private var context: Context? = null

    private var btGatt: BluetoothGatt? = null
    private var hrmCharacteristic: BluetoothGattCharacteristic? = null

    var mConnected = false

    private val btGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            when (newState) {
                BluetoothGatt.STATE_CONNECTED -> {
                    btGatt = gatt
                    onConnected()
                }
                BluetoothGatt.STATE_DISCONNECTED -> {
                    btGatt = null
                    onDisconnected()
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            for(gattService in btGatt!!.services){
                Log.d(TAG, "SERVICE: ${gattService.uuid}")
                for (gattCharacteristic in gattService.characteristics){
                    Log.d(TAG, "    CHARACTERISTIC: ${gattCharacteristic.uuid}")
                    if(gattCharacteristic.uuid.toString() == "00002a37-0000-1000-8000-00805f9b34fb"){
                        hrmCharacteristic = gattCharacteristic
                    }
                    for(gattDescriptor in gattCharacteristic.descriptors){
                        Log.d(TAG, "        DESCRIPTOR: ${gattDescriptor.uuid}")
                    }
                }
            }

            onCharacteristicSet()
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?
        ) {
//            Log.d(TAG, "Characteristic Changed!")

//            var str = "["
//            if (characteristic != null) {
//                for(b in characteristic.value){
//                    str = "$str $b"
//                }
//                EventBus.getDefault().post(DeviceReadingEvent(characteristic.value[1]))
//            }
//            str = "$str]"
//            Log.d(TAG, "VALUE: $str")
            if(characteristic != null){
                val value = characteristic.value[1]
                Log.d("TAG", "VALUE: ${magicallyExtractRightValue(value)} - ${btGatt?.device?.name}")
                EventBus.getDefault().post(DeviceReadingEvent(value))
            }

            super.onCharacteristicChanged(gatt, characteristic)
        }
    }

    private fun magicallyExtractRightValue(o: Byte): Int = when {
        (o.toInt() < 0) -> 255 + o.toInt() + 1
        else -> o.toInt()
    }

    private fun onCharacteristicSet() {
        btGatt?.setCharacteristicNotification(hrmCharacteristic, true)
        val uuid = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
        val descriptor = hrmCharacteristic?.getDescriptor(uuid)
        descriptor?.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
        btGatt?.writeDescriptor(descriptor)
    }

    fun init(context: Context?) {
        this.context = context
    }

    fun onConnected() {
        Log.d(TAG, "Device connected!")
        btGatt?.discoverServices()

        mConnected = true
        EventBus.getDefault().post(ConnectionEstablishedEvent())
    }

    private fun onDisconnected() {
        Log.d(TAG, "Device disconnected!")
        mConnected = false
        EventBus.getDefault().post(ConnectionClosedEvent())
    }

    fun connect(device: BluetoothDevice) {
        device.connectGatt(context, false, btGattCallback)
    }

    fun disconnect() {
        btGatt?.setCharacteristicNotification(hrmCharacteristic, false)
        btGatt?.disconnect()
    }

    fun onConnectClicked(device: BluetoothDevice) {
        if (!mConnected) {
            connect(device)
        } else {
            disconnect()
        }
    }
}
