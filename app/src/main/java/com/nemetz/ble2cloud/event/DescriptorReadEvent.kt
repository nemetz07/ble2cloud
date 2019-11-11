package com.nemetz.ble2cloud.event

import android.bluetooth.BluetoothGattDescriptor

class DescriptorReadEvent(
    var address: String,
    var descriptor: BluetoothGattDescriptor?
)