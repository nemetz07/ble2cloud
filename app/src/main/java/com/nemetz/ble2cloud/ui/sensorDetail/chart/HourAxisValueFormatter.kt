package com.nemetz.ble2cloud.ui.sensorDetail.chart

import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class HourAxisValueFormatter(
    private val referenceTimestamp: Long // minimum timestamp in your data set
) : ValueFormatter() {

    private val mDataFormat: DateFormat
    private val mDate: Date

    val decimalDigits: Int
        get() = 0

    init {
        this.mDataFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        this.mDate = Date()
    }

    override fun getFormattedValue(value: Float): String {
        // convertedTimestamp = originalTimestamp - referenceTimestamp
        val convertedTimestamp = value.toLong()

        // Retrieve original timestamp
        val originalTimestamp = referenceTimestamp + convertedTimestamp

        // Convert timestamp to hour:minute
        return getHour(originalTimestamp)
    }

    private fun getHour(timestamp: Long): String {
        try {
            mDate.time = timestamp * 1000
            return mDataFormat.format(mDate)
        } catch (ex: Exception) {
            return "xx"
        }
    }
}