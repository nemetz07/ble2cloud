package com.nemetz.ble2cloud.ui.deviceBrowser.viewmodel

import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import com.nemetz.ble2cloud.BLEApplication
import com.nemetz.ble2cloud.connection.BLEScanner
import com.nemetz.ble2cloud.connection.MyScanFilter
import com.nemetz.ble2cloud.connection.MyScanSettings
import com.nemetz.ble2cloud.data.DevicesRepository
import com.nemetz.ble2cloud.event.DeviceConnectedEvent
import com.nemetz.ble2cloud.ui.deviceBrowser.model.DeviceCell
import org.greenrobot.eventbus.EventBus
import java.util.*

class DeviceBrowserViewModel(application: BLEApplication) : AndroidViewModel(application) {
    private val TAG = "DEVICE_BROWSER_VIEWMODEL"

    private var cellDevices: MutableList<DeviceCell> = mutableListOf()
    private var context: Context = getApplication<BLEApplication>().applicationContext
    private var bleScanner: BLEScanner

    private var scanCallback = object : ScanCallback() {
        @RequiresApi(Build.VERSION_CODES.O)
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            if (result.device != null) {
                if(!cellDevices.any{ it.device.address == result.device.address }){
                    DevicesRepository.addDevice(result.device)

                    cellDevices.add(
                        DeviceCell(
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

    init {
        bleScanner = BLEScanner
            .Builder(context)
            .setScanFilter(MyScanFilter.SCAN_FILTER_EMPTY)
            .setScanSettings(MyScanSettings.SCAN_SETTINGS_LOW_ENERGY)
            .setScanCallback(scanCallback)
            .build()
    }

    fun getDevices(): ArrayList<DeviceCell> {
        return cellDevices as ArrayList<DeviceCell>
    }

    suspend fun scanDevices(): Boolean {
        DevicesRepository.clearDevices()
        cellDevices.clear()
        val isFinished = bleScanner.scanLeDevice(5000)
        cellDevices.sortBy { it.rssi }
        cellDevices.reverse()
        return true
    }

    fun connect(device: BluetoothDevice) {
        var bluetoothGatt = device.connectGatt(context,
            false,
            object : BluetoothGattCallback() {
                override fun onConnectionStateChange(
                    gatt: BluetoothGatt?,
                    status: Int,
                    newState: Int
                ) {
                    when (newState) {
                        BluetoothProfile.STATE_CONNECTED -> {
                            Log.d(TAG, "Connected to Gatt server")
                            gatt?.discoverServices()
                        }
                    }
                }

                override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                    when (status) {
                        BluetoothGatt.GATT_SUCCESS -> {
                            Log.d(TAG, "Service discover success")

                            EventBus.getDefault().post(DeviceConnectedEvent(gatt))

                            for (service in gatt.services) {
                                Log.d(TAG, "SERVICE: ${service.uuid}")
                                for (characteristic in service.characteristics) {
                                    Log.d(TAG, "    -> CHARACTERISTIC: (${characteristic.uuid})")
                                    for (descriptor in characteristic.descriptors) {
                                        Log.d(TAG, "        -> DESCRIPTOR: ${descriptor.uuid}")
                                    }
                                }
                            }
                        }
                        else -> Log.w(TAG, "onServicesDiscovered received: $status")
                    }
                }

                override fun onCharacteristicRead(
                    gatt: BluetoothGatt?,
                    characteristic: BluetoothGattCharacteristic?,
                    status: Int
                ) {
                    when (status) {
                        BluetoothGatt.GATT_SUCCESS -> Log.d(TAG, "Characteristic: $characteristic")
                    }
                }

            }
        )
    }

    fun setDevices(newDevices: MutableList<DeviceCell>) {
        cellDevices = newDevices
    }

    fun shuffleDevices() {
        cellDevices.shuffle()
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
//        val mDevices = DevicesRepository.devices
//
//        for (cellDevice in cellDevices) {
//            if (mDevices.any { it.macAddress == cellDevice.device.address }) {
//                Log.d(TAG, "Connect to ${cellDevice.device.name}")
//            }
//        }
    }
}
