package com.nemetz.ble2cloud.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.nemetz.ble2cloud.*
import com.nemetz.ble2cloud.service.DataCollectionService
import kotlinx.android.synthetic.main.home_fragment.*

class HomeFragment : Fragment() {

    private val TAG = "HOME_FRAGMENT"

    companion object {
        fun newInstance() = HomeFragment()
    }

    private lateinit var viewModel: HomeViewModel
//    private lateinit var cloudConnector: CloudConnector

    var stopServiceBTN: Button? = null
    var startServiceBTN: Button? = null

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


    private fun init() {

        val application = context!!.applicationContext as BLE2CloudApplication

        val serviceObserver = Observer<Boolean> {isRunning ->
            if(isRunning) {
                startServiceButton.apply {
                    text = "Resume"
                    setOnClickListener {
                        findNavController().navigate(R.id.action_global_dataCollectionFragment)
                    }
                }
                stopServiceButton.apply {
                    isEnabled = true
                    setOnClickListener {
                        Intent(context, DataCollectionService::class.java).apply { action = "STOP" }
                            .also { context!!.startService(it) }
                    }
                }
            } else {
                startServiceButton.apply {
                    text = "Start"
                    setOnClickListener {
                        HomeFragmentDirections.actionActionHomeToDataCollectionOptionsFragment().also { findNavController().navigate(it) }
                    }
                }
                stopServiceButton.apply {
                    isEnabled = false
                    setBackgroundColor(0xFFC5C5C5.toInt())
                }
            }
        }

        application.isServiceRunning.observe(this, serviceObserver)

//        viewModel.initCharacteristics()

//        cloudConnector = (context?.applicationContext as BLE2CloudApplication).cloudConnector

//        startServiceButton.setOnClickListener {
//
////            Intent(
////                context,
////                DataCollectionService::class.java
////            ).also { ContextCompat.startForegroundService(context!!, it) }
//        }

//        viewModel.characteristics = (context as MainActivity).viewModel.characteristics

//        uploadButton.setOnClickListener {
//            viewModel.characteristics.forEach {
//                cloudConnector.saveCharacteristic(it)
//            }
//        }
    }

    override fun onResume() {
        super.onResume()

        startServiceBTN = startServiceButton
        stopServiceBTN = stopServiceButton

//        startServiceButton.apply {
//            if(isServiceRunning) {
//                text = "Resume"
//                setOnClickListener {
//                    findNavController().navigate(R.id.action_global_dataCollectionFragment)
//                }
//            } else {
//                text = "Start"
//                setOnClickListener {
//                    HomeFragmentDirections.actionActionHomeToDataCollectionOptionsFragment().also { findNavController().navigate(it) }
//                }
//            }
//        }
//
//        if(isServiceRunning) {
//            stopServiceButton.isEnabled = true
//            stopServiceButton.setOnClickListener {
//                Intent(context, DataCollectionService::class.java).apply { action = "STOP" }
//                    .also { context!!.startService(it) }
//                stopServiceButton.isEnabled = false
//                stopServiceButton.setBackgroundColor(0xFFC5C5C5.toInt())
//                startServiceButton.text = "Start"
//                startServiceButton.setOnClickListener {
//                    HomeFragmentDirections.actionActionHomeToDataCollectionOptionsFragment().also { findNavController().navigate(it) }
//                }
//            }
//        } else {
//            stopServiceButton.isEnabled = false
//            stopServiceButton.setBackgroundColor(0xFFC5C5C5.toInt())
//        }
    }
}
