package com.nemetz.ble2cloud.ui.home

import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import com.nemetz.ble2cloud.connection.BLEScanner
import com.nemetz.ble2cloud.connection.MyScanSettings
import com.nemetz.ble2cloud.data.*
import com.nemetz.ble2cloud.event.ScanCompleteEvent
import com.nemetz.ble2cloud.ioScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.joda.time.DateTime
import java.util.*

val HRM_SERVICE = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb")
val HRM_CHARACTERISTIC = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb")
val XI_SERVICE = UUID.fromString("226c0000-6476-4566-7562-66734470666d")

val DESCRIPTION_INFO = UUID.fromString("00002901-0000-1000-8000-00805f9b34fb")
val DESCRIPTOR_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

class HomeViewModel : ViewModel() {
    private val TAG = "HOME_VIEWMODEL"

    var sensors: ArrayList<MySensor> = arrayListOf()
    var myCharacteristics: ArrayList<MyCharacteristic> = arrayListOf()
    val foundSensors: ArrayList<ComplexSensor> = arrayListOf()
    var cloudConnector: CloudConnector? = null

    var dataTimes: MutableMap<String, DateTime> = mutableMapOf()

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            if (result?.device != null) {
                Log.d(TAG, "Sensor found!: ${result.device.address}")
                if (!foundSensors.any { it.mySensor.address == result.device.address }) {
                    val mySensor = sensors.find { it.address == result.device.address }!!

                    foundSensors.add(
                        ComplexSensor(
                            mySensor = mySensor,
                            bluetoothDevice = result.device,
                            rssi = result.rssi
                        )
                    )
                }
            }
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Log.d(TAG, "CONNECTED to ${gatt?.device?.address}")
                    foundSensors.find { it.bluetoothDevice.address == gatt?.device?.address }
                        ?.bluetoothGatt = gatt
                    gatt?.discoverServices()
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.d(TAG, "DISCONNECTED from ${gatt?.device?.address}")
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    val sensor =
                        foundSensors.find { it.bluetoothDevice.address == gatt.device?.address }
                            ?: return
                    sensor.services = gatt.services

