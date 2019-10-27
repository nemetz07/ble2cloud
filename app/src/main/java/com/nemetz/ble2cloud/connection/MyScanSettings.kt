package com.nemetz.ble2cloud.connection

import android.bluetooth.le.ScanSettings

object MyScanSettings{

    val SCAN_SETTINGS_AGGRESSIVE = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .setNumOfMatches(ScanSettings.MATCH_NUM_MAX_ADVERTISEMENT)
        .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
        .setReportDelay(0)
        .build()

    val SCAN_SETTINGS_LOW_ENERGY = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
        .setNumOfMatches(ScanSettings.MATCH_NUM_FEW_ADVERTISEMENT)
        .setMatchMode(ScanSettings.MATCH_MODE_STICKY)
        .build()
}