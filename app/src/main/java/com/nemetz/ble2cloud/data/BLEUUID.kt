package com.nemetz.ble2cloud.data

import java.util.*

object BLEUUID {

    val CONFIG: UUID by lazy{
        fromCharactersitic("2902")
    }

    fun fromCharactersitic(characteristic: String): UUID {
        if (characteristic.length > 4)
            return UUID.fromString("00000000-0000-1000-8000-00805f9b34fb")

        return UUID.fromString("0000$characteristic-0000-1000-8000-00805f9b34fb")
    }
}