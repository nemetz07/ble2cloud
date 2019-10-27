@file:Suppress("UNCHECKED_CAST")

package com.nemetz.ble2cloud.ui.base.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nemetz.ble2cloud.BLEApplication
import com.nemetz.ble2cloud.ui.scanner.viewmodel.ScannerViewModel

class BaseViewModelFactory(private val mApplication: BLEApplication): ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        when(modelClass){
            ScannerViewModel::class.java -> return ScannerViewModel(mApplication) as T
            else -> return super.create(modelClass)
        }
    }
}