package com.nemetz.ble2cloud.ui.dataCollection

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.nemetz.ble2cloud.BLE2CloudApplication
import com.nemetz.ble2cloud.MainActivity
import com.nemetz.ble2cloud.R
import com.nemetz.ble2cloud.event.DataCollectionAddedEvent
import com.nemetz.ble2cloud.service.DataCollectionService
import com.nemetz.ble2cloud.ui.base.BaseFragment
import com.nemetz.ble2cloud.utils.FirebaseCollections
import kotlinx.android.synthetic.main.data_collection_fragment.*
import org.greenrobot.eventbus.Subscribe
import org.joda.time.DateTime

class DataCollectionFragment : BaseFragment() {

    companion object {
        fun newInstance() = DataCollectionFragment()
    }

    private lateinit var viewModel: DataCollectionViewModel
    private lateinit var viewAdapter: DataCollectionAdapter
    private lateinit var viewManager: LinearLayoutManager

    private var mRegistration: ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.data_collection_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(DataCollectionViewModel::class.java)
        viewAdapter =
            DataCollectionAdapter(viewModel.cellData, (context as MainActivity).viewModel.sensors)
        viewManager = LinearLayoutManager(context)

        dataCollectionRecyclerView.apply {
            adapter = viewAdapter
            layoutManager = viewManager
        }

        init()
    }

    private fun init() {
        val application = context!!.applicationContext as BLE2CloudApplication

        if ((application.isServiceRunning.value == false) or (application.startTime == null)) {
            application.startTime = Timestamp(DateTime.now().toDate())
        }

        dataCollectionBackButton.setOnClickListener {
            findNavController().navigateUp()
        }

        dataCollectionStopButton.setOnClickListener {
            Intent(context, DataCollectionService::class.java).apply { action = "STOP" }
                .also { context!!.startService(it) }

            findNavController().navigateUp()
        }

        if (viewModel.mRegistration == null) {
            viewModel.mRegistration = FirebaseFirestore.getInstance()
                .collectionGroup(FirebaseCollections.DATA)
                .whereGreaterThan("createdAt", application.startTime!!)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener(viewAdapter)
        }
    }

    @Subscribe
    fun onDataCollectionAdded(event: DataCollectionAddedEvent) {
        viewManager.scrollToPosition(0)
    }

}
