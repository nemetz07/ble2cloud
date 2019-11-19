package com.nemetz.ble2cloud

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.DocumentChange
import com.nemetz.ble2cloud.data.BLECharacteristic
import com.nemetz.ble2cloud.data.BLESensor
import com.nemetz.ble2cloud.utils.getMyCharacteristic
import com.nemetz.ble2cloud.utils.getMySensor

class SharedViewModel : ViewModel() {

    private val TAG = "SHARED_VIEWMODEL"

    val BLECharacteristics: ArrayList<BLECharacteristic> = arrayListOf()
    val BLESensors: ArrayList<BLESensor> = arrayListOf()

    fun addSensor(change: DocumentChange?) {
        if (change == null)
            return
        change.getMySensor().let { BLESensors.add(change.newIndex, it) }
    }

    fun modifySensor(change: DocumentChange?) {
        if (change == null)
            return

        if (change.oldIndex == change.newIndex) {
            // Item changed but remained in same position
            BLESensors[change.oldIndex] = change.getMySensor()
        } else {
            // Item changed and changed position
            BLESensors.removeAt(change.oldIndex)
            change.getMySensor().let { BLESensors.add(change.newIndex, it) }
        }
    }

    fun removeSensor(change: DocumentChange?) {
        if (change == null)
            return

        BLESensors.removeAt(change.oldIndex)
    }

    fun addCharacteristic(change: DocumentChange?) {
        if (change == null)
            return
        change.getMyCharacteristic()?.let { BLECharacteristics.add(change.newIndex, it) }
    }

    fun modifyCharacteristic(change: DocumentChange?) {
        if (change == null)
            return

        if (change.oldIndex == change.newIndex) {
            // Item changed but remained in same position
            BLECharacteristics[change.oldIndex] = change.getMyCharacteristic()!!
        } else {
            // Item changed and changed position
            BLECharacteristics.removeAt(change.oldIndex)
            change.getMyCharacteristic()?.let { BLECharacteristics.add(change.newIndex, it) }
        }
    }

    fun removeCharacteristic(change: DocumentChange?) {
        if (change == null)
            return

        BLECharacteristics.removeAt(change.oldIndex)
    }

    fun getMyCharacteristic(uuid: String): BLECharacteristic? {
        return BLECharacteristics.find { it.uuid == uuid }
    }

    fun getMySensor(address: String): BLESensor? {
        return BLESensors.find { it.address == address }
    }
}