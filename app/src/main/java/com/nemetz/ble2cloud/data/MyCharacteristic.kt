package com.nemetz.ble2cloud.data

import kotlin.collections.ArrayList

data class MyCharacteristic(
    var uuid: String? = null,
    var data: ArrayList<MyDataFormat> = arrayListOf()
)