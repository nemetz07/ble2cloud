package com.nemetz.ble2cloud.ui.dataCollection

data class DataCollectionCell(var value: String,
                              var createdAt: String,
                              var name: String,
                              var sensorName: String? = null,
                              var address: String? = null,
                              var isLocal: Boolean = true
)