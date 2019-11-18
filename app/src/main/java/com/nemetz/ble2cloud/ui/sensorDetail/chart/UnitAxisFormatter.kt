package com.nemetz.ble2cloud.ui.sensorDetail.chart

import com.github.mikephil.charting.formatter.ValueFormatter

class UnitAxisFormatter(private var unit: String): ValueFormatter() {

    override fun getFormattedValue(value: Float): String {
        return "$value $unit"
    }

}