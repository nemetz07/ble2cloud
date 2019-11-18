package com.nemetz.ble2cloud.ui.dataCollectionOptions

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.SeekBar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.nemetz.ble2cloud.R
import com.nemetz.ble2cloud.service.DataCollectionService
import com.nemetz.ble2cloud.uiScope
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.data_collection_options_fragment.*
import kotlinx.coroutines.launch

class DataCollectionOptionsFragment : Fragment() {

    companion object {
        fun newInstance() = DataCollectionOptionsFragment()
    }

    private lateinit var viewModel: DataCollectionOptionsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.data_collection_options_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(DataCollectionOptionsViewModel::class.java)

        init()
    }

    private fun init() {
        dataCollectionOptionsCancelButton.setOnClickListener {
            findNavController().navigateUp()
        }

        dataCollectionOptionsStartButton.setOnClickListener {
            Intent(context, DataCollectionService::class.java)
            .apply {
                putExtra("SCAN_RATE", viewModel.scanRate)
                putExtra("SCAN_EFFORT", viewModel.scanEffort)
                putExtra("DATA_RATE", viewModel.dataRate)
                putExtra("LOCATION_RECORD", viewModel.locationRecord)
            }
            .also { ContextCompat.startForegroundService(context!!, it) }

            DataCollectionOptionsFragmentDirections.actionDataCollectionOptionsFragmentToDataCollectionFragment()
                .also { findNavController().navigate(it) }
        }

        dataCollectionOptionsEffortSpinner.onItemSelectedListener = object: AdapterView.OnItemClickListener,
            AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {  }
            override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) { }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when(position) {
                    0 -> viewModel.scanEffort = "LOW_POWER"
                    1 -> viewModel.scanEffort = "BALANCED"
                    2 -> viewModel.scanEffort = "AGGRESSIVE"
                    else -> viewModel.scanEffort = "LOW_POWER"
                }
            }
        }

        dataCollectionOptionsScanRateSeekbar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                viewModel.scanRate = (progress + 1) * 5

                uiScope.launch {
                    dataCollectionOptionsScanRateValueTV.text = "${(progress+1) * 5} sec"
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {  }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {  }
        })

        dataCollectionOptionsDataRateSeekbar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                viewModel.dataRate = progress + 1

                uiScope.launch {
                    dataCollectionOptionsDataRateValueTV.text = "${progress+1} min"
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {  }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {  }
        })

        dataCollectionOptionsSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            viewModel.locationRecord = isChecked
            uiScope.launch {
                dataCollectionOptionsSwitch.text = if(isChecked) "ON" else "OFF"
            }
        }
    }
}
