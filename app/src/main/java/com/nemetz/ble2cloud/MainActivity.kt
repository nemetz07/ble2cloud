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
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.nemetz.ble2cloud.event.*
import com.nemetz.ble2cloud.connection.BLEConnection
import com.nemetz.ble2cloud.data.SensorRepository
import com.nemetz.ble2cloud.utils.manager.BaseAccessManager
import com.nemetz.ble2cloud.utils.manager.PermissionManager
import com.nemetz.ble2cloud.utils.manager.SharedPreferencesManager
import com.nemetz.ble2cloud.utils.receiver.BluetoothStateChangedReceiver
import com.nemetz.ble2cloud.utils.receiver.LocationStateChangedReceiver
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe


class MainActivity : AppCompatActivity() {
    private val TAG = "MAIN_ACTIVITY"

    private var btReceiver = BluetoothStateChangedReceiver()
    private var locReceiver = LocationStateChangedReceiver()

    private var mBTEnabled = false
    private var mLocEnabled = false

    private lateinit var bleConnection: BLEConnection

    /* Lifecycle START */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        SensorRepository.setUp()

        bleConnection = BLEConnection(this)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navbar)

        NavigationUI.setupWithNavController(
            findViewById<BottomNavigationView>(R.id.bottom_navbar),
            findNavController(R.id.nav_host_fragment)
        )
    }

    override fun onStart() {
        super.onStart()

        EventBus.getDefault().register(this)

        if (PermissionManager.checkLocationPermission(this))
            EventBus.getDefault().post(LocationPermissionAvailableEvent())
    }

    override fun onStop() {
        super.onStop()
        if (mBTEnabled && mLocEnabled) {
            unregisterReceiver(btReceiver)
            unregisterReceiver(locReceiver)
        }

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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_auto_connect -> {
                EventBus.getDefault().post(AutoConnectEvent())
                true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

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
    }

    @Subscribe
    fun onCloseApp(event: CloseAppEvent) {
        Log.d(TAG, "Closing app!")

        finish()
    }

    @Subscribe
    fun onConnectToSensor(event: ConnectToSensor){
//        bleConnection.connect(event.position)
    }

    /* Events END */
}