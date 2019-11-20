package com.nemetz.ble2cloud.ui.sensorDetail

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.nemetz.ble2cloud.ui.sensorDetail.chart.ChartItem

class ChartDataAdapter(
    context: Context,
    objects: List<ChartItem>
) : ArrayAdapter<ChartItem>(context, 0, objects) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return getItem(position)!!.getView(position, convertView, context, parent)!!
    }

    override fun getItemViewType(position: Int): Int {
        // return the views type
        val ci = getItem(position) as ChartItem
        return ci.itemType
    }

    override fun getViewTypeCount(): Int {
        return 3 // we have 3 different item-types
    }
}