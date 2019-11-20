package com.nemetz.ble2cloud.connection

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.nemetz.ble2cloud.data.*
import com.nemetz.ble2cloud.event.SensorAddedEvent
import com.nemetz.ble2cloud.event.SensorAlreadyExistEvent
import com.nemetz.ble2cloud.ioScope
import com.nemetz.ble2cloud.utils.FirebaseCollections
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.joda.time.DateTime

class CloudConnector(val firestore: FirebaseFirestore) {
    private val TAG = "CLOUD_CONNECTOR"

    private var sensorsReference: CollectionReference =
        firestore.collection(FirebaseCollections.SENSORS)
    private var characteristicsReference: CollectionReference =
        firestore.collection(FirebaseCollections.CHARACTERISTICS)

    fun getDataBetween(
        address: String,
        name: String,
        limit: Long = 25,
        startTimestamp: Timestamp = Timestamp(DateTime.now().minusDays(1).toDate()),
        endTimestamp: Timestamp = Timestamp(DateTime.now().toDate())
    ): Task<QuerySnapshot> {
        Log.d(TAG, "Start: $startTimestamp")
        Log.d(TAG, "End: $endTimestamp")

        return sensorsReference.document(address).collection(FirebaseCollections.VALUES)
            .document(name)
            .collection(FirebaseCollections.DATA).orderBy("createdAt", Query.Direction.DESCENDING)
            .whereGreaterThanOrEqualTo("createdAt", startTimestamp)
            .whereLessThanOrEqualTo("createdAt", endTimestamp)
            .limit(limit).get()
    }

    fun getData(
        address: String,
        name: String,
        limit: Long = 25
    ): Task<QuerySnapshot> {
        return sensorsReference.document(address).collection(FirebaseCollections.VALUES)
            .document(name)
            .collection(FirebaseCollections.DATA).orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(limit).get()
    }

    fun updateSensorAlert(
        address: String,
        sensorValue: BLESensorValue
    ) {
        sensorsReference.whereEqualTo("address", address).get().addOnSuccessListener {
            if (!it.isEmpty) {
                sensorsReference.document(address).set(mapOf("values" to mapOf(sensorValue.format?.name to sensorValue)), SetOptions.merge())

                sensorsReference.document(address).collection(FirebaseCollections.VALUES).document(
                    sensorValue.format!!.name
                ).set (mapOf("min" to sensorValue.min, "max" to sensorValue.max), SetOptions.merge())
            }
        }
    }

    fun saveSensor(sensor: BLESensor) {
        ioScope.launch {
            sensorsReference.whereEqualTo("address", sensor.address).get().addOnSuccessListener {
                if (it.isEmpty) {
                    sensorsReference.document(sensor.address).set(
                        mapOf(
                            "address" to sensor.address,
                            "name" to sensor.name,
                            "values" to sensor.values,
                            "createdAt" to Timestamp.now(),
                            "createdBy" to FirebaseAuth.getInstance().uid
                        )
                    ).addOnSuccessListener {
                        Log.d(TAG, "Sensor added")
                        sensor.values.forEach { (name, sensorValue) ->
                            sensorsReference.document(sensor.address)
                                .collection(FirebaseCollections.VALUES)
                                .document(name).set(sensorValue)
                        }

//                        sensor.values.forEach { sensorValue ->
//                            sensorsReference.document(sensor.address)
//                                .collection(FirebaseCollections.VALUES)
//                                .document(sensorValue.format!!.name).set(
//                                    mapOf(
//                                        "name" to sensorValue.format!!.name,
//                                        "unit" to sensorValue.format!!.unit,
//                                        "uuid" to sensorValue.uuid,
//                                        "format" to sensorValue.format!!.format,
//                                        "offset" to sensorValue.format!!.offset,
//                                        "substring_start" to sensorValue.format!!.substring_start,
//                                        "substring_end" to sensorValue.format!!.substring_end
//                                    )
//                                )
//                        }
                    }
                    EventBus.getDefault().post(SensorAddedEvent())
                } else {
                    Log.d(TAG, "Sensor already exist")
                    EventBus.getDefault().post(SensorAlreadyExistEvent())
                }
            }
        }
    }

    fun deleteSensor(document: String?) {
        if (document == null)
            return

        ioScope.launch {
            sensorsReference.document(document)
                .delete()
                .addOnSuccessListener {
                    Log.d(TAG, "Sensor deleted")
                }.addOnFailureListener {
                    Log.d(TAG, "Sensor delete failed")
                }
        }
    }

    fun saveCharacteristic(BLECharacteristic: BLECharacteristic) {
        ioScope.launch {
            val uuid = BLECharacteristic.uuid ?: return@launch
            val values = mutableMapOf<String, Map<String, String?>>()

            BLECharacteristic.data.forEach { myDataFormat ->
                val myMap = mutableMapOf(
                    "name" to myDataFormat.name,
                    "unit" to myDataFormat.unit,
                    "format" to myDataFormat.format,
                    "offset" to myDataFormat.offset.toString()
                )

                if (myDataFormat.substring_start != null) myMap["substring_start"] =
                    myDataFormat.substring_start.toString()
                if (myDataFormat.substring_end != null) myMap["substring_end"] =
                    myDataFormat.substring_end.toString()

                values[myDataFormat.name] = myMap
            }

            characteristicsReference.document(uuid).set(
                mapOf(
                    "uuid" to uuid,
                    "values" to values,
                    "createdAt" to Timestamp.now(),
                    "createdBy" to FirebaseAuth.getInstance().uid
                )
            )
        }
    }

    fun saveData(address: String, BLEDataFormat: BLEDataFormat, sensorData: BLESensorData) {
        ioScope.launch {
            sensorsReference.document(address).collection(FirebaseCollections.VALUES)
                .document(BLEDataFormat.name).collection(FirebaseCollections.DATA).add(sensorData)
                .addOnSuccessListener {
                    Log.d(TAG, "DATA added for $address (${sensorData.createdAt}, ${sensorData.value})")
                }
        }
    }
}