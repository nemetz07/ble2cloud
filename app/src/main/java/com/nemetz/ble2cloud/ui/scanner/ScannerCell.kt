package com.nemetz.ble2cloud.ui.scanner

import android.bluetooth.BluetoothDevice
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

//data class ScannerCell(
//    var name: String,
//    var address: String,
//    var rssi: Int,
//    val icon: String,
//    val isConnectable: Boolean,
//    val manufacturerSpecificData: SparseArray<ByteArray>?,
//    val serviceUUIDs: MutableList<ParcelUuid>?,
////    val serviceSolicitationUUID: MutableList<ParcelUuid>?,
//    val serviceData: MutableMap<ParcelUuid, ByteArray>?
//)

@Parcelize
data class ScannerCell(
    var device: BluetoothDevice,
    var rssi: Int,
    var isConnectable: Boolean
) : Parcelable