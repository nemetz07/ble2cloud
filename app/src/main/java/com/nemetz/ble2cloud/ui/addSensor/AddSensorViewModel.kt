package com.nemetz.ble2cloud.ui.addSensor

import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import com.nemetz.ble2cloud.data.CloudConnector
import com.nemetz.ble2cloud.data.MySensor
import com.nemetz.ble2cloud.event.SensorServicesDiscoveredEvent
import org.greenrobot.eventbus.EventBus

class AddSensorViewModel : ViewModel() {

    private val TAG = "ADD_SENSOR_VIEWMODEL"
    lateinit var bluetoothDevice: BluetoothDevice
    var bluetoothGatt: BluetoothGatt? = null
    var services: MutableList<BluetoothGattService>? = null
    var mySensor: MySensor? = null
    val characteristics: ArrayList<CharacteristicCell> = arrayListOf()

    private val gattCallback = object: BluetoothGattCallback(){
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            when(newState){
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
            when(status){
                BluetoothGatt.GATT_SUCCESS -> {
                    Log.d(TAG, "Services discovered for: ${bluetoothDevice.address}")

                    services = gatt?.services
                    EventBus.getDefault().post(SensorServicesDiscoveredEvent())
                }
            }
        }
    }

    fun discoverCharacteristics(context: Context?) {
        bluetoothDevice.connectGatt(context, false, gattCallback)
    }

    fun saveSensor(cloudConnector: CloudConnector) {
        cloudConnector.saveSensor(mySensor!!)
    }
}
