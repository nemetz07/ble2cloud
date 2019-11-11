package com.nemetz.ble2cloud.data

import com.google.firebase.Timestamp

data class SensorData(
    var createdAt: Timestamp = Timestamp.now(),
    var value: Any? = 0
)