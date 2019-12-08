package com.nemetz.ble2cloud.ui.addSensor

import android.bluetooth.*
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.nemetz.ble2cloud.connection.CloudConnector
import com.nemetz.ble2cloud.data.BLECharacteristic
import com.nemetz.ble2cloud.data.BLEDataFormat
import com.nemetz.ble2cloud.data.BLESensor
import com.nemetz.ble2cloud.data.BLESensorValue
import com.nemetz.ble2cloud.event.ServicesDiscoveredEvent
import org.greenrobot.eventbus.EventBus
import org.joda.time.DateTime

class AddSensorViewModel : ViewModel() {

    private val TAG = "ADD_SENSOR_VIEWMODEL"
    var bluetoothDevice: BluetoothDevice? = null
    var bluetoothGatt: BluetoothGatt? = null
    var services: MutableList<BluetoothGattService>? = null
    var sensor: BLESensor? = null
    val cellCharacteristics: ArrayList<CharacteristicCell> = arrayListOf()

    lateinit var charactersitics: ArrayList<BLECharacteristic>

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Log.d(TAG, "Connected to: ${bluetoothDevice?.address}")
                    bluetoothGatt = gatt
                    Log.d(TAG, "Discovering services for ${bluetoothDevice?.address}")
                    gatt?.discoverServices()
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.d(TAG, "Disconnected from: ${bluetoothDevice?.address}")
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    Log.d(TAG, "Services discovered for: ${bluetoothDevice?.address}")

                    services = gatt?.services
                    onServicesDiscovered()
                }
            }
        }
    }

    fun onServicesDiscovered() {
        sensor = BLESensor(
            name = bluetoothDevice?.name ?: "-",
            address = bluetoothDevice?.address ?: "00:00:00:00:00:00"
        )

        services?.forEach { service ->
            service.characteristics.forEach { characteristic ->
                val bleCharacteristic =
                    charactersitics.find { it.uuid == characteristic.uuid.toString() }
                bleCharacteristic?.data?.forEach { dataFormat ->
                    cellCharacteristics.add(
                        CharacteristicCell(
                            name = dataFormat.name,
                            uuid = bleCharacteristic.uuid ?: "-",
                            unit = dataFormat.unit
                        )
                    )

                    EventBus.getDefault().post(ServicesDiscoveredEvent())
                }
            }
        }
    }


    fun discoverCharacteristics(context: Context) {
        bluetoothDevice?.connectGatt(context, false, gattCallback) ?: Log.d(
            TAG,
            "Can't connect ot sensor because it is null"
        )
    }

    fun saveSensor(cloudConnector: CloudConnector) {
        cellCharacteristics.forEach { characteristic ->
            if (characteristic.enabled) {
                val myCharacteristic = charactersitics.find { it.uuid == characteristic.uuid }
                val dataFormat = myCharacteristic?.data?.find { it.name == characteristic.name }
                if (dataFormat != null) {
                    val sensorValue = BLESensorValue(
                        uuid = characteristic.uuid,
                        format = BLEDataFormat(
                            name = dataFormat.name,
                            format = dataFormat.format,
                            unit = dataFormat.unit,
                            substring_start = dataFormat.substring_start,
                            substring_end = dataFormat.substring_end,
                            offset = dataFormat.offset
                        )
                    )

                    sensor?.apply {
                        values[sensorValue.format?.name ?: "-"] = sensorValue
                        createdAt = Timestamp(DateTime.now().toDate())
                        createdBy = FirebaseAuth.getInstance().uid ?: "-"
                    }
                }
            }
        }

        cloudConnector.saveSensor(sensor!!)
    }
}
