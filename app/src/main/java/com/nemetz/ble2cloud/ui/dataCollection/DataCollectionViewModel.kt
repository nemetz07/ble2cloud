package com.nemetz.ble2cloud.ui.dataCollection

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ListenerRegistration
import com.nemetz.ble2cloud.data.BLESensor

class DataCollectionViewModel : ViewModel() {

    private var TAG = "DATA_COLLECTION_VIEWMODEL"

    val sensors: ArrayList<BLESensor> = arrayListOf()
    val cellData: ArrayList<DataCollectionCell> = arrayListOf()
    var mRegistration: ListenerRegistration? = null

    override fun onCleared() {
        mRegistration?.remove()
        super.onCleared()
    }

}
