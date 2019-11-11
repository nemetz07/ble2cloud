package com.nemetz.ble2cloud

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.google.firebase.firestore.*
import com.nemetz.ble2cloud.connection.BLEConnection
import com.nemetz.ble2cloud.connection.BLEScanner
import com.nemetz.ble2cloud.event.*
import com.nemetz.ble2cloud.utils.Collections
import com.nemetz.ble2cloud.utils.getPath
import com.nemetz.ble2cloud.utils.manager.BaseAccessManager
import com.nemetz.ble2cloud.utils.manager.PermissionManager
import com.nemetz.ble2cloud.utils.manager.SharedPreferencesManager
import com.nemetz.ble2cloud.receiver.BluetoothStateChangedReceiver
import com.nemetz.ble2cloud.receiver.LocationStateChangedReceiver
import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe


class MainActivity : AppCompatActivity(), EventListener<QuerySnapshot> {

    private val TAG = "MAIN_ACTIVITY"

    private var btReceiver = BluetoothStateChangedReceiver()
    private var locReceiver = LocationStateChangedReceiver()

    private var mBTEnabled = false
    private var mLocEnabled = false

    private lateinit var bleConnection: BLEConnection
    lateinit var viewModel: SharedViewModel
    private var mCharacteristicRegistration: ListenerRegistration? = null
    private var mSensorsRegistration: ListenerRegistration? = null

    /* Lifecycle START */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProviders.of(this).get(SharedViewModel::class.java)
        bleConnection = BLEConnection(this)


