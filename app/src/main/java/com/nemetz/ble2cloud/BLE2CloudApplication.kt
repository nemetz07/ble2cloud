package com.nemetz.ble2cloud

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.nemetz.ble2cloud.connection.CloudConnector
import com.nemetz.ble2cloud.data.FirebaseRepo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import net.danlew.android.joda.JodaTimeAndroid
import java.text.SimpleDateFormat
import java.util.*

val uiScope = CoroutineScope(Dispatchers.Main)
val ioScope = CoroutineScope(Dispatchers.IO)

var isServiceRunning: Boolean = false

var OPEN_SERVICE = "OPEN_SERVICE"

@SuppressLint("ConstantLocale")
val SDF_FULL: SimpleDateFormat = SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.getDefault())
@SuppressLint("ConstantLocale")
val SDF_TIME: SimpleDateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
@SuppressLint("ConstantLocale")
val SDF_DATE: SimpleDateFormat = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())

const val RC_SIGN_IN = 19999

class BLE2CloudApplication : Application() {
    val TAG = "BLE2CLOUD_APPLICATION"

    lateinit var firestore: FirebaseFirestore
    lateinit var cloudConnector: CloudConnector
    lateinit var firebaseRepo: FirebaseRepo

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Application onCreate")

        JodaTimeAndroid.init(this)
        initFirestore()
    }

    private fun initFirestore(){
        firestore = FirebaseFirestore.getInstance()
        cloudConnector = CloudConnector(firestore)
        firebaseRepo = FirebaseRepo(firestore)
    }
}