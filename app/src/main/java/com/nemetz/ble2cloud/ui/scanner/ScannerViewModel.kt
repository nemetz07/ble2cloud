package com.nemetz.ble2cloud.ui.scanner

import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.util.Log
import androidx.lifecycle.ViewModel
import com.nemetz.ble2cloud.connection.BLEScanFilter
import com.nemetz.ble2cloud.connection.BLEScanSettings
import com.nemetz.ble2cloud.connection.BLEScanner
import com.nemetz.ble2cloud.data.BLESensor
import com.nemetz.ble2cloud.data.ComplexSensor

class ScannerViewModel : ViewModel() {
    private val TAG = "SCANNER_VIEWMODEL"

    val complexSensors: ArrayList<ComplexSensor> = arrayListOf()
    val cellSensors: ArrayList<ScannerCell> = arrayListOf()
    var isAlreadyScanned: Boolean = false

    private var scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            if (result.device != null) {
                if (!complexSensors.containsAddress(result.device.address)) {
                    Log.d(TAG, "SENSOR Found: $result")

                    val mySensor = BLESensor(
                        address = result.device.address,
                        name = result.device.name ?: "-"
                    )

                    complexSensors.add(ComplexSensor(mySensor, result.device, result.rssi))
                }
            }
            super.onScanResult(callbackType, result)
        }
    }

    private fun ArrayList<ComplexSensor>.containsAddress(address: String): Boolean {
        return this.any { it.BLESensor.address == address }
    }

    private fun ArrayList<ComplexSensor>.sortByRSSI() {
        sortBy { it.rssi }
        reverse()
    }

    private fun updateCellSensors() {
        cellSensors.clear()

        for (sensor in complexSensors) {
            cellSensors.add(
                ScannerCell(
                    name = sensor.BLESensor.name,
                    address = sensor.BLESensor.address,
                    rssi = sensor.rssi
                )
            )
        }
    }

    suspend fun scanSensors(bleScanner: BLEScanner): Boolean {
        complexSensors.clear()

        val isFinished = bleScanner.scanLeDevice(
            BLEScanFilter.SCAN_FILTER_EMPTY,
            BLEScanSettings.SCAN_SETTINGS_AGGRESSIVE,
            scanCallback,
            5000
        )

        complexSensors.sortByRSSI()
        updateCellSensors()

        return isFinished
    }
}
