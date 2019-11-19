package com.nemetz.ble2cloud.ui.sensorDetail

import android.content.DialogInterface
import android.graphics.Color
import android.util.Log
import android.view.View
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModel
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.firestore.QuerySnapshot
import com.nemetz.ble2cloud.SDF_FULL
import com.nemetz.ble2cloud.connection.CloudConnector
import com.nemetz.ble2cloud.data.BLESensor
import com.nemetz.ble2cloud.data.BLESensorValue
import com.nemetz.ble2cloud.event.ChartAddedEvent
import com.nemetz.ble2cloud.event.SensorDetailMapUpdatedEvent
import com.nemetz.ble2cloud.ui.dialog.RangeDialogFragment
import com.nemetz.ble2cloud.ui.sensorDetail.chart.LineChartItem
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog
import org.greenrobot.eventbus.EventBus
import org.joda.time.DateTime
import org.joda.time.MutableDateTime
import java.util.*


class SensorDetailViewModel : ViewModel() {

    private var TAG = "SENSOR_DETAIL_VIEWMODEL"

    var sensor: BLESensor? = null
    val chartItems: ArrayList<LineChartItem> = arrayListOf()
    lateinit var cloudConnector: CloudConnector

    val markers: ArrayList<MyMarker> = arrayListOf()
    val addedMarkers: ArrayList<MyMarker> = arrayListOf()

    var isAlreadyInitialized: Boolean = false
    var latLngBoundsBuilder: LatLngBounds.Builder? = null

    lateinit var mMap: GoogleMap

    val onMapReadyCallback = OnMapReadyCallback { map ->
        mMap = map
        latLngBoundsBuilder = LatLngBounds.Builder()
    }

