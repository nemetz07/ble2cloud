package com.nemetz.ble2cloud.ui.deviceDetails.fragment

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.nemetz.ble2cloud.R
import com.nemetz.ble2cloud.event.ConnectionClosedEvent
import com.nemetz.ble2cloud.event.ConnectionEstablishedEvent
import com.nemetz.ble2cloud.event.DeviceReadingEvent
import com.nemetz.ble2cloud.ui.base.fragment.BaseFragment
import com.nemetz.ble2cloud.ui.deviceBrowser.model.DeviceCell
import com.nemetz.ble2cloud.ui.deviceDetails.viewmodel.DeviceDetailsViewModel
import com.nemetz.ble2cloud.uiScope
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class DeviceDetailsFragment : BaseFragment() {
    override val TAG = "DEVICE_DETAILS_FRAGMENT"

    companion object {
        fun newInstance() = DeviceDetailsFragment()
    }

    private val args: DeviceDetailsFragmentArgs by navArgs()
    private lateinit var deviceCell: DeviceCell

    private lateinit var viewModel: DeviceDetailsViewModel

    private lateinit var deviceNameTextView: TextView
    private lateinit var deviceConnectedTextView: TextView
    private lateinit var deviceValue: TextView
    private lateinit var deviceConnectButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.device_details_fragment, container, false)

        deviceNameTextView = view.findViewById(R.id.device_detail_name) as TextView
        deviceConnectedTextView = view.findViewById(R.id.device_detail_connected) as TextView
        deviceValue = view.findViewById(R.id.device_detail_value) as TextView
        deviceConnectButton = view.findViewById(R.id.device_detail_connect_button) as Button

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(DeviceDetailsViewModel::class.java)

        init()
    }

    fun init() {
        viewModel.init(context)

        deviceCell = args.device
        deviceNameTextView.text = deviceCell.device.name
        if(viewModel.mConnected){
            deviceConnectedTextView.text = "Connected"
            deviceConnectedTextView.setTextColor(Color.GREEN)
            deviceConnectButton.text = "Disconnect"
        }

        deviceConnectButton.setOnClickListener {
            viewModel.onConnectClicked(deviceCell.device)
        }
    }

    override fun onResume() {
        super.onResume()

        EventBus.getDefault().register(this)
    }

    override fun onPause() {
        super.onPause()
        if(findNavController().currentDestination?.label != "device_detail_fragment" && viewModel.mConnected){
//            viewModel.disconnect()
//            EventBus.getDefault().unregister(this)
        }
    }

    private fun magicallyExtractRightValue(o: Byte): Int = when {
        (o.toInt() < 0) -> 255 + o.toInt() + 1
        else -> o.toInt()
    }

    /* Events START */

    @Subscribe
    fun onConnectionEstablished(event: ConnectionEstablishedEvent) {
        Log.d(TAG, "onConnectionEstablished")
        uiScope.launch {
            deviceConnectedTextView.text = "Connected"
            deviceConnectedTextView.setTextColor(Color.GREEN)
            deviceConnectButton.text = "Disconnect"
        }
    }

    @Subscribe
    fun onConnectionClosed(event: ConnectionClosedEvent) {
        Log.d(TAG, "onConnectionClosed")
        uiScope.launch {
            deviceConnectedTextView.text = "Not connected"
            deviceConnectedTextView.setTextColor(Color.parseColor("#C62828"))
            deviceConnectButton.text = "Connect"
            deviceValue.text = "-"
        }
    }

    @Subscribe
    fun onDeviceReading(event: DeviceReadingEvent){
        uiScope.launch {
            deviceValue.text =  magicallyExtractRightValue(event.value).toString()
        }
    }

    /* Events END */
}
