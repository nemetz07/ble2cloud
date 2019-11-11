package com.nemetz.ble2cloud.data

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.nemetz.ble2cloud.event.SensorAddedEvent
import com.nemetz.ble2cloud.event.SensorAlreadyExistEvent
import com.nemetz.ble2cloud.ioScope
import com.nemetz.ble2cloud.utils.Collections
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.joda.time.DateTime

class CloudConnector(val firestore: FirebaseFirestore) {
    private val TAG = "CLOUD_CONNECTOR"

    private var sensorsReference: CollectionReference = firestore.collection(Collections.SENSORS)
    private var characteristicsReference: CollectionReference =
        firestore.collection(Collections.CHARACTERISTICS)

//    fun getDataForSensorValue(address: String, name: String): Task<QuerySnapshot> {
//        return getDataForSensorValue(address = address, name = name, limit = 100)
//    }
//
//    fun getDataForSensorValue(address: String, name: String, limit: Long): Task<QuerySnapshot> {
//        return getDataForSensorValue(
//            address = address,
//            name = name,
//            limit = limit,
//            startDateTime = DateTime.now().minusHours(2)
//        )
//    }
//
//    fun getDataForSensorValue(
//        address: String,
//        name: String,
//        limit: Long,
//        startDateTime: DateTime
//    ): Task<QuerySnapshot> {
//        return getDataForSensorValue(address=address, name=name, limit=limit, startDateTime=startDateTime, endDateTime=DateTime.now())
//    }

    fun getDataForSensorValue(
        address: String,
        name: String,
        limit: Long = 25,
        startTimestamp: Timestamp = Timestamp(DateTime.now().minusDays(1).toDate()),
        endTimestamp: Timestamp = Timestamp(DateTime.now().toDate())
    ): Task<QuerySnapshot> {
        Log.d(TAG, "Start: $startTimestamp")
        Log.d(TAG, "End: $endTimestamp")

        return sensorsReference.document(address).collection(Collections.VALUES).document(name)
            .collection(Collections.DATA).orderBy("createdAt", Query.Direction.DESCENDING)
            .whereGreaterThanOrEqualTo("createdAt", startTimestamp)
            .whereLessThanOrEqualTo("createdAt", endTimestamp)
            .limit(limit).get()
    }

    fun saveSensor(sensor: MySensor) {
        ioScope.launch {
            sensorsReference.whereEqualTo("address", sensor.address).get().addOnSuccessListener {
                if (it.isEmpty) {
                    sensorsReference.document(sensor.address).set(
                        mapOf(
                            "address" to sensor.address,
                            "name" to sensor.name,
                            "values" to sensor.values
                        )
                    ).addOnSuccessListener {
                        Log.d(TAG, "Sensor added")
                        sensor.values.forEach { sensorValue ->
                            sensorsReference.document(sensor.address).collection(Collections.VALUES)
                                .document(sensorValue.format!!.name).set(
                                    mapOf(
                                        "name" to sensorValue.format!!.name,
                                        "unit" to sensorValue.format!!.unit,
                                        "uuid" to sensorValue.uuid,
                                        "format" to sensorValue.format!!.format,
                                        "offset" to sensorValue.format!!.offset,
                                        "substring_start" to sensorValue.format!!.substring_start,
                                        "substring_end" to sensorValue.format!!.substring_end
                                    )
                                )
                        }
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

    fun saveCharacteristic(myCharacteristic: MyCharacteristic) {
        ioScope.launch {
            val uuid = myCharacteristic.uuid ?: return@launch
            val values = mutableMapOf<String, Map<String, String?>>()

            myCharacteristic.data.forEach { myDataFormat ->
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
                    "values" to values
                )
            )
        }
    }

    fun saveData(address: String, myDataFormat: MyDataFormat, data: SensorData) {
        ioScope.launch {
            sensorsReference.document(address).collection(Collections.VALUES)
                .document(myDataFormat.name).collection(Collections.DATA).add(data)
                .addOnSuccessListener {
                    Log.d(TAG, "DATA added for $address (${data.createdAt}, ${data.value})")
                }
        }
    }
}