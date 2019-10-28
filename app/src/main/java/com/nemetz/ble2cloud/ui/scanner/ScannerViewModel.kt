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
import com.nemetz.ble2cloud.data.SensorRepository
import java.util.*

class ScannerViewModel : ViewModel() {
    private val TAG = "SCANNER_VIEWMODEL"

    private var cellSensors: MutableList<ScannerCell> = mutableListOf()
//    private var context: Context = getApplication<BLEApplication>().applicationContext

    private var scanCallback = object : ScanCallback() {
        @RequiresApi(Build.VERSION_CODES.O)
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            if (result.device != null) {
                if (!cellSensors.any { it.device.address == result.device.address }) {
                    SensorRepository.addSensor(result.device)

                    cellSensors.add(
                        ScannerCell(
                            result.device,
                            result.rssi,
                            result.isConnectable
                        )
                    )
                }
            }
            super.onScanResult(callbackType, result)
        }
    }

    override fun onCleared() {
        Log.d(TAG, "VIEWMODEL CLEARED")
        super.onCleared()
    }

    fun getSensors(): ArrayList<ScannerCell> {
        return cellSensors as ArrayList<ScannerCell>
    }

    suspend fun scanSensors(): Boolean {
        SensorRepository.clearSensors()
        cellSensors.clear()
        val isFinished = BLEScanner.scanLeDevice(
            MyScanFilter.SCAN_FILTER_EMPTY,
            MyScanSettings.SCAN_SETTINGS_LOW_ENERGY,
            scanCallback
        )
        cellSensors.sortBy { it.rssi }
        cellSensors.reverse()
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
//                                for (characteristic in service.characteristics) {
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

    fun setSensors(newSensors: MutableList<ScannerCell>) {
        cellSensors = newSensors
    }

    fun shuffleSensors() {
        cellSensors.shuffle()
    }

    fun getRandomMacAddress(): String {
        var mac = ""
        val r = Random()
        for (i in 0..5) {
            val n: Int = r.nextInt(255)
            mac += "${String.format("%02x", n)}:"
        }
        return mac.removeSuffix(":").toUpperCase()
    }

    fun autoConnect() {
//        val mDevices = SensorRepository.devices
//
//        for (cellDevice in cellSensors) {
//            if (mDevices.any { it.macAddress == cellDevice.sensor.address }) {
//                Log.d(TAG, "Connect to ${cellDevice.sensor.name}")
//            }
//        }
    }
}
