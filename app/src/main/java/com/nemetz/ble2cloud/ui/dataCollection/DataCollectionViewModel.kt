package com.nemetz.ble2cloud.ui.dataCollection

import androidx.lifecycle.ViewModel

class DataCollectionViewModel : ViewModel() {

    private var TAG = "DATA_COLLECTION_VIEWMODEL"

    val cellData: ArrayList<DataCollectionCell> = arrayListOf()

}
