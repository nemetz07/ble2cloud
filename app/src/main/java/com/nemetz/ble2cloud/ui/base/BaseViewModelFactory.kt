@file:Suppress("UNCHECKED_CAST")

package com.nemetz.ble2cloud.ui.base

import androidx.lifecycle.ViewModelProvider
import com.nemetz.ble2cloud.BLE2CloudApplication

class BaseViewModelFactory(private val mApplication: BLE2CloudApplication): ViewModelProvider.NewInstanceFactory() {
//
//    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
//        when(modelClass){
//            ScannerViewModel::class.java -> return ScannerViewModel(mApplication) as T
//            else -> return super.create(modelClass)
//        }
//    }
}