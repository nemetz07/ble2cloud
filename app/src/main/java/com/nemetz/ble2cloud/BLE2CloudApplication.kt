package com.nemetz.ble2cloud

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.nemetz.ble2cloud.data.CloudConnector
import com.nemetz.ble2cloud.data.FireabaseRepo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import net.danlew.android.joda.JodaTimeAndroid
import java.text.SimpleDateFormat
import java.util.*

val uiScope = CoroutineScope(Dispatchers.Main)
val ioScope = CoroutineScope(Dispatchers.IO)

@SuppressLint("ConstantLocale")
val SDF: SimpleDateFormat = SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.getDefault())

class BLE2CloudApplication : Application() {
    val TAG = "BLE2CLOUD_APPLICATION"

    lateinit var firestore: FirebaseFirestore
    lateinit var cloudConnector: CloudConnector
    lateinit var firebaseRepo: FireabaseRepo

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Application onCreate")

        JodaTimeAndroid.init(this)
        initFirestore()
    }

    private fun initFirestore(){
        firestore = FirebaseFirestore.getInstance()
        cloudConnector = CloudConnector(firestore)
        firebaseRepo = FireabaseRepo(firestore)
    }
}