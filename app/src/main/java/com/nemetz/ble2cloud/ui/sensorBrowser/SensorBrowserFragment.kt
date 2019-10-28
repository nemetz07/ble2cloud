package com.nemetz.ble2cloud.ui.sensorBrowser

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.nemetz.ble2cloud.R

class SensorBrowserFragment : Fragment() {

    companion object {
        fun newInstance() = SensorBrowserFragment()
    }

    private lateinit var viewModel: SensorBrowserViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.sensor_browser_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(SensorBrowserViewModel::class.java)
        // TODO: Use the ViewModel
    }

}
