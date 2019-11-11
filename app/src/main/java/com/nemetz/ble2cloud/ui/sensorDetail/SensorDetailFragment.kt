package com.nemetz.ble2cloud.ui.sensorDetail

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.navArgs
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import com.nemetz.ble2cloud.BLE2CloudApplication
import com.nemetz.ble2cloud.R
import com.nemetz.ble2cloud.event.ChartAddedEvent
import com.nemetz.ble2cloud.ui.base.BaseFragment
import com.nemetz.ble2cloud.uiScope
import kotlinx.android.synthetic.main.sensor_detail_fragment.*
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.Subscribe
import java.util.*


class SensorDetailFragment : BaseFragment() {

    override val TAG = "SENSOR_DETAIL_FRAGMENT"

    companion object {
        fun newInstance() = SensorDetailFragment()
    }

    val args: SensorDetailFragmentArgs by navArgs()

    private lateinit var viewModel: SensorDetailViewModel
    private lateinit var viewAdapter: ChartDataAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.sensor_detail_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(SensorDetailViewModel::class.java)

        init()
    }

    private fun init() {
        viewModel.sensor = args.sensor

        viewAdapter = ChartDataAdapter(context!!, viewModel.chartItems)
        chartListView.adapter = viewAdapter

        sensorDetailNameTV.text = args.sensor.name

        viewModel.cloudConnector = (context?.applicationContext as BLE2CloudApplication).cloudConnector

        fragmentManager?.let { viewModel.fetchCharts(it) }
    }

    private fun generateDataLine(cnt: Int): LineData {

        val values1 = ArrayList<Entry>()

        for (i in 0..11) {
            values1.add(Entry(i.toFloat(), ((Math.random() * 65).toInt() + 40).toFloat()))
        }

        val d1 = LineDataSet(values1, "New DataSet $cnt, (1)")
        d1.lineWidth = 2.5f
        d1.circleRadius = 4.5f
        d1.highLightColor = Color.rgb(244, 117, 117)
        d1.setDrawValues(false)

        val values2 = ArrayList<Entry>()

        for (i in 0..11) {
            values2.add(Entry(i.toFloat(), values1[i].y - 30))
        }

        val d2 = LineDataSet(values2, "New DataSet $cnt, (2)")
        d2.lineWidth = 2.5f
        d2.circleRadius = 4.5f
        d2.highLightColor = Color.rgb(244, 117, 117)
        d2.color = ColorTemplate.VORDIPLOM_COLORS[0]
        d2.setCircleColor(ColorTemplate.VORDIPLOM_COLORS[0])
        d2.setDrawValues(false)

        val sets = ArrayList<ILineDataSet>()
        sets.add(d1)
        sets.add(d2)

        return LineData(sets)
    }

    @Subscribe
    fun onChartAdded(event: ChartAddedEvent) {
        uiScope.launch {
            viewAdapter.notifyDataSetChanged()
        }
    }

}
