package com.nemetz.ble2cloud.ui.sensorDetail

import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.Drawable
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
import com.nemetz.ble2cloud.ui.dialog.EditSensorDialogFragment
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

    private val markers: ArrayList<MyMarker> = arrayListOf()
    private val addedMarkers: ArrayList<MyMarker> = arrayListOf()

    var isAlreadyInitialized: Boolean = false
    private var latLngBoundsBuilder: LatLngBounds.Builder? = null

    private lateinit var mMap: GoogleMap

    var outlierDataIcon: Drawable? = null

    val onMapReadyCallback = OnMapReadyCallback { map ->
        mMap = map
        latLngBoundsBuilder = LatLngBounds.Builder()
    }

    private fun getEditClickListener(
        fragmentManager: FragmentManager,
        sensorValue: BLESensorValue
    ): View.OnClickListener {

        return View.OnClickListener {
            Log.d("EDIT", "onClick")

            val transaction = fragmentManager.beginTransaction()
            transaction.apply {
                add(EditSensorDialogFragment(sensor!!.address, sensorValue), "EditSensorDialog")
                commitAllowingStateLoss()
            }
        }

    }

    private fun getRangeClickListener(
        fragmentManager: FragmentManager,
        BLESensorValue: BLESensorValue
    ): View.OnClickListener {
        return View.OnClickListener {
            RangeDialogFragment(DialogInterface.OnClickListener { _: DialogInterface, which: Int ->
                when (which) {
                    0 -> {
                        updateChart(
                            sensorValue = BLESensorValue,
                            startTimestamp = Timestamp(DateTime.now().minusHours(1).toDate()),
                            endTimestamp = Timestamp(DateTime.now().toDate()),
                            fragmentManager = fragmentManager
                        )
                    }
                    1 -> {
                        updateChart(
                            sensorValue = BLESensorValue,
                            startTimestamp = Timestamp(DateTime.now().minusHours(3).toDate()),
                            endTimestamp = Timestamp(DateTime.now().toDate()),
                            fragmentManager = fragmentManager
                        )
                    }
                    2 -> {
                        updateChart(
                            sensorValue = BLESensorValue,
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
                                    sensorValue = BLESensorValue,
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
        sensor!!.values.forEach { (name, sensorValue) ->
            cloudConnector.getData(
                address = sensor!!.address,
                name = name
            ).addOnCompleteListener { querySnapshot ->
                initChart(
                    querySnapshot,
                    sensorValue,
                    getRangeClickListener(fragmentManager, sensorValue),
                    getEditClickListener(fragmentManager, sensorValue)
                )
                updateMap()
            }
        }

    }

    private fun updateChart(
        sensorValue: BLESensorValue,
        startTimestamp: Timestamp,
        endTimestamp: Timestamp,
        fragmentManager: FragmentManager
    ) {
        val position =
            chartItems.indexOf(chartItems.find { it.chartName == sensorValue.format?.name })

        cloudConnector.getDataBetween(
            address = sensor!!.address,
            name = sensorValue.format!!.name,
            startTimestamp = startTimestamp,
            endTimestamp = endTimestamp,
            limit = 100
        ).addOnCompleteListener { querySnapshot ->
            chartItems.removeAt(position)
            initChart(
                querySnapshot,
                sensorValue,
                getRangeClickListener(fragmentManager, sensorValue),
                getEditClickListener(fragmentManager, sensorValue),
                position
            )
            updateMap()
        }
    }

    private fun initChart(
        querySnapshot: Task<QuerySnapshot>,
        BLESensorValue: BLESensorValue,
        onRangeButtonClickListener: View.OnClickListener,
        onEditButtonClickListener: View.OnClickListener,
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

        entries.reverse()

        simpleValidate(entries)
        val lineDataSet = LineDataSet(entries, BLESensorValue.format!!.name)
        styleDataSet(lineDataSet)

        if (position == null) {
            chartItems.add(
                LineChartItem(
                    LineData(lineDataSet),
                    referenceTime ?: 0,
                    BLESensorValue.format!!.name,
                    onRangeButtonClickListener,
                    onEditButtonClickListener,
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
                    onEditButtonClickListener,
                    BLESensorValue.format!!.unit
                )
            )
        }
        EventBus.getDefault().post(ChartAddedEvent())
    }

    private fun simpleValidate(entries: ArrayList<Entry>) {
        val sortedEntries = entries.sortedBy {
            it.y
        }

        val lowerEntries: List<Entry>
        val upperEntries: List<Entry>

        if (sortedEntries.size % 2 == 0) {
            lowerEntries = sortedEntries.subList(0, sortedEntries.size / 2)
            upperEntries = sortedEntries.subList(sortedEntries.size / 2, sortedEntries.size)
        } else {
            lowerEntries = sortedEntries.subList(0, sortedEntries.size / 2)
            upperEntries =
                sortedEntries.subList(sortedEntries.size / 2 + 1, sortedEntries.size)
        }

        val q1 = median(lowerEntries)
        val q3 = median(upperEntries)

        val iqr = q3 - q1
        val lowerFence = q1 - 1.5 * iqr
        val upperFence = q3 + 1.5 * iqr

        Log.d(TAG, "Lower fence: $lowerFence, Upper fence: $upperFence")

        entries.forEach {
            if ((it.y > upperFence) or (it.y < lowerFence)) {
                it.icon = outlierDataIcon
            }
        }
    }

    private fun validateData(entries: ArrayList<Entry>) {
        val sections = arrayListOf<ArrayList<Entry>>()

        var k = 0
        var n = 0

        for (i in 0 until entries.size) {
            if (i > 0) {
                if ((entries[i].x - entries[i - 1].x > 5000f) or (n > 20)) {
                    k++
                    sections.add(k, arrayListOf())
                    n = 0
                }
            } else {
                sections.add(k, arrayListOf())
            }

            sections[k].add(n, entries[i])
            n++
        }

        sections.forEach { sectionEntries ->

            if (sectionEntries.size > 1) {

                val sortedEntries = sectionEntries.sortedBy {
                    it.y
                }

                val lowerEntries: List<Entry>
                val upperEntries: List<Entry>

                if (sortedEntries.size % 2 == 0) {
                    lowerEntries = sortedEntries.subList(0, sortedEntries.size / 2)
                    upperEntries = sortedEntries.subList(sortedEntries.size / 2, sortedEntries.size)
                } else {
                    lowerEntries = sortedEntries.subList(0, sortedEntries.size / 2)
                    upperEntries =
                        sortedEntries.subList(sortedEntries.size / 2 + 1, sortedEntries.size)
                }

                val q1 = median(lowerEntries)
                val q3 = median(upperEntries)

                val iqr = q3 - q1
                val lowerFence = q1 - 1.5 * iqr
                val upperFence = q3 + 1.5 * iqr

                Log.d(TAG, "Lower fence: $lowerFence, Upper fence: $upperFence")

//            val colors = arrayListOf<Int>()

                sectionEntries.forEach {
                    if ((it.y > upperFence) or (it.y < lowerFence)) {
//                    colors.add(Color.parseColor("#A62639"))
                        it.icon = outlierDataIcon
//                } else {
////                    colors.add(Color.parseColor("#26A69A"))
                    }
                }
            }
        }
    }

    private fun median(data: List<Entry>): Float {
        if (data.size % 2 == 0)
            return (data[data.size / 2].y + data[data.size / 2 - 1].y) / 2
        else
            return data[data.size / 2].y
    }

    private fun styleDataSet(lineDataSet: LineDataSet) {
        lineDataSet.apply {
            lineWidth = 0f
            circleRadius = 3f
            setDrawCircleHole(false)
            setCircleColor(Color.parseColor("#26A69A"))
            highLightColor = Color.rgb(244, 117, 117)
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