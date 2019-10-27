package com.nemetz.ble2cloud.ui.deviceBrowser.fragment

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.nemetz.ble2cloud.BLEApplication
import com.nemetz.ble2cloud.R
import com.nemetz.ble2cloud.event.AutoConnectEvent
import com.nemetz.ble2cloud.ui.base.factory.BaseViewModelFactory
import com.nemetz.ble2cloud.ui.base.fragment.BaseFragment
import com.nemetz.ble2cloud.ui.deviceBrowser.adapter.DeviceBrowserAdapter
import com.nemetz.ble2cloud.ui.deviceBrowser.fragment.DeviceBrowserFragmentDirections
import com.nemetz.ble2cloud.ui.deviceBrowser.viewmodel.DeviceBrowserViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class DeviceBrowserFragment : BaseFragment() {
    override val TAG = "DEVICE_BROWSER_FRAGMENT"

    private lateinit var viewModel: DeviceBrowserViewModel
    private lateinit var activity: Activity
    private lateinit var application: BLEApplication

    companion object {
        fun newInstance() = DeviceBrowserFragment()
    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: DeviceBrowserAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager

    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.device_browser_fragment, container, false)
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
        application = getActivity()!!.application as BLEApplication
        viewModel =
            ViewModelProviders.of(this, BaseViewModelFactory(application)).get(
                DeviceBrowserViewModel::class.java)
        init()

        swipeRefreshLayout = activity.findViewById(R.id.device_browser_refresh)
        swipeRefreshLayout.setOnRefreshListener {
            GlobalScope.launch {
                if (viewModel.scanDevices()) {
                    Log.d("BLEScan", "Scan finished!")
                    activity.runOnUiThread {
                        viewAdapter.cellDevices = viewModel.getDevices()
                        viewAdapter.notifyDataSetChanged()
                    }
                    swipeRefreshLayout.isRefreshing = false
                }
            }
        }
    }

    fun init() {
        viewManager = LinearLayoutManager(context)
        viewAdapter = DeviceBrowserAdapter(viewModel.getDevices())
        viewAdapter.setClickListener(object : DeviceBrowserAdapter.ItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                val action =
                    DeviceBrowserFragmentDirections.openDetailsAction(viewAdapter.getItem(position))
                findNavController().navigate(action)
            }
        })

        recyclerView = activity.findViewById<RecyclerView>(R.id.device_browser_recycler_view).apply {
            setHasFixedSize(true)

            layoutManager = viewManager
            adapter = viewAdapter
        }
    }

    @Subscribe
    fun onAutoConnect(event: AutoConnectEvent){
        viewModel.autoConnect()
    }

}
