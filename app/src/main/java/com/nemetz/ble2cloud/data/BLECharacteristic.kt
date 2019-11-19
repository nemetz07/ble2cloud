package com.nemetz.ble2cloud.data

data class BLECharacteristic(
    var uuid: String? = null,
    var data: ArrayList<BLEDataFormat> = arrayListOf()
)