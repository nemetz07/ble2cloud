package com.nemetz.ble2cloud.ui.sensorBrowser

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.utils.Utils
import com.google.firebase.firestore.ListenerRegistration
import com.nemetz.ble2cloud.BLE2CloudApplication
import com.nemetz.ble2cloud.MainActivity
import com.nemetz.ble2cloud.R
import com.nemetz.ble2cloud.data.CloudConnector
import com.nemetz.ble2cloud.data.FireabaseRepo
import com.nemetz.ble2cloud.event.FetchCompletedEvent
import com.nemetz.ble2cloud.ui.base.BaseFragment
import com.nemetz.ble2cloud.uiScope
import com.nemetz.ble2cloud.utils.Collections
import kotlinx.android.synthetic.main.sensor_browser_fragment.*
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.Subscribe

class SensorBrowserFragment : BaseFragment() {

    override val TAG = "SENSOR_BROWSER_FRAGMENT"

    companion object {
        fun newInstance() = SensorBrowserFragment()
    }

    private lateinit var viewModel: SensorBrowserViewModel
    private lateinit var viewAdapter: SensorBrowserAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager

    private lateinit var cloudConnector: CloudConnector
    private lateinit var firebaseSensorRepo: FireabaseRepo
    private var mRegistration: ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.sensor_browser_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(SensorBrowserViewModel::class.java)

        init()
    }

    override fun onStart() {
        super.onStart()

        mRegistration = firebaseSensorRepo.addListener(Collections.SENSORS, viewAdapter)
    }

    override fun onStop() {
        super.onStop()

        firebaseSensorRepo.removeListener(Collections.SENSORS, mRegistration)
    }

    private fun init() {
        cloudConnector = (context?.applicationContext as BLE2CloudApplication).cloudConnector
        firebaseSensorRepo = (context?.applicationContext as BLE2CloudApplication).firebaseRepo

        viewManager = LinearLayoutManager(context)
        viewAdapter = SensorBrowserAdapter((context as MainActivity).viewModel.mySensors)

        Utils.init(context)

        viewAdapter.setClickListener(object : SensorBrowserAdapter.ItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                val mySensor = (context as MainActivity).viewModel.mySensors[position]
                val action = SensorBrowserFragmentDirections.actionActionSensorsToSensorDetailFragment(mySensor)
                findNavController().navigate(action)
            }
        })

        sensorBrowserRecyclerView.apply {
            layoutManager = viewManager
            adapter = viewAdapter
        }

        val itemTouchHelper = ItemTouchHelper(SwipeToDeleteCallback(viewAdapter))
        itemTouchHelper.attachToRecyclerView(sensorBrowserRecyclerView)

        addSensorButton.setOnClickListener {
            findNavController().navigate(SensorBrowserFragmentDirections.actionActionSensorsToActionScan())
        }
    }

    inner class SwipeToDeleteCallback(adapter: SensorBrowserAdapter) :
        ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.adapterPosition
            cloudConnector.deleteSensor((context as MainActivity).viewModel.mySensors[position].address)
        }

    }

    @Subscribe
    fun onFetchCompletedEvent(event: FetchCompletedEvent) {

    }
}
