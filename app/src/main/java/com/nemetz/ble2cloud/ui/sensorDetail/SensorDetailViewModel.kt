package com.nemetz.ble2cloud.ui.sensorDetail

import android.graphics.Color
import android.util.Log
import android.view.View
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModel
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.firestore.QuerySnapshot
import com.nemetz.ble2cloud.data.CloudConnector
import com.nemetz.ble2cloud.data.MySensor
import com.nemetz.ble2cloud.data.SensorValue
import com.nemetz.ble2cloud.event.ChartAddedEvent
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog
import org.greenrobot.eventbus.EventBus
import org.joda.time.DateTime
import org.joda.time.MutableDateTime
import java.util.*

class SensorDetailViewModel : ViewModel() {

    private var TAG = "SENSOR_DETAIL_VIEWMODEL"

    var sensor: MySensor? = null
    val chartItems: ArrayList<LineChartItem> = arrayListOf()
    lateinit var cloudConnector: CloudConnector

    private fun getRangeClickListener(
        fragmentManager: FragmentManager,
        sensorValue: SensorValue
    ): View.OnClickListener {
        return View.OnClickListener {
            val now = DateTime.now()

            val startDateTime: MutableDateTime = MutableDateTime.now()
            val endDateTime: MutableDateTime = MutableDateTime.now()

            val endTimePickerDialog = TimePickerDialog.newInstance(
                { view, hourOfDay, minute, second ->
                    endDateTime.hourOfDay = hourOfDay
                    endDateTime.minuteOfHour = minute
                    endDateTime.secondOfMinute = second

                    startDateTime.addHours(745)
                    endDateTime.addHours(745)

                    updateChart(
                        sensorValue = sensorValue,
                        startTimestamp = Timestamp(startDateTime.toDate()),
                        endTimestamp = Timestamp(endDateTime.toDate()),
                        fragmentManager = fragmentManager
                    )
                },
                now.hourOfDay,
                now.minuteOfHour,
                true
            )
            endTimePickerDialog.title = "End Time"

            val endDatePickerDialog = DatePickerDialog.newInstance { view, year, monthOfYear, dayOfMonth ->
                endDateTime.year = year
                endDateTime.monthOfYear = monthOfYear
                endDateTime.dayOfMonth = dayOfMonth
                fragmentManager.let { endTimePickerDialog.show(it, "EndTimePickerDialog") }
            }
            endDatePickerDialog.setTitle("End Date")

            val startTimePickerDialog = TimePickerDialog.newInstance(
                { view, hourOfDay, minute, second ->
                    startDateTime.hourOfDay = hourOfDay
                    startDateTime.minuteOfHour = minute
                    startDateTime.secondOfMinute = second
                    fragmentManager.let { endDatePickerDialog.show(it, "EndDatePickerDialog") }
                },
                now.hourOfDay,
                now.minuteOfHour,
                true
            )
            startTimePickerDialog.title = "Start Time"

            val startDatePickerDialog = DatePickerDialog.newInstance { view, year, monthOfYear, dayOfMonth ->
                startDateTime.year = year
                startDateTime.monthOfYear = monthOfYear
                startDateTime.dayOfMonth = dayOfMonth
                fragmentManager.let { startTimePickerDialog.show(it, "StartTimePickerDialog") }
            }
            startDatePickerDialog.setTitle("Start Date")
            fragmentManager.let { fragmentManager ->
                startDatePickerDialog.show(
                    fragmentManager,
                    "StartDatePickerDialog"
                )
            }
        }
    }


    fun fetchCharts(
        fragmentManager: FragmentManager
    ) {
        sensor!!.values.forEach { sensorValue ->
            cloudConnector.getDataForSensorValue(
                address = sensor!!.address,
                name = sensorValue.format!!.name
            ).addOnCompleteListener { querySnapshot ->
                initChart(
                    querySnapshot,
                    sensorValue,
                    getRangeClickListener(fragmentManager, sensorValue)
                )
            }
        }
    }

    fun updateChart(
        sensorValue: SensorValue,
        startTimestamp: Timestamp,
        endTimestamp: Timestamp,
        fragmentManager: FragmentManager
    ) {
        chartItems.remove(chartItems.find { it.chartName == sensorValue.format?.name })

        cloudConnector.getDataForSensorValue(
            address = sensor!!.address,
            name = sensorValue.format!!.name,
            startTimestamp = startTimestamp,
            endTimestamp = endTimestamp
        ).addOnCompleteListener { querySnapshot ->
            initChart(querySnapshot, sensorValue, getRangeClickListener(fragmentManager, sensorValue))
        }
    }

    private fun initChart(
        querySnapshot: Task<QuerySnapshot>,
        sensorValue: SensorValue,
        onRangeButtonClickListener: View.OnClickListener
    ) {
        val entries = arrayListOf<Entry>()
        var referenceTime: Long? = 0
        if (!querySnapshot.result?.isEmpty!!) {
            referenceTime =
                querySnapshot.result?.documents?.get(querySnapshot.result?.documents!!.size - 1)?.getTimestamp(
                    "createdAt"
                )?.seconds!!

            querySnapshot.result?.documents?.forEach { document ->
                val timestamp = document.getTimestamp("createdAt")?.seconds?.minus(referenceTime)
                val value = document.get("value").toString().toFloat()

                Log.d(
                    TAG,
                    "DATA for ${sensorValue.format!!.name}: ${timestamp}, ${value}"
                )

                entries.add(
                    Entry(
                        timestamp?.toFloat() ?: 0f,
                        value
                    )
                )
            }
        }
        val lineDataSet = LineDataSet(entries.reversed(), sensorValue.format!!.name)
        styleDataSet(lineDataSet)

        chartItems.add(
            LineChartItem(
                LineData(lineDataSet),
                referenceTime ?: 0,
                sensorValue.format!!.name,
                onRangeButtonClickListener
            )
        )
        EventBus.getDefault().post(ChartAddedEvent())
    }

    private fun styleDataSet(lineDataSet: LineDataSet) {
        lineDataSet.apply {
            lineWidth = 2.5f
            circleRadius = 4.5f
            highLightColor = Color.rgb(244, 117, 117)
            setCircleColor(Color.parseColor("#26A69A"))
            setDrawValues(false)
        }
    }

}
