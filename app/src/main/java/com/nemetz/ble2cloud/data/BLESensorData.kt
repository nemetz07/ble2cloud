package com.nemetz.ble2cloud.data

import com.google.firebase.Timestamp

data class BLESensorData(
    val createdAt: Timestamp = Timestamp.now(),
    var createdBy: String?,
    val value: String? = "0",
    var unit: String? = null,
    var name: String = "-",
    var address: String? = null,
    var sensorName: String? = null,
    var latitude: Double? = null,
    var longitude: Double? = null
)