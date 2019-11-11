package com.nemetz.ble2cloud.ui.sensorDetail

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.ChartData
import com.github.mikephil.charting.data.LineData
import com.nemetz.ble2cloud.R
import com.nemetz.ble2cloud.utils.HourAxisValueFormatter
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog
import java.util.*

class LineChartItem(
    cd: ChartData<*>,
    private var referenceTimestamp: Long,
    var chartName: String,
    private var onRangeButtonClickListener: View.OnClickListener
) : ChartItem(cd) {

    private lateinit var mTf: Typeface

    override val itemType: Int
        get() = TYPE_LINECHART

    @Suppress("NAME_SHADOWING")
    override fun getView(
        position: Int,
        convertView: View?,
        context: Context,
        parent: ViewGroup
    ): View? {
        val holder: ViewHolder
        var convertView: View? = convertView

        if (convertView == null) {
            holder = ViewHolder()
            convertView =
                LayoutInflater.from(context).inflate(R.layout.list_item_linechart, parent, false)
            holder.chart = (convertView as View).findViewById(R.id.chart)
            holder.chartNameTV = convertView.findViewById(R.id.chartNameTV)
            holder.chartSetRangeButton = convertView.findViewById(R.id.chartSetRangeButton)
            convertView.tag = holder
        } else {
            holder = convertView.tag as ViewHolder
        }

        mTf = Typeface.createFromAsset(convertView.context.assets, "open_sans.ttf")

        holder.chartNameTV!!.text = this.chartName

        // apply styling
        holder.chart!!.description.isEnabled = false
        holder.chart!!.setDrawGridBackground(false)

        val xAxis = holder.chart!!.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.typeface = mTf
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(true)
        val valueFormatter = HourAxisValueFormatter(referenceTimestamp)
        xAxis.valueFormatter = valueFormatter

        val leftAxis = holder.chart!!.axisLeft
        leftAxis.typeface = mTf
        leftAxis.setLabelCount(5, false)
        leftAxis.axisMinimum = mChartData.yMin * 0.9f
        leftAxis.axisMaximum = mChartData.yMax * 1.1f

        // set data
        holder.chart!!.data = mChartData as LineData

        // do not forget to refresh the chart
        // holder.chart.invalidate();
        holder.chart!!.animateX(2000)

        holder.chartSetRangeButton?.setOnClickListener(onRangeButtonClickListener)

        return convertView
    }

    private class ViewHolder {
        internal var chart: LineChart? = null
        internal var chartNameTV: TextView? = null
        internal var chartSetRangeButton: Button? = null
    }
}