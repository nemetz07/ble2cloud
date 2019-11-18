package com.nemetz.ble2cloud.ui.sensorBrowser

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.*
import com.nemetz.ble2cloud.R
import com.nemetz.ble2cloud.data.MySensor
import com.nemetz.ble2cloud.utils.getMySensor

class SensorBrowserAdapter() :
    RecyclerView.Adapter<SensorBrowserAdapter.ViewHolder>(), EventListener<QuerySnapshot> {

    private val TAG = "SENSOR_BROWSER_ADAPTER"

    val cellSensors = arrayListOf<MySensor>()

    override fun onEvent(querySnapshot: QuerySnapshot?, e: FirebaseFirestoreException?) {
        // Handle errors
        if (e != null) {
            Log.w(TAG, "onEvent:error", e)
            return
        }

        for(change in querySnapshot!!.documentChanges) {
            when (change.type) {
                DocumentChange.Type.ADDED -> {
                    addSensor(change)
                    Log.w(TAG, "SENSOR added")
                }
                DocumentChange.Type.MODIFIED -> {
                    modifySensor(change)
                    Log.w(TAG, "SENSOR modified")
                }
                DocumentChange.Type.REMOVED -> {
                    removeSensor(change)
                    Log.w(TAG, "SENSOR removed")
                }
            }
        }
    }

    private fun addSensor(change: DocumentChange) {
        change.getMySensor().let {
            cellSensors.add(change.newIndex, it)
        }

        notifyItemInserted(change.newIndex)
    }

    fun modifySensor(change: DocumentChange) {
        if (change.oldIndex == change.newIndex) {
            // Item changed but remained in same position
            cellSensors[change.oldIndex] = change.getMySensor().let { it }
        } else {
            // Item changed and changed position
            cellSensors.removeAt(change.oldIndex)
            change.getMySensor().let { cellSensors.add(change.newIndex, it) }
            notifyItemMoved(change.oldIndex, change.newIndex)
        }
    }

    private fun removeSensor(change: DocumentChange) {
        cellSensors.removeAt(change.oldIndex)
        notifyItemRemoved(change.oldIndex)
    }

    lateinit var mClickListener: ItemClickListener

    interface ItemClickListener {
        fun onItemClick(view: View, position: Int)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        var sensorNameTV = view.findViewById<TextView>(R.id.sensorBrowserNameTV)
        var sensorAddressTv = view.findViewById<TextView>(R.id.sensorBrowserAddressTV)

        init {
            view.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            mClickListener.onItemClick(v!!, adapterPosition)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.sensor_browser_cell, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount() = cellSensors.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        val sensorBrowserCell: MySensor? = mSnapshots[position].toObject(MySensor::class.java)
        val mySensor = cellSensors[position]
        holder.sensorNameTV.text = mySensor.name
        holder.sensorAddressTv.text = mySensor.address
    }


    fun setClickListener(itemClickListener: ItemClickListener) {
        mClickListener = itemClickListener
    }
}