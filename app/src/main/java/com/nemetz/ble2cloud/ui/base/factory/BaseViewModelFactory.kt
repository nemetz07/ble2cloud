@file:Suppress("UNCHECKED_CAST")

package com.nemetz.ble2cloud.ui.base.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nemetz.ble2cloud.BLEApplication
import com.nemetz.ble2cloud.ui.deviceBrowser.viewmodel.DeviceBrowserViewModel

class BaseViewModelFactory(private val mApplication: BLEApplication): ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        when(modelClass){
            DeviceBrowserViewModel::class.java -> return DeviceBrowserViewModel(mApplication) as T
            else -> return super.create(modelClass)
        }
    }
}