        NavigationUI.setupWithNavController(
            bottom_navbar,
            findNavController(R.id.nav_host_fragment)
        )
    }

    override fun onStart() {
        super.onStart()

        EventBus.getDefault().register(this)

        mCharacteristicRegistration = (application as BLE2CloudApplication).firebaseRepo.addListener(Collections.CHARACTERISTICS, this)
        mSensorsRegistration = (application as BLE2CloudApplication).firebaseRepo.addListener(Collections.SENSORS, this)

        if (PermissionManager.checkLocationPermission(this))
            EventBus.getDefault().post(LocationPermissionAvailableEvent())
    }

    override fun onStop() {
        super.onStop()
        if (mBTEnabled && mLocEnabled) {
            unregisterReceiver(btReceiver)
            unregisterReceiver(locReceiver)
        }

        (application as BLE2CloudApplication).firebaseRepo.removeListener(Collections.CHARACTERISTICS, mCharacteristicRegistration)
        (application as BLE2CloudApplication).firebaseRepo.removeListener(Collections.SENSORS, mSensorsRegistration)

        EventBus.getDefault().unregister(this)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PermissionManager.REQUEST_COARSE_LOCATION -> {
                if (permissions.isNotEmpty() && grantResults.size == permissions.size) {
                    for (i in permissions.indices) {
                        if (Manifest.permission.ACCESS_COARSE_LOCATION.compareTo(permissions[i]) == 0) {
                            SharedPreferencesManager.putLocationPermissionDenied(this)
                            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                                EventBus.getDefault().post(CloseAppEvent())
                            } else {
                                if (!BaseAccessManager.isLocationEnabled(this)) {
                                    BaseAccessManager.requestLocation(this)
                                }
                            }
                        }
                    }
                } else {
                    if (PermissionManager.isLocationPermissionGranted(this)) {
                        EventBus.getDefault().post(LocationPermissionAvailableEvent())
                    } else {
                        EventBus.getDefault().post(CloseAppEvent())
                    }
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onEvent(querySnapshot: QuerySnapshot?, exception: FirebaseFirestoreException?) {
        // Handle errors
        if (exception != null) {
            Log.w(TAG, "onEvent:error", exception)
            return
        }

        for (change in querySnapshot!!.documentChanges) {
            when(change.getPath()){
                Collections.SENSORS -> {
                    when (change.type) {
                        DocumentChange.Type.ADDED -> {
                            viewModel.addSensor(change)
                            Log.w(TAG, "SENSOR added")
                        }
                        DocumentChange.Type.MODIFIED -> {
                            viewModel.modifySensor(change)
                            Log.w(TAG, "SENSOR modified")
                        }
                        DocumentChange.Type.REMOVED -> {
                            viewModel.removeSensor(change)
                            Log.w(TAG, "SENSOR removed")
                        }
                    }
                }
                Collections.CHARACTERISTICS -> {
                    when (change.type) {
                        DocumentChange.Type.ADDED -> {
                            viewModel.addCharacteristic(change)
                            Log.w(TAG, "CHARACTERISTIC added")
                        }
                        DocumentChange.Type.MODIFIED -> {
                            viewModel.modifyCharacteristic(change)
                            Log.w(TAG, "CHARACTERISTIC modified")
                        }
                        DocumentChange.Type.REMOVED -> {
                            viewModel.removeCharacteristic(change)
                            Log.w(TAG, "CHARACTERISTIC removed")
                        }
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            PermissionManager.OPEN_SETTINGS_FOR_PERMISSION -> {
                if (PermissionManager.isLocationPermissionGranted(this)) {
                    EventBus.getDefault().post(LocationPermissionAvailableEvent())
                } else {
                    EventBus.getDefault().post(CloseAppEvent())
                }
            }
            BaseAccessManager.REQUEST_ENABLE_LOCATION -> {
                if (BaseAccessManager.isLocationEnabled(this)) {
                    mLocEnabled = true
                    EventBus.getDefault().post(LocationEnabledEvent())
                } else {
                    EventBus.getDefault().post(CloseAppEvent())
                }
            }
            BaseAccessManager.REQUEST_ENABLE_BT -> {
                if (resultCode == Activity.RESULT_OK) {
                    mBTEnabled = true
                    EventBus.getDefault().post(BluetoothEnabledEvent())
                } else {
                    EventBus.getDefault().post(CloseAppEvent())
                }
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onSupportNavigateUp() = findNavController(R.id.nav_host_fragment).navigateUp()

    /* Lifecycle END */

    /* Events START */

    @Subscribe
    fun onLocationPermissionAvailable(event: LocationPermissionAvailableEvent) {
        Log.d(TAG, "Location permission granted")

        if (BaseAccessManager.isLocationEnabled(this)) {
            mLocEnabled = true
            EventBus.getDefault().post(LocationEnabledEvent())
        } else {
            BaseAccessManager.requestLocation(this)
        }
    }

    @Subscribe
    fun onLocationEnabled(event: LocationEnabledEvent) {
        Log.d(TAG, "Location enabled")

        if (BaseAccessManager.isBluetoothEnabled(this)) {
            mBTEnabled = true
            EventBus.getDefault().post(BluetoothEnabledEvent())
        } else {
            BaseAccessManager.requestBluetooth(this)
        }
    }

    @Subscribe
    fun onBTEnabled(event: BluetoothEnabledEvent) {
        Log.d(TAG, "Bluetooth enabled")

        if (mLocEnabled && mBTEnabled) {
            EventBus.getDefault().post(AppStartEvent())
        }
    }

    @Subscribe
    fun onAppStart(event: AppStartEvent) {
        Log.d(TAG, "App started")

        registerReceiver(btReceiver, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))
        registerReceiver(locReceiver, IntentFilter(LocationManager.MODE_CHANGED_ACTION))

        BLEScanner.setUp(this)
        if (!BLEScanner.isReady) {
            finish()
        }
    }

    @Subscribe
    fun onCloseApp(event: CloseAppEvent) {
        Log.d(TAG, "Closing app!")

        finish()
    }

    @Subscribe
    fun onConnectToSensor(event: ConnectToSensor) {
//        bleConnection.connect(event.position)
    }

    /* Events END */
}