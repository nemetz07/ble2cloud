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
import com.nemetz.ble2cloud.data.CloudConnector
import com.nemetz.ble2cloud.data.MyDataFormat
import com.nemetz.ble2cloud.data.MySensor
import com.nemetz.ble2cloud.data.SensorValue
import com.nemetz.ble2cloud.event.SensorServicesDiscoveredEvent
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
            if (viewModel.mySensor != null) {
                viewModel.saveSensor(cloudConnector)
                findNavController().navigateUp()
            }
        }
    }

    @Subscribe
    fun onSensorServicesDiscovered(event: SensorServicesDiscoveredEvent) {
        if (viewModel.services == null)
            return

        viewModel.mySensor = MySensor(
            address = viewModel.bluetoothDevice.address,
            name = viewModel.bluetoothDevice.name
        )

        viewModel.services!!.forEach { service ->
            service.characteristics.forEach { characteristic ->
                val myCharacteristic =
                    (context as MainActivity).viewModel.getMyCharacteristic(characteristic.uuid.toString())
                myCharacteristic?.data?.forEach { dataFormat ->
                    val sensorValue = SensorValue(
                        format = MyDataFormat(
                            name = dataFormat.name,
                            unit = dataFormat.unit,
                            format = dataFormat.format,
                            offset = dataFormat.offset,
                            substring_start = dataFormat.substring_start,
                            substring_end = dataFormat.substring_end
                        ),
                        uuid = myCharacteristic.uuid ?: ""
                    )

                    viewModel.characteristics.add(
                        CharacteristicCell(
                            name = dataFormat.name,
                            unit = dataFormat.unit,
                            uuid = myCharacteristic.uuid ?: "",
                            enabled = true
                        )
                    )
                    uiScope.launch {
                        viewAdapter.notifyItemInserted(viewModel.characteristics.size - 1)
                    }

                    viewModel.mySensor!!.values.add(sensorValue)
                }
            }
        }

        uiScope.launch {
            addSensorDoneButton.apply {
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