    private fun getRangeClickListener(
        fragmentManager: FragmentManager,
        BLESensorValue: BLESensorValue
    ): View.OnClickListener {
        return View.OnClickListener {
            RangeDialogFragment(DialogInterface.OnClickListener { dialog: DialogInterface, which: Int ->
                when (which) {
                    0 -> {
                        updateChart(
                            BLESensorValue = BLESensorValue,
                            startTimestamp = Timestamp(DateTime.now().minusHours(1).toDate()),
                            endTimestamp = Timestamp(DateTime.now().toDate()),
                            fragmentManager = fragmentManager
                        )
                    }
                    1 -> {
                        updateChart(
                            BLESensorValue = BLESensorValue,
                            startTimestamp = Timestamp(DateTime.now().minusHours(3).toDate()),
                            endTimestamp = Timestamp(DateTime.now().toDate()),
                            fragmentManager = fragmentManager
                        )
                    }
                    2 -> {
                        updateChart(
                            BLESensorValue = BLESensorValue,
                            startTimestamp = Timestamp(DateTime.now().minusDays(1).toDate()),
                            endTimestamp = Timestamp(DateTime.now().toDate()),
                            fragmentManager = fragmentManager
                        )
                    }
                    else -> {
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
                                    BLESensorValue = BLESensorValue,
                                    startTimestamp = Timestamp(startDateTime.toDate()),
                                    endTimestamp = Timestamp(endDateTime.toDate()),
                                    fragmentManager = fragmentManager
                                )
                            },
                            now.hourOfDay,
                            now.minuteOfHour,
                            true
                        ).also {
                            it.setOkColor(0xFFFFFFFF.toInt())
                            it.setCancelColor(0xFFFFFFFF.toInt())
                            it.title = "End Time"
                        }

                        val endDatePickerDialog =
                            DatePickerDialog.newInstance { view, year, monthOfYear, dayOfMonth ->
                                endDateTime.year = year
                                endDateTime.monthOfYear = monthOfYear
                                endDateTime.dayOfMonth = dayOfMonth
                                fragmentManager.let {
                                    endTimePickerDialog.show(
                                        it,
                                        "EndTimePickerDialog"
                                    )
                                }
                            }.also {
                                it.setOkColor(0xFFFFFFFF.toInt())
                                it.setCancelColor(0xFFFFFFFF.toInt())
                                it.setTitle("End Date")
                            }

                        val startTimePickerDialog = TimePickerDialog.newInstance(
                            { view, hourOfDay, minute, second ->
                                startDateTime.hourOfDay = hourOfDay
                                startDateTime.minuteOfHour = minute
                                startDateTime.secondOfMinute = second
                                fragmentManager.let {
                                    endDatePickerDialog.show(
                                        it,
                                        "EndDatePickerDialog"
                                    )
                                }
                            },
                            now.hourOfDay,
                            now.minuteOfHour,
                            true
                        ).also {
                            it.setOkColor(0xFFFFFFFF.toInt())
                            it.setCancelColor(0xFFFFFFFF.toInt())
                            it.title = "Start Time"
                        }

                        val startDatePickerDialog =
                            DatePickerDialog.newInstance { view, year, monthOfYear, dayOfMonth ->
                                startDateTime.year = year
                                startDateTime.monthOfYear = monthOfYear
                                startDateTime.dayOfMonth = dayOfMonth
                                fragmentManager.let {
                                    startTimePickerDialog.show(
                                        it,
                                        "StartTimePickerDialog"
                                    )
                                }
                            }.also {
                                it.setOkColor(0xFFFFFFFF.toInt())
                                it.setCancelColor(0xFFFFFFFF.toInt())
                                it.setTitle("Start Date")
                            }
                        fragmentManager.let { fragmentManager ->
                            startDatePickerDialog.show(
                                fragmentManager,
                                "StartDatePickerDialog"
                            )
                        }
                    }
                }
            }).show(fragmentManager, "RangePickerDialog")
        }
    }

    fun fetchCharts(
        fragmentManager: FragmentManager
    ) {
        sensor!!.values.forEach { sensorValue ->
            cloudConnector.getData(
                address = sensor!!.address,
                name = sensorValue.format!!.name
            ).addOnCompleteListener { querySnapshot ->
                initChart(
                    querySnapshot,
                    sensorValue,
                    getRangeClickListener(fragmentManager, sensorValue)
                )
                updateMap()
            }
        }

    }

    private fun updateChart(
        BLESensorValue: BLESensorValue,
        startTimestamp: Timestamp,
        endTimestamp: Timestamp,
        fragmentManager: FragmentManager
    ) {
        val position =
            chartItems.indexOf(chartItems.find { it.chartName == BLESensorValue.format?.name })

        cloudConnector.getDataBetween(
            address = sensor!!.address,
            name = BLESensorValue.format!!.name,
            startTimestamp = startTimestamp,
            endTimestamp = endTimestamp,
            limit = 100
        ).addOnCompleteListener { querySnapshot ->
            chartItems.removeAt(position)
            initChart(
                querySnapshot,
                BLESensorValue,
                getRangeClickListener(fragmentManager, BLESensorValue),
                position
            )
            updateMap()
        }
    }

    private fun initChart(
        querySnapshot: Task<QuerySnapshot>,
        BLESensorValue: BLESensorValue,
        onRangeButtonClickListener: View.OnClickListener,
        position: Int? = null
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
                val latitude = document.getDouble("latitude")
                val longitude = document.getDouble("longitude")
                if ((latitude != null) and (longitude != null)) {
                    markers.add(
                        MyMarker(
                            latLng = LatLng(latitude!!, longitude!!),
                            title = SDF_FULL.format(Date(timestamp?.plus(referenceTime)!! * 1000))
                        )
                    )
                }
                Log.d(
                    TAG,
                    "DATA for ${BLESensorValue.format!!.name}: ${timestamp}, ${value}, ($latitude, $longitude)"
                )

                entries.add(
                    Entry(
                        timestamp?.toFloat() ?: 0f,
                        value
                    )
                )
            }
        }
        val lineDataSet = LineDataSet(entries.reversed(), BLESensorValue.format!!.name)
        styleDataSet(lineDataSet)

        if (position == null) {
            chartItems.add(
                LineChartItem(
                    LineData(lineDataSet),
                    referenceTime ?: 0,
                    BLESensorValue.format!!.name,
                    onRangeButtonClickListener,
                    BLESensorValue.format!!.unit
                )
            )
        } else {
            chartItems.add(
                position,
                LineChartItem(
                    LineData(lineDataSet),
                    referenceTime ?: 0,
                    BLESensorValue.format!!.name,
                    onRangeButtonClickListener,
                    BLESensorValue.format!!.unit
                )
            )
        }
        EventBus.getDefault().post(ChartAddedEvent())
    }

    private fun styleDataSet(lineDataSet: LineDataSet) {
        lineDataSet.apply {
            lineWidth = 0f
            circleRadius = 3f
            setDrawCircleHole(false)
            highLightColor = Color.rgb(244, 117, 117)
            setCircleColor(Color.parseColor("#26A69A"))
            setDrawValues(false)
        }
    }

    private fun updateMap() {
        if (markers.isNotEmpty()) {
            markers.forEach { marker ->
                if (!addedMarkers.contains(marker)) {
                    mMap.addMarker(MarkerOptions().position(marker.latLng).title(marker.title))
                    latLngBoundsBuilder?.include(marker.latLng)
                    addedMarkers.add(marker)
                }
            }
            val latLngBounds = latLngBoundsBuilder?.build()
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 100))
            markers.clear()
            EventBus.getDefault().post(SensorDetailMapUpdatedEvent())
        }
    }
}