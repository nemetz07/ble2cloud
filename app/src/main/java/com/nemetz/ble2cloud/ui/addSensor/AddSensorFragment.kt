package com.nemetz.ble2cloud.ui.addSensor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nemetz.ble2cloud.BLE2CloudApplication
import com.nemetz.ble2cloud.MainActivity
import com.nemetz.ble2cloud.R
import com.nemetz.ble2cloud.connection.CloudConnector
import com.nemetz.ble2cloud.event.ServiceDiscoverEndedEvent
import com.nemetz.ble2cloud.ui.base.BaseFragment
import com.nemetz.ble2cloud.uiScope
import kotlinx.android.synthetic.main.add_sensor_fragment.*
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.Subscribe

class AddSensorFragment : BaseFragment() {

    companion object {
        fun newInstance() = AddSensorFragment()
    }

    private lateinit var viewModel: AddSensorViewModel
    private lateinit var viewAdapter: CharacteristicsAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var cloudConnector: CloudConnector

    val args: AddSensorFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.add_sensor_fragment, container, false)

        view.findViewById<TextView>(R.id.addSensorNameTV).text = args.device.name
        view.findViewById<TextView>(R.id.addSensorAddressTV).text = args.device.address

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(AddSensorViewModel::class.java)

        init()
    }

    private fun init() {
        viewModel.BLECharacteristics = (context as MainActivity).viewModel.BLECharacteristics

        viewManager = LinearLayoutManager(context)
        viewAdapter = CharacteristicsAdapter(viewModel.characteristics)

        addSensorRecyclerView.apply {
            layoutManager = viewManager
            adapter = viewAdapter
        }

        cloudConnector = (context?.applicationContext as BLE2CloudApplication).cloudConnector
        viewModel.bluetoothDevice = args.device

        viewModel.discoverCharacteristics(context)

        addSensorDoneButton.setOnClickListener {
            if (viewModel.BLESensor != null) {
                viewModel.saveSensor(cloudConnector)
                AddSensorFragmentDirections.actionAddSensorFragmentToActionSensors()
                    .also { findNavController().navigate(it) }
            }
        }
    }

    @Subscribe
    fun onSensorServicesDiscovered(event: ServiceDiscoverEndedEvent) {
        uiScope.launch {
            viewAdapter.notifyDataSetChanged()
            addSensorDoneButton.apply {
                setBackgroundColor(0xFF5E9E5F.toInt())
                text = "Done"
                isEnabled = true
            }
        }

        viewModel.bluetoothGatt?.apply {
            disconnect()
            close()
        }
    }
}
