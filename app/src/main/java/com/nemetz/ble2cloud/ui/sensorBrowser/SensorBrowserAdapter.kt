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

class SensorBrowserAdapter(private val mySensors: ArrayList<MySensor>) :
    RecyclerView.Adapter<SensorBrowserAdapter.ViewHolder>(), EventListener<QuerySnapshot> {

    private val TAG = "SENSOR_BROWSER_ADAPTER"

    override fun onEvent(querySnapshot: QuerySnapshot?, e: FirebaseFirestoreException?) {
        // Handle errors
        if (e != null) {
            Log.w(TAG, "onEvent:error", e)
            return
        }

        notifyDataSetChanged()
    }

    lateinit var mClickListener: ItemClickListener

    interface ItemClickListener {
        fun onItemClick(view: View, position: Int)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        var sensorNameTV = view.findViewById<TextView>(R.id.sensor_browser_cell_name)

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

    override fun getItemCount() = mySensors.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        val sensorBrowserCell: MySensor? = mSnapshots[position].toObject(MySensor::class.java)
        val mySensor = mySensors[position]
        holder.sensorNameTV.text = mySensor.name
    }


    fun setClickListener(itemClickListener: ItemClickListener) {
        mClickListener = itemClickListener
    }
}