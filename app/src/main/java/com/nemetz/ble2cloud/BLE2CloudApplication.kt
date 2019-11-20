package com.nemetz.ble2cloud

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.nemetz.ble2cloud.connection.BLEScanner
import com.nemetz.ble2cloud.connection.CloudConnector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import net.danlew.android.joda.JodaTimeAndroid
import java.text.SimpleDateFormat
import java.util.*

val uiScope = CoroutineScope(Dispatchers.Main)
val ioScope = CoroutineScope(Dispatchers.IO)


@SuppressLint("ConstantLocale")
val SDF_FULL: SimpleDateFormat = SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.getDefault())
@SuppressLint("ConstantLocale")
val SDF_TIME: SimpleDateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
@SuppressLint("ConstantLocale")
val SDF_DATE: SimpleDateFormat = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())

const val REQUEST_SIGNIN = 19999

class BLE2CloudApplication : Application() {
    val TAG = "BLE2CLOUD_APPLICATION"

    lateinit var firestore: FirebaseFirestore
    lateinit var cloudConnector: CloudConnector
    lateinit var bleScanner: BLEScanner

    var isServiceRunning: MutableLiveData<Boolean> = MutableLiveData(false)
    var startTime: Timestamp? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Application onCreate")

        JodaTimeAndroid.init(this)
        initFirestore()
        initBLEScanner()
    }

    private fun initBLEScanner() {
        bleScanner = BLEScanner(this)
    }

    private fun initFirestore() {
        firestore = FirebaseFirestore.getInstance()
        cloudConnector = CloudConnector(firestore)
    }
}