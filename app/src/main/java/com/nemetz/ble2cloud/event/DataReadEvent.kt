package com.nemetz.ble2cloud.event

import android.bluetooth.BluetoothGattCharacteristic

class DataReadEvent(val address: String,
                    val characteristic: BluetoothGattCharacteristic?)