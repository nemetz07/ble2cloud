package com.nemetz.ble2cloud.ui.addSensor

import android.bluetooth.*
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import com.nemetz.ble2cloud.connection.CloudConnector
import com.nemetz.ble2cloud.data.*
import com.nemetz.ble2cloud.event.ServiceDiscoverEndedEvent
import org.greenrobot.eventbus.EventBus

class AddSensorViewModel : ViewModel() {

    private val TAG = "ADD_SENSOR_VIEWMODEL"
    lateinit var bluetoothDevice: BluetoothDevice
    var bluetoothGatt: BluetoothGatt? = null
    var services: MutableList<BluetoothGattService>? = null
    var mySensor: MySensor? = null
    val characteristics: ArrayList<CharacteristicCell> = arrayListOf()

    lateinit var myCharacteristics: ArrayList<MyCharacteristic>

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Log.d(TAG, "Connected to: ${bluetoothDevice.address}")
                    bluetoothGatt = gatt
                    Log.d(TAG, "Discovering services for ${bluetoothDevice.address}")
                    gatt?.discoverServices()
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.d(TAG, "Disconnected from: ${bluetoothDevice.address}")
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    Log.d(TAG, "Services discovered for: ${bluetoothDevice.address}")

                    services = gatt?.services
                    onServicesDiscovered()
                }
            }
        }
    }

    fun onServicesDiscovered() {
        mySensor = MySensor(
            name = bluetoothDevice.name,
            address = bluetoothDevice.address
        )

        services?.forEach {service ->
            service.characteristics.forEach {characteristic ->
                val myCharacteristic = myCharacteristics.find { it.uuid == characteristic.uuid.toString() }
                myCharacteristic?.data?.forEach { dataFormat ->
                    characteristics.add(CharacteristicCell(name = dataFormat.name, uuid = myCharacteristic.uuid ?: "-", unit = dataFormat.unit))

                    EventBus.getDefault().post(ServiceDiscoverEndedEvent())
                }
            }
        }

    }

    fun discoverCharacteristics(context: Context?) {
        bluetoothDevice.connectGatt(context, false, gattCallback)
    }

    fun saveSensor(cloudConnector: CloudConnector) {
        characteristics.forEach {characteristic ->
            if(characteristic.enabled){
                val myCharacteristic = myCharacteristics.find { it.uuid == characteristic.uuid }
                val dataFormat = myCharacteristic?.data?.find { it.name == characteristic.name }
                if(dataFormat != null){
                    val sensorValue = SensorValue(
                        uuid = characteristic.uuid,
                        format = MyDataFormat(
                            name = dataFormat.name,
                            format = dataFormat.format,
                            unit = dataFormat.unit,
                            substring_start = dataFormat.substring_start,
                            substring_end = dataFormat.substring_end,
                            offset = dataFormat.offset
                        )
                    )

                    mySensor?.values?.add(sensorValue)
                }
            }
        }

        cloudConnector.saveSensor(mySensor!!)
    }
}
