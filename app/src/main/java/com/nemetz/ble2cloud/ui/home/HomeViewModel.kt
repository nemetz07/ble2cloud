package com.nemetz.ble2cloud.ui.home

import androidx.lifecycle.ViewModel
import java.util.*

val HRM_SERVICE = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb")
val HRM_CHARACTERISTIC = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb")
val XI_SERVICE = UUID.fromString("226c0000-6476-4566-7562-66734470666d")

val DESCRIPTION_INFO = UUID.fromString("00002901-0000-1000-8000-00805f9b34fb")
val DESCRIPTOR_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

class HomeViewModel : ViewModel() {
    private val TAG = "HOME_VIEWMODEL"

//    var BLECharacteristics: ArrayList<BLECharacteristic> = arrayListOf()

//    fun initCharacteristics() {
//
//        BLECharacteristics.add(
//            BLECharacteristic(
//                uuid = "00002a37-0000-1000-8000-00805f9b34fb",
//                data = arrayListOf(
//                    BLEDataFormat(
//                        format = "UINT8",
//                        offset = 1,
//                        name = "Heart rate",
//                        unit = "BPM"
//                    )
//                )
//            )
//        )
//
//        BLECharacteristics.add(
//            BLECharacteristic(
//                uuid = "226caa55-6476-4566-7562-66734470666d",
//                data = arrayListOf(
//                    BLEDataFormat(
//                        name = "Temperature",
//                        unit = "C",
//                        format = "STRING",
//                        substring_start = 2,
//                        substring_end = 6
//                    ),
//                    BLEDataFormat(
//                        name = "Humidity",
//                        unit = "%",
//                        format = "STRING",
//                        substring_start = 9,
//                        substring_end = 13
//                    )
//                )
//            )
//        )
//    }
}
