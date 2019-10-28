package com.nemetz.ble2cloud.connection

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.util.Log
import kotlinx.coroutines.delay

object BLEScanner {
    private const val SCAN_PERIOD: Long = 3000

    private var mScanning = false

    private var bluetoothLeScanner: BluetoothLeScanner? = null
    private var bluetoothAdapter: BluetoothAdapter? = null

    fun setUp(context: Context) {
        bluetoothAdapter = (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
        bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner
    }

    suspend fun scanLeDevice(
        scanFilter: List<ScanFilter>,
        scanSettings: ScanSettings,
        scanCallback: ScanCallback,
        scanPeriod: Long = SCAN_PERIOD
    ): Boolean {
        if (bluetoothLeScanner == null) {
            Log.d("BLEScanner", "Scanner not initialized")
            return false
        }

        if (mScanning != true) {
            bluetoothLeScanner!!.startScan(scanFilter, scanSettings, scanCallback)
            mScanning = true

            delay(scanPeriod)

            bluetoothLeScanner!!.stopScan(scanCallback)
            mScanning = false
        }

        return true
    }
}