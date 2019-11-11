package com.nemetz.ble2cloud.data

import com.google.firebase.firestore.*
import com.nemetz.ble2cloud.ioScope
import kotlinx.coroutines.launch

class FireabaseRepo(var firestore: FirebaseFirestore) {
    private val TAG = "FIREBASE_SENSOR_REPO"

    private var mRegistrations: HashMap<String, ArrayList<ListenerRegistration>> = hashMapOf()
    private var mQueries: HashMap<String, Query> = hashMapOf()

//    suspend fun getValuesForSensor(address: String): MutableList<DocumentSnapshot> {
//        firestore.collectionGroup("values").whereEqualTo("address", address).get().addOnSuccessListener {
//            it.documents
//        }
//    }

    private fun addCollectionQuery(collection: String) {
        mQueries[collection] = firestore.collection(collection)
    }

    fun addListener(
        collection: String,
        listener: EventListener<QuerySnapshot>
    ): ListenerRegistration? {
        if (!mQueries.containsKey(collection)) {
            addCollectionQuery(collection)
        }

        val registration = mQueries[collection]?.addSnapshotListener(listener) ?: return null

        if (mRegistrations[collection] == null) {
            mRegistrations[collection] = arrayListOf()
        }

        mRegistrations[collection]?.add(registration)

        return registration
    }

    fun removeListener(collection: String, registration: ListenerRegistration?) {
        if (registration != null) {
            mRegistrations[collection]?.remove(registration)
        }
    }
}