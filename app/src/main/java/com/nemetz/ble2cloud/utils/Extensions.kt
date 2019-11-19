package com.nemetz.ble2cloud.utils

import android.os.ParcelUuid
import android.util.SparseArray
import androidx.core.util.keyIterator
import com.google.firebase.firestore.DocumentChange
import com.nemetz.ble2cloud.data.BLECharacteristic
import com.nemetz.ble2cloud.data.BLEDataFormat
import com.nemetz.ble2cloud.data.BLESensor
import java.util.*

fun SparseArray<ByteArray>.asFirestoreData(): MutableMap<String, List<Int>> {
    val mutableMap = mutableMapOf<String, List<Int>>()

    for (key in this.keyIterator()) {
        if (key != 0) {
            val l = arrayListOf<Int>()
            for (value in this.get(key).asIterable()) {
                l.add(value.toInt())
            }
            mutableMap.put("$key", l)
        }
    }

    return mutableMap
}

fun MutableMap<ParcelUuid, ByteArray>.asFirestoreData(): MutableMap<String, List<Int>> {
    val mutableMap = mutableMapOf<String, List<Int>>()

    for (key in keys) {
        val l = arrayListOf<Int>()
        for (value in this[key]!!) {
            l.add(value.toInt())
        }
        mutableMap.put(key.uuid.toString(), l)
    }

    return mutableMap
}

fun List<ParcelUuid>.asFirestoreData(): List<String> {
    val arrayList = arrayListOf<String>()

    for (value in this) {
        arrayList.add(value.uuid.toString())
    }

    return arrayList
}

fun DocumentChange.getMySensor(): BLESensor {
    val mySensor = this.document.toObject(BLESensor::class.java)

    return mySensor
}

fun DocumentChange.getMyCharacteristic(): BLECharacteristic? {
    var values: Any? = this.document.get("values") ?: return null
    values = values as Map<String, Map<String, String?>>

    val data = arrayListOf<BLEDataFormat>()
    values.forEach { value ->
        val myDataFormat = value.value
        data.add(
            BLEDataFormat(
                name = myDataFormat.get("name") ?: "",
                unit = myDataFormat.get("unit") ?: "",
                format = myDataFormat.get("format"),
                offset = myDataFormat.get("offset")?.toInt() ?: 0,
                substring_start = myDataFormat.get("substring_start")?.toInt(),
                substring_end = myDataFormat.get("substring_end")?.toInt()
            )
        )
    }

    val myCharacteristic = BLECharacteristic(
        uuid = this.document.get("uuid") as String,
        data = data
    )

    return myCharacteristic
}

fun DocumentChange.getPath(): String {
    return this.document.reference.path.substringBefore("/")
}