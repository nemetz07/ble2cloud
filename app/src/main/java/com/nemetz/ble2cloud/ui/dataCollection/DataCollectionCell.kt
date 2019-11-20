package com.nemetz.ble2cloud.ui.dataCollection

import com.nemetz.ble2cloud.utils.enums.DataCellStatus

data class DataCollectionCell(
    var value: String,
    var createdAt: String,
    var name: String,
    var sensorName: String? = null,
    var address: String? = null,
    var status: DataCellStatus = DataCellStatus.LOCAL
)