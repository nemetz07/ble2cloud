package com.nemetz.ble2cloud.data

import android.bluetooth.BluetoothGattCharacteristic
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class BLEDataFormat(
    var name: String = "-",
    var unit: String = "",
    var format: String? = null,
    var offset: Int = 0,
    var substring_start: Int? = null,
    var substring_end: Int? = null
) : Parcelable {
    fun dataFormat(): Int {
        return when (format) {
            "UINT8" -> BluetoothGattCharacteristic.FORMAT_UINT8
            "UINT16" -> BluetoothGattCharacteristic.FORMAT_UINT16
            "UINT32" -> BluetoothGattCharacteristic.FORMAT_UINT32
            "SINT8" -> BluetoothGattCharacteristic.FORMAT_SINT8
            "SINT16" -> BluetoothGattCharacteristic.FORMAT_SINT16
            "SINT32" -> BluetoothGattCharacteristic.FORMAT_SINT32
            "FLOAT" -> BluetoothGattCharacteristic.FORMAT_FLOAT
            "SFLOAT" -> BluetoothGattCharacteristic.FORMAT_SFLOAT
            else -> 0
        }
    }
}