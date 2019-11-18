package com.nemetz.ble2cloud.ui.sensorDetail.chart

import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.github.mikephil.charting.data.ChartData

abstract class ChartItem internal constructor(internal var mChartData: ChartData<*>) {

    abstract val itemType: Int

    abstract fun getView(position: Int, convertView: View?, context: Context, parent: ViewGroup): View?

    companion object {

        internal val TYPE_BARCHART = 0
        internal val TYPE_LINECHART = 1
        internal val TYPE_PIECHART = 2
    }
}