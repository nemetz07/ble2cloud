package com.nemetz.ble2cloud.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import com.nemetz.ble2cloud.BLE2CloudApplication
import com.nemetz.ble2cloud.MainActivity
import com.nemetz.ble2cloud.R
import com.nemetz.ble2cloud.data.CloudConnector
import com.nemetz.ble2cloud.data.FireabaseRepo
import com.nemetz.ble2cloud.event.*
import com.nemetz.ble2cloud.service.DataGatheringService
import com.nemetz.ble2cloud.ui.base.BaseFragment
import kotlinx.android.synthetic.main.home_fragment.*
import org.greenrobot.eventbus.Subscribe

class HomeFragment : BaseFragment() {

    override val TAG = "HOME_FRAGMENT"

    companion object {
        fun newInstance() = HomeFragment()
    }

    private lateinit var viewModel: HomeViewModel
    private lateinit var firebaseRepo: FireabaseRepo
    //    private var mSensorRegistration: ListenerRegistration? = null
    private lateinit var cloudConnector: CloudConnector

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.home_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(HomeViewModel::class.java)

        init()
    }

//    override fun onStart() {
//        super.onStart()
//
////        mSensorRegistration = firebaseRepo.addListener(Collections.SENSORS, this)
//    }
//
//    override fun onStop() {
//        super.onStop()
//
////        firebaseRepo.removeListener(Collections.SENSORS, mSensorRegistration)
//    }

    private fun init() {
//        viewModel.initCharacteristics()

        firebaseRepo =
            (context?.applicationContext as BLE2CloudApplication).firebaseRepo

        cloudConnector = (context?.applicationContext as BLE2CloudApplication).cloudConnector

        connectToSensorsButton.setOnClickListener {
//            viewModel.startDataGathering()
            val serviceIntent = Intent(context, DataGatheringService::class.java)
            ContextCompat.startForegroundService(context!!, serviceIntent)
        }

        disconnectButton.setOnClickListener {
//            viewModel.disconnectSensors()
            val serviceIntent = Intent(context, DataGatheringService::class.java)
            context!!.stopService(serviceIntent)
        }

        viewModel.cloudConnector = cloudConnector
        viewModel.sensors = (context as MainActivity).viewModel.mySensors
        viewModel.myCharacteristics = (context as MainActivity).viewModel.myCharacteristics

//        uploadButton.setOnClickListener {
//            viewModel.myCharacteristics.forEach {
//                cloudConnector.saveCharacteristic(it)
//            }
//        }
    }

//    override fun onEvent(querySnapshot: QuerySnapshot?, e: FirebaseFirestoreException?) {
//        // Handle errors
//        if (e != null) {
//            Log.w(TAG, "onEvent:error", e)
//            return
//        }
//
//        for (change in querySnapshot!!.documentChanges) {
//            when (change.getPath()) {
//                Collections.SENSORS -> {
//                    when (change.type) {
//                        DocumentChange.Type.ADDED -> {
//                            onSensorAdded(change)
//                            Log.w(TAG, "SENSOR added")
//                        }
//                        DocumentChange.Type.MODIFIED -> {
//                            onSensorModified(change)
//                            Log.w(TAG, "SENSOR modified")
//                        }
//                        DocumentChange.Type.REMOVED -> {
//                            onSensorRemoved(change)
//                            Log.w(TAG, "SENSOR removed")
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//    protected fun onSensorAdded(change: DocumentChange) {
//        viewModel.sensors.add(change.newIndex, change.getMySensor())
//    }
//
//    protected fun onSensorModified(change: DocumentChange) {
//        if (change.oldIndex == change.newIndex) {
//            // Item changed but remained in same position
//            viewModel.sensors[change.oldIndex] = change.getMySensor()
//        } else {
//            // Item changed and changed position
//            viewModel.sensors.removeAt(change.oldIndex)
//            viewModel.sensors.add(change.newIndex, change.getMySensor())
//        }
//    }
//
//    protected fun onSensorRemoved(change: DocumentChange) {
//        viewModel.sensors.removeAt(change.oldIndex)
//    }

    @Subscribe
    fun onScanComplete(event: ScanCompleteEvent) {
        if (context != null) {
            viewModel.connectToSensors(context!!)
        }
    }
}
