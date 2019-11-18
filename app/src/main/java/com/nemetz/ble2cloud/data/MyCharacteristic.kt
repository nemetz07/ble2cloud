package com.nemetz.ble2cloud.data

data class MyCharacteristic(
    var uuid: String? = null,
    var data: ArrayList<MyDataFormat> = arrayListOf()
)