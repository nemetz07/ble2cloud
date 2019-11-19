package com.nemetz.ble2cloud.ui.scanner

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nemetz.ble2cloud.R
import com.nemetz.ble2cloud.event.*
import com.nemetz.ble2cloud.ioScope
import com.nemetz.ble2cloud.ui.base.BaseFragment
import com.nemetz.ble2cloud.uiScope
import kotlinx.android.synthetic.main.scanner_fragment.*
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.Subscribe

class ScannerFragment : BaseFragment() {
    override val TAG = "SCANNER_FRAGMENT"

    private lateinit var viewModel: ScannerViewModel
    private lateinit var activity: Activity

    companion object {
        fun newInstance() = ScannerFragment()
    }

    private lateinit var viewAdapter: ScannerAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager

    private val mClickListener = object: ScannerAdapter.ItemClickListener {
        override fun onItemClick(view: View, position: Int) {
//            cloudConnector.saveSensor(viewModel.complexSensors[position].BLESensor)
            val device = viewModel.complexSensors[position].bluetoothDevice
            val action = ScannerFragmentDirections.actionActionScanToAddSensorFragment(device)
            findNavController().navigate(action)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.scanner_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity = context as Activity
        viewModel = ViewModelProviders.of(this).get(ScannerViewModel::class.java)
        init()

        scannerRefresh.setOnRefreshListener {
            refresh()
        }
    }

    fun init() {
        viewManager = LinearLayoutManager(context)
        viewAdapter = ScannerAdapter(viewModel.cellSensors)
        viewAdapter.setClickListener(mClickListener)

        scannerRecyclerView.apply {
            layoutManager = viewManager
            adapter = viewAdapter
        }

        scannerBackButton.setOnClickListener {
            findNavController().navigateUp()
        }

        if(!viewModel.isAlreadyScanned){
            uiScope.launch {
                scannerRefresh.isRefreshing = true
            }
            refresh()
            viewModel.isAlreadyScanned = true
        }
    }

    fun refresh(){
        ioScope.launch {
            viewAdapter.disableClickListener()
            if (viewModel.scanSensors()) {
                uiScope.launch {
                    viewAdapter.notifyDataSetChanged()
                    viewAdapter.enableClickListener()
                    scannerRefresh?.isRefreshing = false
                }
            }
        }
    }

    @Subscribe
    fun onSensorAdded(event: SensorAddedEvent){
        uiScope.launch {
            Toast.makeText(context, "Sensor added", Toast.LENGTH_LONG).show()
        }
    }

    @Subscribe
    fun onSensorAlreadyExists(event: SensorAlreadyExistEvent){
        uiScope.launch {
            Toast.makeText(context, "Sensor already added", Toast.LENGTH_LONG).show()
        }
    }

    @Subscribe
    fun onAutoConnect(event: AutoConnectEvent) {
//        viewModel.autoConnect()
    }

}
