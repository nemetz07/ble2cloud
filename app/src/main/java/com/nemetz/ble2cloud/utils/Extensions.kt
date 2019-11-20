package com.nemetz.ble2cloud.utils

import com.google.firebase.firestore.DocumentChange
import com.nemetz.ble2cloud.data.BLECharacteristic
import com.nemetz.ble2cloud.data.BLEDataFormat
import com.nemetz.ble2cloud.data.BLESensor

fun DocumentChange.getBLESensor(): BLESensor {
    return document.toObject(BLESensor::class.java)
}

fun DocumentChange.getBLECharactersitic(): BLECharacteristic? {
    var values: Any? = this.document.get("values") ?: return null
    values = values as Map<String, Map<String, String?>>

    val data = arrayListOf<BLEDataFormat>()
    values.forEach { value ->
        val myDataFormat = value.value
        data.add(
            BLEDataFormat(
                name = myDataFormat["name"] ?: "",
                unit = myDataFormat["unit"] ?: "",
                format = myDataFormat["format"],
                offset = myDataFormat["offset"]?.toInt() ?: 0,
                substring_start = myDataFormat["substring_start"]?.toInt(),
                substring_end = myDataFormat["substring_end"]?.toInt()
            )
        )
    }

    return BLECharacteristic(
        uuid = document.get("uuid") as String,
        data = data
    )
}

fun DocumentChange.getPath(): String {
    return this.document.reference.path.substringBefore("/")
}