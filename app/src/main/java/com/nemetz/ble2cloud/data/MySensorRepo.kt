package com.nemetz.ble2cloud.data

import android.util.Log
import com.google.firebase.firestore.*
import com.nemetz.ble2cloud.BLE2CloudApplication
import com.nemetz.ble2cloud.event.FetchCompletedEvent
import com.nemetz.ble2cloud.ioScope
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus

class MySensorRepo(val application: BLE2CloudApplication) {
    private val TAG = "SENSOR_REPO"

    private val sensors = arrayListOf<MySensor>()

    val complexSensors: MutableList<ComplexSensor> = mutableListOf()

    private var firestore: FirebaseFirestore = application.firestore
    private var sensorsReference: CollectionReference

    init {
        sensorsReference = firestore.collection("sensors")
    }

    fun addSensor(sensor: MySensor){
        ioScope.launch {
            sensorsReference.document(sensor.address).set(sensor, SetOptions.merge())
        }
    }

    fun getSensors(): ArrayList<MySensor>{
        return sensors
    }

    fun fetchSensors(){
        ioScope.launch {
            sensorsReference.get().addOnSuccessListener {document ->
                if(document != null){
                    updateLocalSensors(document)
                } else {
                    Log.d(TAG, "No such document!")
                }
            }
        }
    }

    fun updateLocalSensors(data: QuerySnapshot){
        sensors.clear()

        for(document in data.documents){
            val mData = document.data
            val address = mData?.get("address") as String
            val name = mData.get("name") as String

//            sensors.add(MySensor(
//                address = address,
//                name = name,
//                serviceData =
//            ))
        }

        EventBus.getDefault().post(FetchCompletedEvent())
    }
}