package com.nemetz.ble2cloud.data

import android.bluetooth.*
import android.content.Context
import android.util.Log
import java.util.*

data class ComplexSensor(
    val BLESensor: BLESensor,
    val bluetoothDevice: BluetoothDevice,
    val rssi: Int,
    var services: List<BluetoothGattService>? = null,
    var bluetoothGatt: BluetoothGatt? = null,
    var values: ArrayList<BLESensorValue> = arrayListOf()
) {

    private val TAG = "COMPLEX_SENSOR"

    fun enableNotification(descriptor: BluetoothGattDescriptor?) {
        if (descriptor!!.value!!.contentEquals(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE)) {
            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            bluetoothGatt?.writeDescriptor(descriptor)
        }
        bluetoothGatt?.setCharacteristicNotification(descriptor.characteristic, true)
        Log.d(TAG, "Notification enabled for ${BLESensor.address}!")
    }

    fun disableNotification(characteristic: BluetoothGattCharacteristic?) {
        val descriptor = characteristic?.getDescriptor(BLEUUID.CONFIG) ?: return

        if (descriptor.value!!.contentEquals(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)) {
            descriptor.value = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
            bluetoothGatt?.writeDescriptor(descriptor)
        }
        bluetoothGatt?.setCharacteristicNotification(characteristic, false)
    }

    fun connect(
        context: Context,
        gattCallback: BluetoothGattCallback
    ) {
        bluetoothGatt = bluetoothDevice.connectGatt(context, false, gattCallback)
    }

    fun disconnect() {
        bluetoothGatt?.apply {
            disconnect()
            close()
        }
    }


}