package com.nemetz.ble2cloud.ui.sensorDetail.chart

import android.content.Context
import android.widget.TextView
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.nemetz.ble2cloud.R
import com.nemetz.ble2cloud.SDF_DATE
import com.nemetz.ble2cloud.SDF_TIME
import java.util.*

class MyMarkerView(
    context: Context,
    layoutResource: Int,
    private val referenceTimestamp: Long,
    private val unit: String
) :
    MarkerView(context, layoutResource) {

    var dateTV: TextView = findViewById(R.id.customChartMarkerDateTV)
    var timeTV: TextView = findViewById(R.id.customChartMarkerTimeTV)
    var valueTV: TextView = findViewById(R.id.customChartMarkerValueTV)
    private val mDate: Date = Date()

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        val currentTimestamp = e?.x?.plus(referenceTimestamp)?.toLong()
        dateTV.text = getDate(currentTimestamp)
        timeTV.text = getTime(currentTimestamp)
        valueTV.text = "${e?.y} $unit"
    }

    override fun getPivotX(): Float {
        return (-(width / 2)).toFloat()
    }

    override fun getPivotY(): Float {
        return (-height).toFloat()
    }

    private fun getDate(timestamp: Long?): String {
        return try {
            if (timestamp != null) {
                mDate.time = timestamp * 1000
            }
            SDF_DATE.format(mDate)
        } catch (ex: Exception) {
            "xx"
        }
    }

    private fun getTime(timestamp: Long?): String {
        return try {
            if (timestamp != null) {
                mDate.time = timestamp * 1000
            }
            SDF_TIME.format(mDate)
        } catch (ex: Exception) {
            "xx"
        }
    }

}