package com.nemetz.ble2cloud.data

import android.util.Log
import com.google.firebase.firestore.*


object ComplexSensorRepo {
    private const val TAG = "COMPLEX_SENSOR_REPO"

    private val sensors: MutableList<ComplexSensor> = mutableListOf()
    private var mRegistrations: ArrayList<ListenerRegistration?> = arrayListOf()
    private val mSnapshots = arrayListOf<DocumentSnapshot>()
    private lateinit var mQuery: Query

//    fun startListening() {
//        mRegistration = mQuery.addSnapshotListener(this)
//    }
//
//    fun stopListening() {
//        if (mRegistration != null) {
//            mRegistration!!.remove()
//            mRegistration = null
//        }
//
//        mSnapshots.clear()
//    }


//    override fun onEvent(querySnapshot: QuerySnapshot?, e: FirebaseFirestoreException?) {
//        // Handle errors
//        if (e != null) {
//            Log.w(TAG, "onEvent:error", e)
//            return
//        }
//
//        for (change in querySnapshot!!.documentChanges) {
//            val snapshot = change.document
//            when (change.type) {
//                DocumentChange.Type.ADDED -> {
//                    Log.w(TAG, "SENSOR added")
//                    onSensorAdded(change)
//                }
//                DocumentChange.Type.MODIFIED -> {
//                    Log.w(TAG, "SENSOR modified")
//                    onSensorModified(change)
//                }
//                DocumentChange.Type.REMOVED -> {
//                    Log.w(TAG, "SENSOR removed")
//                    onSensorRemoved(change)
//                }
//            }
//        }
//    }

    fun getSensors(): MutableList<ComplexSensor> {
        return sensors
    }

    fun getSensor(position: Int): ComplexSensor {
        return sensors[position]
    }

    fun addSensor(sensor: ComplexSensor){
        sensors.add(sensor)
    }

    fun containsAddress(address: String): Boolean {
        return sensors.any{ it.mySensor.address == address }
    }

    fun sortByRSSI() {
        sensors.apply {
            sortBy { it.rssi }
            reverse()
        }
    }
}
