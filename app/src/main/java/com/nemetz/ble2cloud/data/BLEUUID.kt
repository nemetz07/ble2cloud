package com.nemetz.ble2cloud.data

import java.util.*

object BLEUUID {

    fun fromCharactersitic(characteristic: String): UUID? {
        if (characteristic.length > 4)
            return null
        return UUID.fromString("0000$characteristic-0000-1000-8000-00805f9b34fb")
    }
}