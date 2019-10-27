package com.nemetz.ble2cloud.ui.scanner.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.nemetz.ble2cloud.R
import com.nemetz.ble2cloud.ui.scanner.model.SensorCell
import kotlin.collections.ArrayList

class ScannerAdapter(var cellSensors: ArrayList<SensorCell>) :
    RecyclerView.Adapter<ScannerAdapter.ScannerViewHolder>() {

    lateinit var mClickListener: ItemClickListener
    private lateinit var context: Context

    interface ItemClickListener{
        fun onItemClick(view: View, position: Int)
    }

    inner class ScannerViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        var nameTextView: TextView = view.findViewById(R.id.sensor_name_tv)
        var addressTextView: TextView = view.findViewById(R.id.sensor_address_tv)
        var rssiTextView: TextView = view.findViewById(R.id.sensor_rssi_tv)
        var icon: ImageView = view.findViewById(R.id.sensor_icon)
        var legacyTextView: TextView = view.findViewById(R.id.sensor_connectable)

        init{
            view.setOnClickListener(this)
        }

        override fun onClick(view: View?) {
            mClickListener.onItemClick(view!!, adapterPosition)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScannerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.sensor_cell, parent, false)

        context = parent.context

        return ScannerViewHolder(view)
    }

    fun getItem(id: Int): SensorCell {
        return cellSensors[id]
    }

    override fun getItemCount() = cellSensors.size

    override fun onBindViewHolder(holder: ScannerViewHolder, position: Int) {
        val sensorCell: SensorCell = cellSensors[position]

        holder.nameTextView.text = sensorCell.device.name ?: "-"
        holder.addressTextView.text = sensorCell.device.address
        holder.rssiTextView.text = "${sensorCell.rssi} dB"
//        holder.icon.src = holder.itemView.context.getString(R.string.icon_mobile)
        holder.legacyTextView.text = if(sensorCell.isConnectable) "Connectable" else ""
    }

    fun setClickListener(itemClickListener: ItemClickListener){
        mClickListener = itemClickListener
    }

}
