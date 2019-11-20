package com.nemetz.ble2cloud.service

import android.app.*
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.*
import com.nemetz.ble2cloud.BLE2CloudApplication
import com.nemetz.ble2cloud.MainActivity
import com.nemetz.ble2cloud.R
import com.nemetz.ble2cloud.connection.BLEScanSettings
import com.nemetz.ble2cloud.connection.BLEScanner
import com.nemetz.ble2cloud.connection.CloudConnector
import com.nemetz.ble2cloud.data.*
import com.nemetz.ble2cloud.ioScope
import com.nemetz.ble2cloud.utils.FirebaseCollections
import com.nemetz.ble2cloud.utils.getBLESensor
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.joda.time.DateTime


class DataCollectionService : Service(), EventListener<QuerySnapshot> {

    private val TAG = "DATA_COLLECTION_SERVICE"
    private val CHANNEL_ID = "ForegroundServiceChannel"
    private val ACTION_STOP_SERVICE = "STOP"

    private var sensors: ArrayList<BLESensor> = arrayListOf()
    private var foundSensors: ArrayList<ComplexSensor> = arrayListOf()
    private val scanFilters = arrayListOf<ScanFilter>()

    private var firestore: FirebaseFirestore? = null
    private var sensorRegistration: ListenerRegistration? = null
    private var cloudConnector: CloudConnector? = null

    private var scanEffort: String = "LOW_POWER"
    private var scanRate: Long = 10
    private var dataRate: Int = 5
    private var locationRecord: Boolean = false

    private var scanSettings: ScanSettings = BLEScanSettings.SCAN_SETTINGS_LOW_ENERGY

    private lateinit var notification: Notification

    private val dataTimes: MutableMap<String, DateTime> = mutableMapOf()

    private var loop: Job? = null

    private var fusedLocationClient: FusedLocationProviderClient? = null

    private var bleScanner: BLEScanner? = null

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            if (result?.device != null) {
                Log.d(TAG, "Sensor found!: ${result.device.address}")
                if (!foundSensors.any { it.BLESensor.address == result.device.address }) {
                    val sensor = sensors.find { it.address == result.device.address } ?: return

                    Log.d(TAG, "Sensor added to foundsensors!: ${result.device.address}")
                    foundSensors.add(
                        ComplexSensor(
                            BLESensor = sensor,
                            bluetoothDevice = result.device,
                            rssi = result.rssi
                        )
                    )
                }
            }
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Log.d(TAG, "CONNECTED to ${gatt?.device?.address}")
                    foundSensors.find { it.bluetoothDevice.address == gatt?.device?.address }
                        ?.bluetoothGatt = gatt
                    gatt?.discoverServices()
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.d(TAG, "DISCONNECTED from ${gatt?.device?.address}")
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    val sensor =
                        foundSensors.find { it.bluetoothDevice.address == gatt.device?.address }
                            ?: return
                    sensor.services = gatt.services

