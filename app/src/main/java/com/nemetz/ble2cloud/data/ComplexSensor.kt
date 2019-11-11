package com.nemetz.ble2cloud.data

import android.bluetooth.*
import android.content.Context
import android.util.Log
import java.util.*

val DISABLED: Byte = 0
val ENABLED: Byte = 0

val HRM_SERVICE = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb")
val HRM_CHARACTERISTIC = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb")
val HRM_ADDRESS = "3C:71:BF:F4:68:46"

val DESCRIPTION_INFO = UUID.fromString("00002901-0000-1000-8000-00805f9b34fb")
val DESCRIPTOR_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

val XI_SERVICE = UUID.fromString("226c0000-6476-4566-7562-66734470666d")
val XI_CHARACTERISTIC = UUID.fromString("226caa55-6476-4566-7562-66734470666d")
val XI_ADDRESS = "58:2D:34:30:B5:47"

data class ComplexSensor(
    val mySensor: MySensor,
    val bluetoothDevice: BluetoothDevice,
    val rssi: Int,
    var services: List<BluetoothGattService>? = null,
    var bluetoothGatt: BluetoothGatt? = null,
    var values: ArrayList<SensorValue> = arrayListOf()
) {

    private val TAG = "COMPLEX_SENSOR"

    fun enableNotification(descriptor: BluetoothGattDescriptor?) {
        if (descriptor!!.value!!.contentEquals(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE)) {
            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            bluetoothGatt?.writeDescriptor(descriptor)
        }
        bluetoothGatt?.setCharacteristicNotification(descriptor.characteristic, true)
        Log.d(TAG, "Notification enabled for ${mySensor.address}!")
    }

    fun disableNotification(characteristic: BluetoothGattCharacteristic?) {
        val descriptor = characteristic?.getDescriptor(DESCRIPTOR_CONFIG) ?: return

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