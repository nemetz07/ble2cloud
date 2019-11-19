package com.nemetz.ble2cloud.ui.scanner

import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import com.nemetz.ble2cloud.connection.BLEScanner
import com.nemetz.ble2cloud.connection.MyScanFilter
import com.nemetz.ble2cloud.connection.MyScanSettings
import com.nemetz.ble2cloud.data.*

class ScannerViewModel : ViewModel() {
    private val TAG = "SCANNER_VIEWMODEL"

    val complexSensors: ArrayList<ComplexSensor> = arrayListOf()
    val cellSensors: ArrayList<ScannerCell> = arrayListOf()
    var isAlreadyScanned: Boolean = false

    private var scanCallback = object : ScanCallback() {
        @RequiresApi(Build.VERSION_CODES.O)
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
        return this.any{ it.BLESensor.address == address }
    }

    private fun ArrayList<ComplexSensor>.sortByRSSI(){
        sortBy { it.rssi }
        reverse()
    }

    private fun updateCellSensors(){
        cellSensors.clear()

        for (sensor in complexSensors){
            cellSensors.add(ScannerCell(
                name = sensor.BLESensor.name,
                address = sensor.BLESensor.address,
                rssi = sensor.rssi
            ))
        }
    }

    suspend fun scanSensors(): Boolean {
        complexSensors.clear()

        val isFinished = BLEScanner.scanLeDevice(
            MyScanFilter.SCAN_FILTER_EMPTY,
            MyScanSettings.SCAN_SETTINGS_LOW_ENERGY,
            scanCallback,
            3000
        )

        complexSensors.sortByRSSI()
        updateCellSensors()

        return true
    }

//    fun connect(device: BluetoothDevice) {
//        var bluetoothGatt = device.connectGatt(context,
//            false,
//            object : BluetoothGattCallback() {
//                override fun onConnectionStateChange(
//                    gatt: BluetoothGatt?,
//                    status: Int,
//                    newState: Int
//                ) {
//                    when (newState) {
//                        BluetoothProfile.STATE_CONNECTED -> {
//                            Log.d(TAG, "Connected to Gatt server")
//                            gatt?.discoverServices()
//                        }
//                    }
//                }
//
//                override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
//                    when (status) {
//                        BluetoothGatt.GATT_SUCCESS -> {
//                            Log.d(TAG, "Service discover success")
//
//                            EventBus.getDefault().post(SensorConnectedEvent(gatt))
//
//                            for (service in gatt.services) {
//                                Log.d(TAG, "SERVICE: ${service.uuid}")
//                                for (characteristic in service.BLECharacteristics) {
//                                    Log.d(TAG, "    -> CHARACTERISTIC: (${characteristic.uuid})")
//                                    for (descriptor in characteristic.descriptors) {
//                                        Log.d(TAG, "        -> DESCRIPTOR: ${descriptor.uuid}")
//                                    }
//                                }
//                            }
//                        }
//                        else -> Log.w(TAG, "onServicesDiscovered received: $status")
//                    }
//                }
//
//                override fun onCharacteristicRead(
//                    gatt: BluetoothGatt?,
//                    characteristic: BluetoothGattCharacteristic?,
//                    status: Int
//                ) {
//                    when (status) {
//                        BluetoothGatt.GATT_SUCCESS -> Log.d(TAG, "Characteristic: $characteristic")
//                    }
//                }
//
//            }
//        )
//    }

//    fun setSensors(newSensors: ArrayList<ScannerCell>) {
//        cellSensors = newSensors
//    }
//
//    fun shuffleSensors() {
//        cellSensors.shuffle()
//    }

//    fun autoConnect() {
////        val mDevices = SensorRepository.devices
////
////        for (cellDevice in cellSensors) {
////            if (mDevices.any { it.macAddress == cellDevice.sensor.address }) {
////                Log.d(TAG, "Connect to ${cellDevice.sensor.name}")
////            }
////        }
//    }
}
