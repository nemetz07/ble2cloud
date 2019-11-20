package com.nemetz.ble2cloud

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.DocumentChange
import com.nemetz.ble2cloud.data.BLECharacteristic
import com.nemetz.ble2cloud.data.BLESensor
import com.nemetz.ble2cloud.utils.getBLECharactersitic
import com.nemetz.ble2cloud.utils.getBLESensor

class SharedViewModel : ViewModel() {

    private val TAG = "SHARED_VIEWMODEL"

    val characteristics: ArrayList<BLECharacteristic> = arrayListOf()
    val sensors: ArrayList<BLESensor> = arrayListOf()

    fun addSensor(change: DocumentChange?) {
        if (change == null)
            return
        change.getBLESensor().let { sensors.add(change.newIndex, it) }
    }

    fun modifySensor(change: DocumentChange?) {
        if (change == null)
            return

        if (change.oldIndex == change.newIndex) {
            // Item changed but remained in same position
            sensors[change.oldIndex] = change.getBLESensor()
        } else {
            // Item changed and changed position
            sensors.removeAt(change.oldIndex)
            change.getBLESensor().let { sensors.add(change.newIndex, it) }
        }
    }

    fun removeSensor(change: DocumentChange?) {
        if (change == null)
            return

        sensors.removeAt(change.oldIndex)
    }

    fun addCharacteristic(change: DocumentChange?) {
        if (change == null)
            return
        change.getBLECharactersitic()?.let { characteristics.add(change.newIndex, it) }
    }

    fun modifyCharacteristic(change: DocumentChange?) {
        if (change == null)
            return

        if (change.oldIndex == change.newIndex) {
            // Item changed but remained in same position
            characteristics[change.oldIndex] = change.getBLECharactersitic()!!
        } else {
            // Item changed and changed position
            characteristics.removeAt(change.oldIndex)
            change.getBLECharactersitic()?.let { characteristics.add(change.newIndex, it) }
        }
    }

    fun removeCharacteristic(change: DocumentChange?) {
        if (change == null)
            return

        characteristics.removeAt(change.oldIndex)
    }

    fun getMyCharacteristic(uuid: String): BLECharacteristic? {
        return characteristics.find { it.uuid == uuid }
    }

    fun getMySensor(address: String): BLESensor? {
        return sensors.find { it.address == address }
    }
}