                    gatt.services.forEach { service ->
                        service.characteristics.forEach { characteristic ->
                            if (sensor.mySensor.values.any { it.uuid == characteristic.uuid.toString() }) {
                                characteristic.descriptors.forEach { descriptor ->
                                    if (descriptor.uuid == DESCRIPTOR_CONFIG) {
                                        gatt.readDescriptor(descriptor)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        override fun onDescriptorRead(
            gatt: BluetoothGatt?,
            descriptor: BluetoothGattDescriptor?,
            status: Int
        ) {
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    if (gatt?.device?.address != null) {
                        foundSensors.find { it.mySensor.address == gatt.device.address }
                            ?.enableNotification(descriptor)
                    }
                }
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?
        ) {
            gatt?.device?.address?.let { dataRead(it, characteristic) }
        }
    }

    fun initCharacteristics() {

        myCharacteristics.add(
            MyCharacteristic(
                uuid = "00002a37-0000-1000-8000-00805f9b34fb",
                data = arrayListOf(
                    MyDataFormat(
                        format = "UINT8",
                        offset = 1,
                        name = "Heart rate",
                        unit = "BPM"
                    )
                )
            )
        )

        myCharacteristics.add(
            MyCharacteristic(
                uuid = "226caa55-6476-4566-7562-66734470666d",
                data = arrayListOf(
                    MyDataFormat(
                        name = "Temperature",
                        unit = "C",
                        format = "STRING",
                        substring_start = 2,
                        substring_end = 6
                    ),
                    MyDataFormat(
                        name = "Humidity",
                        unit = "%",
                        format = "STRING",
                        substring_start = 9,
                        substring_end = 13
                    )
                )
            )
        )
    }

    fun startDataGathering() {
        val scanFilters = arrayListOf<ScanFilter>()

        for (sensor in sensors) {
            scanFilters.add(ScanFilter.Builder().setDeviceAddress(sensor.address).build())
        }

        Log.d(TAG, "Filters: $scanFilters")

        ioScope.launch {
            BLEScanner.scanLeDevice(
                scanFilters,
                MyScanSettings.SCAN_SETTINGS_MATCHONE,
                scanCallback,
                5000
            )
        }.invokeOnCompletion {
            Log.d(TAG, "FOUND Sensors: $foundSensors")
            EventBus.getDefault().post(ScanCompleteEvent())
            ioScope.launch {
                delay(10000)
                startDataGathering()
            }
        }
    }

    fun connectToSensors(context: Context) {
        for (sensor in foundSensors) {
            sensor.connect(context, gattCallback)
        }
    }

    fun disconnectSensors() {
        foundSensors.forEach {
            it.disconnect()
        }
    }

    fun dataRead(
        address: String,
        characteristic: BluetoothGattCharacteristic?
    ) {
        val sensor = foundSensors.find { it.bluetoothDevice.address == address } ?: return

        if (characteristic == null) {
            sensor.disableNotification(characteristic)
            sensor.disconnect()
            return
        }

        sensor.mySensor.values.forEach { sensorValue ->
            val data: SensorData

            when (sensorValue.format?.format) {
                "STRING" -> {
                    data = processStringData(sensorValue.format!!, characteristic)
                }
                "UINT8", "UINT16", "UINT32", "SINT8", "SINT16", "SINT32" -> {
                    data = processIntegerData(sensorValue.format!!, characteristic)
                }
                "FLOAT", "SFLOAT" -> {
                    data = processFloatData(sensorValue.format!!, characteristic)
                }
                else -> {
                    Log.d(TAG, "Invalid format: ${sensorValue.format?.format}")
                    return
                }
            }
            saveData(sensor.mySensor.address, sensorValue.format!!, data)
        }

        sensor.disableNotification(characteristic)
        sensor.disconnect()
    }

    private fun processFloatData(
        myDataFormat: MyDataFormat,
        characteristic: BluetoothGattCharacteristic
    ): SensorData {
        val data = characteristic.getFloatValue(myDataFormat.dataFormat(), myDataFormat.offset)

        return SensorData(value = data)
    }

    private fun processIntegerData(
        myDataFormat: MyDataFormat,
        characteristic: BluetoothGattCharacteristic
    ): SensorData {
        val data = characteristic.getIntValue(myDataFormat.dataFormat(), myDataFormat.offset)

        return SensorData(value = data)
    }

    private fun processStringData(
        myDataFormat: MyDataFormat,
        characteristic: BluetoothGattCharacteristic
    ): SensorData {
        val dataString = characteristic.getStringValue(myDataFormat.offset)
        var data = "-"
        data = if (myDataFormat.substring_end != null || myDataFormat.substring_end != 0) {
            dataString.substring(myDataFormat.substring_start!!, myDataFormat.substring_end!!)
        } else {
            dataString
        }

        return SensorData(value = data)
    }

    private fun saveData(address: String, myDataFormat: MyDataFormat, data: SensorData) {
        if (!dataTimes.containsKey("{$address}:${myDataFormat.name}")) {
            Log.d(TAG, "${myDataFormat.name}: ${data.value} ${myDataFormat.unit}")
            cloudConnector?.saveData(address, myDataFormat, data)
            dataTimes["{$address}:${myDataFormat.name}"] = DateTime.now()
        } else if (dataTimes["{$address}:${myDataFormat.name}"]?.isBefore(
                DateTime.now().minusMinutes(
                    1
                )
            )!!
        ) {
            Log.d(TAG, "${myDataFormat.name}: ${data.value} ${myDataFormat.unit}")
            cloudConnector?.saveData(address, myDataFormat, data)
            dataTimes["{$address}:${myDataFormat.name}"] = DateTime.now()
        } else {
            Log.d(
                TAG,
                "Data already collected in a minute ${myDataFormat.name}: ${data.value} ${myDataFormat.unit}"
            )
        }
    }
}
