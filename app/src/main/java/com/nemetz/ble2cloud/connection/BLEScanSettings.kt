package com.nemetz.ble2cloud.connection

import android.bluetooth.le.ScanSettings

object BLEScanSettings {

    val SCAN_SETTINGS_AGGRESSIVE: ScanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .setNumOfMatches(ScanSettings.MATCH_NUM_MAX_ADVERTISEMENT)
        .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
        .setReportDelay(0)
        .build()

    val SCAN_SETTINGS_LOW_ENERGY: ScanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
        .setNumOfMatches(ScanSettings.MATCH_NUM_ONE_ADVERTISEMENT)
        .setMatchMode(ScanSettings.MATCH_MODE_STICKY)
        .build()

    val SCAN_SETTINGS_MATCHONE: ScanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
        .setNumOfMatches(ScanSettings.MATCH_NUM_ONE_ADVERTISEMENT)
        .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
        .setCallbackType(ScanSettings.CALLBACK_TYPE_FIRST_MATCH)
        .build()
}