package com.nemetz.ble2cloud.connection

import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.util.Log
import kotlinx.coroutines.delay

class BLEScanner(val context: Context) {
    private val SCAN_PERIOD: Long = 3000

    private var mScanning = false
    private var bluetoothLeScanner: BluetoothLeScanner? =
        (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter?.bluetoothLeScanner
    var isReady = false

    init {
        isReady = if (bluetoothLeScanner == null) {
            Log.d("BLEScanner", "Scanner failed to initialize!")
            false
        } else {
            true
        }
    }

    fun stopScan(scanCallback: ScanCallback) {
        bluetoothLeScanner?.stopScan(scanCallback)
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

        if (!mScanning) {
            bluetoothLeScanner!!.startScan(scanFilter, scanSettings, scanCallback)
            mScanning = true

            delay(scanPeriod)

            bluetoothLeScanner!!.stopScan(scanCallback)
            mScanning = false
        }

        return true
    }
}

//object BLEScanner {
//    private const val SCAN_PERIOD: Long = 3000
//
//    private var mScanning = false
//    private var bluetoothLeScanner: BluetoothLeScanner? = null
//    var isReady = false
//
//    fun setUp(context: Context) {
//        bluetoothLeScanner =
//            (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter?.bluetoothLeScanner
//        if (bluetoothLeScanner != null) {
//            Log.d("BLEScanner", "Scanner ready!")
//            isReady = true
//        }
//    }
//
//    fun stopScan(scanCallback: ScanCallback) {
//        bluetoothLeScanner?.stopScan(scanCallback)
//    }
//
//    suspend fun scanLeDevice(
//        scanFilter: List<ScanFilter>,
//        scanSettings: ScanSettings,
//        scanCallback: ScanCallback,
//        scanPeriod: Long = SCAN_PERIOD
//    ): Boolean {
//        if (bluetoothLeScanner == null) {
//            Log.d("BLEScanner", "Scanner not initialized")
//            return false
//        }
//
//        if (!mScanning) {
//            bluetoothLeScanner!!.startScan(scanFilter, scanSettings, scanCallback)
//            mScanning = true
//
//            delay(scanPeriod)
//
//            bluetoothLeScanner!!.stopScan(scanCallback)
//            mScanning = false
//        }
//
//        return true
//    }
//}