package com.nemetz.ble2cloud.ui.dataCollectionOptions

import androidx.lifecycle.ViewModel

class DataCollectionOptionsViewModel : ViewModel() {

    private val TAG = "DATA_COLLECTION_OPTIONS_VIEWMODEL"

    var scanEffort: String = "LOW_POWER"
    var scanRate: Int = 10
    var dataRate: Int = 5
    var locationRecord: Boolean = false

}
