package com.nemetz.ble2cloud.ui.scanner

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.nemetz.ble2cloud.R
import com.nemetz.ble2cloud.event.AutoConnectEvent
import com.nemetz.ble2cloud.ui.base.BaseFragment
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class ScannerFragment : BaseFragment() {
    override val TAG = "SCANNER_FRAGMENT"

    private lateinit var viewModel: ScannerViewModel
    private lateinit var activity: Activity

    companion object {
        fun newInstance() = ScannerFragment()
    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: ScannerAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager

    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.scanner_fragment, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()

        EventBus.getDefault().unregister(this)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity = context as Activity
        viewModel = ViewModelProviders.of(this).get(ScannerViewModel::class.java)
        init()

        swipeRefreshLayout = activity.findViewById(R.id.scanner_refresh)
        swipeRefreshLayout.setOnRefreshListener {
            GlobalScope.launch {
                if (viewModel.scanSensors()) {
                    Log.d("BLEScan", "Scan finished!")
                    activity.runOnUiThread {
                        viewAdapter.cellSensors = viewModel.getSensors()
                        viewAdapter.notifyDataSetChanged()
                    }
                    swipeRefreshLayout.isRefreshing = false
                }
            }
        }
    }

    fun init() {
        viewManager = LinearLayoutManager(context)
        viewAdapter =
            ScannerAdapter(viewModel.getSensors())
        viewAdapter.setClickListener(object : ScannerAdapter.ItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                Log.d(TAG, "Item clicked!")
            }
        })

        recyclerView = activity.findViewById<RecyclerView>(R.id.scanner_recyclerview).apply {
            setHasFixedSize(true)

            layoutManager = viewManager
            adapter = viewAdapter
        }

        Log.d(TAG, "${viewModel}")

        viewAdapter.cellSensors = viewModel.getSensors()
        viewAdapter.notifyDataSetChanged()
    }

    @Subscribe
    fun onAutoConnect(event: AutoConnectEvent){
        viewModel.autoConnect()
    }

}
