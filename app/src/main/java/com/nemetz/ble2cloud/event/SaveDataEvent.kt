package com.nemetz.ble2cloud.event

import com.nemetz.ble2cloud.data.SensorData

class SaveDataEvent(var documentId: String, var sensorData: SensorData)