                    gatt.services.forEach { service ->
                        service.characteristics.forEach { characteristic ->
                            if (sensor.BLESensor.values.any { it.value.uuid == characteristic.uuid.toString() }) {
                                characteristic.descriptors.forEach { descriptor ->
                                    if (descriptor.uuid == BLEUUID.CONFIG) {
                                        gatt.readDescriptor(descriptor)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        override fun onDescriptorRead(
            gatt: BluetoothGatt?,
            descriptor: BluetoothGattDescriptor?,
            status: Int
        ) {
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    if (gatt?.device?.address != null) {
                        foundSensors.find { it.BLESensor.address == gatt.device.address }
                            ?.enableNotification(descriptor)
                    }
                }
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?
        ) {
            gatt?.device?.address?.let { dataRead(it, characteristic) }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if ((ACTION_STOP_SERVICE == intent?.action) or ((applicationContext as BLE2CloudApplication).isServiceRunning.value == true)) {
            Log.d(TAG, "called to cancel service")
            die()
        } else {
            dataRate = intent?.getIntExtra("DATA_RATE", 5) ?: 5
            scanRate = (intent?.getIntExtra("SCAN_RATE", 10) ?: 10).toLong()
            scanEffort = intent?.getStringExtra("SCAN_EFFORT") ?: "LOW_POWER"
            locationRecord = intent?.getBooleanExtra("LOCATION_RECORD", false) ?: false

            notification = createNotification()

            startForeground(1, notification)
            startDataCollection()

//            (applicationContext as BLE2CloudApplication).startTime = Timestamp(DateTime.now().toDate())
            (applicationContext as BLE2CloudApplication).isServiceRunning.value = true
        }

        return START_NOT_STICKY
    }

    private fun die() {
        bleScanner?.stopScan(scanCallback)
        stopForeground(true)
        stopSelf()

        (applicationContext as BLE2CloudApplication).startTime = null
    }

    override fun onCreate() {
        super.onCreate()

        firestore = FirebaseFirestore.getInstance()
        sensorRegistration =
            firestore!!.collection(FirebaseCollections.SENSORS).addSnapshotListener(this)
        Log.d(TAG, "Listener added")
        cloudConnector = CloudConnector(firestore!!)
        bleScanner = (applicationContext as BLE2CloudApplication).bleScanner
        if (!bleScanner!!.isReady) {
            Log.d(TAG, "Can't start service because scanner is not initialized!")
            die()
        }
    }

    private fun startDataCollection() {
        Log.d(
            TAG,
            "START with settings: EFFORT: $scanEffort, SCAN_RATE: $scanRate, DATA_RATE: $dataRate, LOCATION: $locationRecord"
        )
        if (locationRecord) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        }

        scanSettings = when (scanEffort) {
            "LOW_POWER" -> BLEScanSettings.SCAN_SETTINGS_LOW_ENERGY
            "AGGRESSIVE" -> BLEScanSettings.SCAN_SETTINGS_AGGRESSIVE
            else -> BLEScanSettings.SCAN_SETTINGS_LOW_ENERGY
        }
        loop = ioScope.launch {
            delay(1000)
            while (true) {
                if (scannerTask()) {
                    connectToSensors()
                }

                delay(10000)
            }
        }
    }

    private fun connectToSensors() {
        for (sensor in foundSensors) {
            if (!dataTimes.containsKey(sensor.BLESensor.address) or (dataTimes[sensor.BLESensor.address]?.isBefore(
                    DateTime.now().minusMinutes(dataRate)
                ) == true)
            ) {
                dataTimes[sensor.BLESensor.address] = DateTime.now()
                sensor.connect(baseContext, gattCallback)
            } else {
                Log.d(TAG, "DATA already collected recently: $dataTimes")
            }
        }
    }

    fun dataRead(
        address: String,
        characteristic: BluetoothGattCharacteristic?
    ) {
        val sensor = foundSensors.find { it.bluetoothDevice.address == address } ?: return

        if (characteristic == null) {
            sensor.disableNotification(characteristic)
            sensor.disconnect()
            return
        }

        sensor.BLESensor.values.forEach { (_, sensorValue) ->
            val sensorData: BLESensorData = when (sensorValue.format?.format) {
                "STRING" -> {
                    processStringData(sensorValue.format!!, characteristic)
                }
                "UINT8", "UINT16", "UINT32", "SINT8", "SINT16", "SINT32" -> {
                    processIntegerData(sensorValue.format!!, characteristic)
                }
                "FLOAT", "SFLOAT" -> {
                    processFloatData(sensorValue.format!!, characteristic)
                }
                else -> {
                    Log.d(TAG, "Invalid format: ${sensorValue.format?.format}")
                    return
                }
            }

            if (locationRecord) {
                fusedLocationClient?.lastLocation?.addOnSuccessListener {
                    sensorData.latitude = it.latitude
                    sensorData.longitude = it.longitude
                    sensorData.sensorName = sensor.BLESensor.name
                    sensorData.address = sensor.BLESensor.address
                    if (sensorValue.format != null) {
                        sensorData.name = sensorValue.format!!.name
                        sensorData.unit = sensorValue.format!!.unit
                    }

                    saveData(sensor.BLESensor.address, sensorValue.format!!, sensorData)
                }
            } else {
                sensorData.sensorName = sensor.BLESensor.name
                sensorData.address = sensor.BLESensor.address
                if (sensorValue.format != null) {
                    sensorData.name = sensorValue.format!!.name
                    sensorData.unit = sensorValue.format!!.unit
                }

                saveData(sensor.BLESensor.address, sensorValue.format!!, sensorData)
            }

        }

        sensor.disableNotification(characteristic)
        sensor.disconnect()
    }

    private fun processFloatData(
        BLEDataFormat: BLEDataFormat,
        characteristic: BluetoothGattCharacteristic
    ): BLESensorData {
        val data = characteristic.getFloatValue(BLEDataFormat.dataFormat(), BLEDataFormat.offset)

        return BLESensorData(value = data.toString())
    }

    private fun processIntegerData(
        BLEDataFormat: BLEDataFormat,
        characteristic: BluetoothGattCharacteristic
    ): BLESensorData {
        val data = characteristic.getIntValue(BLEDataFormat.dataFormat(), BLEDataFormat.offset)

        return BLESensorData(value = data.toString())
    }

    private fun processStringData(
        BLEDataFormat: BLEDataFormat,
        characteristic: BluetoothGattCharacteristic
    ): BLESensorData {
        val dataString = characteristic.getStringValue(BLEDataFormat.offset)
        val data = if (BLEDataFormat.substring_end != null || BLEDataFormat.substring_end != 0) {
            dataString.substring(BLEDataFormat.substring_start!!, BLEDataFormat.substring_end!!)
        } else {
            dataString
        }

        return BLESensorData(value = data)
    }

    private fun saveData(address: String, dataFormat: BLEDataFormat, sensorData: BLESensorData) {
        cloudConnector?.saveData(address, dataFormat, sensorData)
    }

    private suspend fun scannerTask(): Boolean {
        sensors.forEach { sensor ->
            if (!scanFilters.any { it.deviceAddress == sensor.address }) {
                scanFilters.add(ScanFilter.Builder().setDeviceAddress(sensor.address).build())
            }
        }

        Log.d(TAG, "Filters: $scanFilters")

        return bleScanner?.scanLeDevice(
            scanFilters,
            scanSettings,
            scanCallback,
            scanRate * 1000
        ) ?: false
    }

    override fun onDestroy() {
        loop?.cancel()
        sensorRegistration?.remove()
        Log.d(TAG, "Listener removed")
        sensorRegistration = null
        (applicationContext as BLE2CloudApplication).isServiceRunning.value = false

        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onEvent(querySnapshot: QuerySnapshot?, exception: FirebaseFirestoreException?) {
        // Handle errors
        if (exception != null) {
            Log.w(TAG, "onEvent:error", exception)
            return
        }

        for (change in querySnapshot!!.documentChanges) {
            when (change.type) {
                DocumentChange.Type.ADDED -> {
                    addSensor(change)
                    Log.w(TAG, "SENSOR added")
                }
                DocumentChange.Type.MODIFIED -> {
                    modifySensor(change)
                    Log.w(TAG, "SENSOR modified")
                }
                DocumentChange.Type.REMOVED -> {
                    removeSensor(change)
                    Log.w(TAG, "SENSOR removed")
                }
            }
        }
    }

    private fun removeSensor(change: DocumentChange?) {
        if (change == null)
            return

        sensors.removeAt(change.oldIndex)
    }

    private fun modifySensor(change: DocumentChange?) {
        if (change == null)
            return

        if (change.oldIndex == change.newIndex) {
            // Item changed but remained in same position
            sensors[change.oldIndex] = change.getBLESensor()
        } else {
            // Item changed and changed position
            sensors.removeAt(change.oldIndex)
            change.getBLESensor().let { sensors.add(change.newIndex, it) }
        }
    }

    private fun addSensor(change: DocumentChange?) {
        change?.getBLESensor()?.let { sensors.add(change.newIndex, it) }
    }

    private fun createNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)

        val stopIntent = Intent(this, DataCollectionService::class.java).apply {
            action = ACTION_STOP_SERVICE
        }
        val stopPendingIntent =
            PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_CANCEL_CURRENT)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_cpu)
            .setContentTitle("Bluetooth Data Collection")
            .setContentText("Data collection in progress...")
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.ic_dialog_close_light, "Stop", stopPendingIntent)
            .build()
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "BLE2Cloud data collection"
            val descriptionText = "Bluetooth data collection service"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

}
