package com.nemetz.ble2cloud.connection

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanSettings
import android.content.Context
import com.nemetz.ble2cloud.ui.scanner.model.SensorCell
import kotlinx.coroutines.delay

class BLEScanner private constructor(
    val context: Context,
    val scanFilter: List<ScanFilter>?,
    val scanSettings: ScanSettings?,
    val scanCallback: ScanCallback?
) {
    data class Builder(
        val context: Context
    ) {

        lateinit var scanFilter: List<ScanFilter>
        lateinit var scanSettings: ScanSettings
        lateinit var scanCallback: ScanCallback

        fun setScanFilter(scanFilter: List<ScanFilter>) = apply { this.scanFilter = scanFilter }
        fun setScanSettings(scanSettings: ScanSettings) = apply { this.scanSettings = scanSettings }
        fun setScanCallback(scanCallback: ScanCallback) = apply { this.scanCallback = scanCallback }
        fun build() = BLEScanner(context, scanFilter, scanSettings, scanCallback)
    }

    val SCAN_PERIOD: Long = 3000

    private var mScanning = false

    private lateinit var bluetoothLeScanner: BluetoothLeScanner
    private var bluetoothAdapter: BluetoothAdapter

    private var cellDevices: MutableList<SensorCell> = mutableListOf()

    init {
        bluetoothAdapter =
            (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
    }

    suspend fun scanLeDevice(scanPeriod: Long): Boolean {
        bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner

        if (mScanning != true) {
            bluetoothLeScanner.startScan(scanFilter, scanSettings, scanCallback)
            mScanning = true

            delay(scanPeriod)

            bluetoothLeScanner.stopScan(scanCallback)
            mScanning = false
        }

        return true
    }
}