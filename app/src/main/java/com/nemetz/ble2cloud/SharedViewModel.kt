package com.nemetz.ble2cloud

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.DocumentChange
import com.nemetz.ble2cloud.data.MyCharacteristic
import com.nemetz.ble2cloud.data.MySensor
import com.nemetz.ble2cloud.utils.getMyCharacteristic
import com.nemetz.ble2cloud.utils.getMySensor

class SharedViewModel : ViewModel() {

    private val TAG = "SHARED_VIEWMODEL"

    val myCharacteristics: ArrayList<MyCharacteristic> = arrayListOf()
    val mySensors: ArrayList<MySensor> = arrayListOf()

    fun addSensor(change: DocumentChange?) {
        if (change == null)
            return
        change.getMySensor().let { mySensors.add(change.newIndex, it) }
    }

    fun modifySensor(change: DocumentChange?) {
        if (change == null)
            return

        if (change.oldIndex == change.newIndex) {
            // Item changed but remained in same position
            mySensors[change.oldIndex] = change.getMySensor()
        } else {
            // Item changed and changed position
            mySensors.removeAt(change.oldIndex)
            change.getMySensor().let { mySensors.add(change.newIndex, it) }
        }
    }

    fun removeSensor(change: DocumentChange?) {
        if (change == null)
            return

        mySensors.removeAt(change.oldIndex)
    }

    fun addCharacteristic(change: DocumentChange?) {
        if (change == null)
            return
        change.getMyCharacteristic()?.let { myCharacteristics.add(change.newIndex, it) }
    }

    fun modifyCharacteristic(change: DocumentChange?) {
        if (change == null)
            return

        if (change.oldIndex == change.newIndex) {
            // Item changed but remained in same position
            myCharacteristics[change.oldIndex] = change.getMyCharacteristic()!!
        } else {
            // Item changed and changed position
            myCharacteristics.removeAt(change.oldIndex)
            change.getMyCharacteristic()?.let { myCharacteristics.add(change.newIndex, it) }
        }
    }

    fun removeCharacteristic(change: DocumentChange?) {
        if (change == null)
            return

        myCharacteristics.removeAt(change.oldIndex)
    }

    fun getMyCharacteristic(uuid: String): MyCharacteristic? {
        return myCharacteristics.find { it.uuid == uuid }
    }

    fun getMySensor(address: String): MySensor? {
        return mySensors.find { it.address == address }
    }
}