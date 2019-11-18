package com.nemetz.ble2cloud.ui.sensorDetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.gms.maps.MapView
import com.google.firebase.firestore.FirebaseFirestore
import com.nemetz.ble2cloud.R
import com.nemetz.ble2cloud.connection.CloudConnector
import com.nemetz.ble2cloud.event.ChartAddedEvent
import com.nemetz.ble2cloud.event.SensorDetailMapUpdatedEvent
import com.nemetz.ble2cloud.ui.base.BaseFragment
import com.nemetz.ble2cloud.uiScope
import kotlinx.android.synthetic.main.sensor_detail_fragment.*
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.Subscribe


class SensorDetailFragment : BaseFragment() {

    override val TAG = "SENSOR_DETAIL_FRAGMENT"

    companion object {
        fun newInstance() = SensorDetailFragment()
    }

    val args: SensorDetailFragmentArgs by navArgs()

    private lateinit var viewModel: SensorDetailViewModel
    private lateinit var viewAdapter: ChartDataAdapter

    private lateinit var mapView: MapView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.sensor_detail_fragment, container, false)
        mapView = view.findViewById(R.id.sensorDetailMapView)
        mapView.onCreate(savedInstanceState)

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(SensorDetailViewModel::class.java)

        init()
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onDestroy() {
        mapView.onDestroy()
        super.onDestroy()
    }

    override fun onPause() {
        mapView.onPause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onStop() {
        mapView.onStop()
        super.onStop()
    }

    private fun init() {
        viewModel.sensor = args.sensor

        viewAdapter = ChartDataAdapter(context!!, viewModel.chartItems)
        chartListView.adapter = viewAdapter

        sensorDetailNameTV.text = args.sensor.name

        sensorDetailBackButton.setOnClickListener {
            findNavController().navigateUp()
        }

        viewModel.cloudConnector =
            CloudConnector(FirebaseFirestore.getInstance())
        if(!viewModel.isAlreadyInitialized) {
            fragmentManager?.let { viewModel.fetchCharts(it) }
            viewModel.isAlreadyInitialized = true
            mapView.getMapAsync(viewModel.onMapReadyCallback)
        }
    }

    @Subscribe
    fun onChartAdded(event: ChartAddedEvent) {
        uiScope.launch {
            viewAdapter.notifyDataSetChanged()
        }
    }

    @Subscribe
    fun onMapsUpdated(event: SensorDetailMapUpdatedEvent){
        uiScope.launch {
            sensorDetailMapOverlayImageView.visibility = View.INVISIBLE
            sensorDetailMapOverlayTV.visibility = View.INVISIBLE
        }
    }
}
