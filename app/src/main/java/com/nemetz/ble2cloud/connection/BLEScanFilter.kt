package com.nemetz.ble2cloud.connection

import android.bluetooth.le.ScanFilter

object BLEScanFilter {

    val SCAN_FILTER_EMPTY = listOf<ScanFilter>(ScanFilter.Builder().build())

}