package com.nemetz.ble2cloud.ui.sensorBrowser

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.nemetz.ble2cloud.BLE2CloudApplication
import com.nemetz.ble2cloud.R
import com.nemetz.ble2cloud.connection.CloudConnector
import com.nemetz.ble2cloud.event.FetchCompletedEvent
import com.nemetz.ble2cloud.ui.base.BaseFragment
import com.nemetz.ble2cloud.ui.dialog.DeleteConfirmDialogFragment
import com.nemetz.ble2cloud.utils.FirebaseCollections
import kotlinx.android.synthetic.main.sensor_browser_fragment.*
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

    override fun onDestroy() {
        mRegistration?.remove()
        super.onDestroy()
    }

    private fun init() {
        cloudConnector = (context?.applicationContext as BLE2CloudApplication).cloudConnector

        viewManager = LinearLayoutManager(context)
        viewAdapter = SensorBrowserAdapter()

        viewAdapter.setClickListener(object : SensorBrowserAdapter.ItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                val mySensor = viewAdapter.cellSensors[position]
                val action =
                    SensorBrowserFragmentDirections.actionActionSensorsToSensorDetailFragment(
                        mySensor
                    )
                findNavController().navigate(action)
            }
        })

        sensorBrowserRecyclerView.apply {
            layoutManager = viewManager
            adapter = viewAdapter
        }

        ItemTouchHelper(SwipeToDeleteCallback()).attachToRecyclerView(sensorBrowserRecyclerView)

        addSensorButton.setOnClickListener {
            findNavController().navigate(SensorBrowserFragmentDirections.actionActionSensorsToActionScan())
        }
        mRegistration = FirebaseFirestore.getInstance().collection(FirebaseCollections.SENSORS)
            .addSnapshotListener(viewAdapter)
    }

    inner class SwipeToDeleteCallback :
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
            fragmentManager?.let {
                DeleteConfirmDialogFragment(DialogInterface.OnClickListener { _: DialogInterface, _: Int ->
                    cloudConnector.deleteSensor(
                        viewAdapter.cellSensors[position].address
                    )
                }, DialogInterface.OnClickListener { _, _ ->
                    viewAdapter.notifyDataSetChanged()
                }).show(it, "Delete confirm dialog")
            }
        }
    }

    @Subscribe
    fun onFetchCompletedEvent(event: FetchCompletedEvent) {

    }
}
