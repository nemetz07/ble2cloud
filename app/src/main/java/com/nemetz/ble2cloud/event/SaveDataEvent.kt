package com.nemetz.ble2cloud.event

import com.nemetz.ble2cloud.data.BLESensorData

class SaveDataEvent(var documentId: String, var BLESensorData: BLESensorData)